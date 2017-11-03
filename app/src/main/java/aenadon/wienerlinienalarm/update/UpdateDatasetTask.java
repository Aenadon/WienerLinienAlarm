package aenadon.wienerlinienalarm.update;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.utils.DatasetUpdateStatus;
import aenadon.wienerlinienalarm.utils.Keys;
import hugo.weaving.DebugLog;
import trikita.log.Log;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.List;

import aenadon.wienerlinienalarm.enums.Direction;
import aenadon.wienerlinienalarm.enums.NetworkStatus;
import aenadon.wienerlinienalarm.enums.TransportType;
import aenadon.wienerlinienalarm.exceptions.NetworkClientException;
import aenadon.wienerlinienalarm.exceptions.NetworkServerException;
import aenadon.wienerlinienalarm.models.wl_metadata.Line;
import aenadon.wienerlinienalarm.models.wl_metadata.Station;
import aenadon.wienerlinienalarm.models.wl_metadata.Steig;
import aenadon.wienerlinienalarm.update.csv_header.HaltestellenHeader;
import aenadon.wienerlinienalarm.update.csv_header.LinienHeader;
import aenadon.wienerlinienalarm.update.csv_header.SteigHeader;
import aenadon.wienerlinienalarm.utils.ApiProvider;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;

public class UpdateDatasetTask extends AsyncTask<Void, Void, NetworkStatus> {

    private Realm realm;
    private WeakReference<Context> weakCtx;

    private final ApiProvider.CSVApi csvApi;
    private final CSVFormat baseCsvFormat;
    private CheckForUpdateHelper checkForUpdateHelper;

    public UpdateDatasetTask(Context ctx) {
        csvApi = ApiProvider.getCSVApi();
        baseCsvFormat = CSVFormat.newFormat(';').withQuote('"').withTrim().withSkipHeaderRecord();
        checkForUpdateHelper = new CheckForUpdateHelper(ctx);

        this.weakCtx = new WeakReference<>(ctx);
    }

    @Override
    @DebugLog
    protected NetworkStatus doInBackground(Void... params) {
        try {
            boolean datasetUnchanged = !checkForUpdateHelper.datasetChanged();
            if (datasetUnchanged) {
                return NetworkStatus.SUCCESS_NO_UPDATE;
            }

            putStationPickerMessage(R.string.dataset_updating);

            String haltestellenCSV = call(csvApi.getHaltestellenCSV()).body();
            String steigCSV = call(csvApi.getSteigeCSV()).body();
            String lineCSV = call(csvApi.getLinienCSV()).body();

            realm = Realm.getDefaultInstance(); // need to create instance in the same thread
            processLineData(lineCSV);
            processSteigData(steigCSV);
            processStationData(haltestellenCSV);

            return NetworkStatus.SUCCESS;
        } catch (NetworkClientException e) {
            Log.e("ClientError while retrieving Metadata CSV", e);
            return NetworkStatus.NO_CONNECTION;
        } catch (NetworkServerException e) {
            Log.e("ServerError while retrieving Metadata CSV", e);
            return NetworkStatus.ERROR_SERVER;
        } catch (IOException e) {
            Log.e("Error while reading/parsing CSV", e);
            throw new RuntimeException(e);
        } finally {
            if (realm != null && !realm.isClosed()) {
                realm.close();
            }
        }
    }

    @Override
    protected void onPostExecute(NetworkStatus resultCode) {
        switch (resultCode) {
            case ERROR_SERVER:
                putStationPickerMessage(R.string.no_connection_old_dataset);
                break;
            case NO_CONNECTION:
                putStationPickerMessage(R.string.no_internet_old_dataset);
                break;
            case SUCCESS:
                Context ctx = weakCtx.get();
                if (ctx != null) {
                    Intent datasetUpdatedIntent = new Intent(Keys.Intent.DATASET_UPDATED);
                    ctx.sendBroadcast(datasetUpdatedIntent);
                }
                // fallthrough intended
            case SUCCESS_NO_UPDATE:
                deleteStationPickerMessage();
                break;
        }
    }

    private void putStationPickerMessage(int messageCode) {
        Context ctx = weakCtx.get();
        if (ctx != null) {
            DatasetUpdateStatus.putSnackbarMessage(ctx, ctx.getString(messageCode));
        }
    }

    private void deleteStationPickerMessage() {
        Context ctx = weakCtx.get();
        if (ctx != null) {
            DatasetUpdateStatus.deleteSnackbarMessage(ctx);
        }
    }

    private Response<String> call(Call<String> networkCall) throws NetworkClientException, NetworkServerException {
        Response<String> response;
        try {
            response = networkCall.execute();
            if (!response.isSuccessful()) {
                throw new NetworkServerException(response.code());
            }
        } catch (IOException e) {
            throw new NetworkClientException(e.getMessage(), e);
        }
        return response;
    }

    private void processLineData(String csv) throws IOException {
        Iterable<CSVRecord> lines = getCSVIterator(LinienHeader.class, csv);

        realm.beginTransaction();
        for (CSVRecord lineRecord : lines) {
            String id = lineRecord.get(LinienHeader.LINIEN_ID);
            String lineName = lineRecord.get(LinienHeader.BEZEICHNUNG);
            int sortOrder = Integer.parseInt(lineRecord.get(LinienHeader.REIHENFOLGE));
            boolean realtimeEnabled = "1".equals(lineRecord.get(LinienHeader.ECHTZEIT));
            TransportType type = TransportType.findByTypeString(lineRecord.get(LinienHeader.VERKEHRSMITTEL));

            Line line = getLineObject(id);
            line.setLineName(lineName);
            line.setLineSortOrder(sortOrder);
            line.setRealtimeEnabled(realtimeEnabled);
            line.setTransportType(type);

            realm.copyToRealm(line);
        }
        realm.commitTransaction();
    }

    private void processSteigData(String csv) throws IOException {
        Iterable<CSVRecord> steigs = getCSVIterator(SteigHeader.class, csv);

        realm.beginTransaction();
        for (CSVRecord steigRecord : steigs) {
            String id = steigRecord.get(SteigHeader.STEIG_ID);
            String lineId = steigRecord.get(SteigHeader.FK_LINIEN_ID);
            String stationId = steigRecord.get(SteigHeader.FK_HALTESTELLEN_ID);
            Direction direction = Direction.valueOf(steigRecord.get(SteigHeader.RICHTUNG));
            String rbl = steigRecord.get(SteigHeader.RBL_NUMMER);

            if ("".equals(rbl)) {
                continue;
            }

            Line steigLine = realm.where(Line.class)
                    .equalTo("id", lineId)
                    .findFirst();

            if (steigLine == null) {
                Log.w("Line with ID " + lineId + " not found. Skipping steig with ID " + id);
                continue;
            }

            Steig steig = getSteigObject(id);
            steig.setRbl(rbl);
            steig.setLine(steigLine);
            steig.setStationId(stationId);
            steig.setDirection(direction);

            realm.copyToRealm(steig);
        }
        realm.commitTransaction();
    }

    private void processStationData(String csv) throws IOException {
        Iterable<CSVRecord> stations = getCSVIterator(HaltestellenHeader.class, csv);

        realm.beginTransaction();
        for (CSVRecord stationRecord : stations) {
            String id = stationRecord.get(HaltestellenHeader.HALTESTELLEN_ID);
            String idForXMLApi = stationRecord.get(HaltestellenHeader.DIVA);
            String stationName = stationRecord.get(HaltestellenHeader.NAME);
            String city = stationRecord.get(HaltestellenHeader.GEMEINDE);

            List<Steig> steigsForStation = realm.where(Steig.class)
                    .equalTo("stationId", id)
                    .findAll();

            if (steigsForStation.isEmpty()) {
                continue;
            }

            Station station = getStationObject(id);
            station.setIdForXMLApi(idForXMLApi);
            station.setName(stationName);
            station.setCity(city);
            station.setSteigs(steigsForStation);

            realm.copyToRealm(station);
        }
        realm.commitTransaction();
    }

    private Iterable<CSVRecord> getCSVIterator(Class<? extends Enum<?>> header, String csv) throws IOException {
        return baseCsvFormat.withHeader(header).parse(new BufferedReader(new StringReader(csv)));
    }

    private Line getLineObject(String id) {
        Line line = realm.where(Line.class).equalTo("id", id).findFirst();
        if (line == null) {
            line = new Line();
            line.setId(id);
        }
        return line;
    }

    private Steig getSteigObject(String id) {
        Steig steig = realm.where(Steig.class).equalTo("id", id).findFirst();
        if (steig == null) {
            steig = new Steig();
            steig.setId(id);
        }
        return steig;
    }

    private Station getStationObject(String id) {
        Station station = realm.where(Station.class).equalTo("id", id).findFirst();
        if (station == null) {
            station = new Station();
            station.setId(id);
        }
        return station;
    }

}