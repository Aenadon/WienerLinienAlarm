package aenadon.wienerlinienalarm.update;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
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
import aenadon.wienerlinienalarm.utils.AlertDialogs;
import aenadon.wienerlinienalarm.utils.ApiProvider;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;

public class UpdateDatasetService extends AsyncTask<Void, Void, NetworkStatus> {

    private static final String LOG_TAG = UpdateDatasetService.class.getSimpleName();

    private final Context ctx;
    private Realm realm;

    private final ApiProvider.CSVApi csvApi;
    private final CSVFormat baseCsvFormat;
    private CheckForUpdateService checkForUpdateService;

    public UpdateDatasetService(Context c) {
        ctx = c;

        csvApi = ApiProvider.getCSVApi();
        baseCsvFormat = CSVFormat.newFormat(';').withQuote('"').withTrim().withSkipHeaderRecord();
        checkForUpdateService = new CheckForUpdateService(ctx);
    }

    @Override
    protected NetworkStatus doInBackground(Void... params) {
        // TODO add loading screen activity for first launch + do in background from then on
        try {
            boolean datasetUnchanged = !checkForUpdateService.datasetChanged();
            if (datasetUnchanged) {
                return NetworkStatus.SUCCESS;
            }
            String haltestellenCSV = call(csvApi.getHaltestellenCSV()).body();
            String steigCSV = call(csvApi.getSteigeCSV()).body();
            String lineCSV = call(csvApi.getLinienCSV()).body();

            realm = Realm.getDefaultInstance(); // need to create instance in the same thread
            processLineData(lineCSV);
            processSteigData(steigCSV);
            processStationData(haltestellenCSV);

            return NetworkStatus.SUCCESS;
        } catch (NetworkClientException e) {
            Log.e(LOG_TAG, "ClientError while retrieving Metadata CSV", e);
            return NetworkStatus.NO_CONNECTION;
        } catch (NetworkServerException e) {
            Log.e(LOG_TAG, "ServerError while retrieving Metadata CSV", e);
            return NetworkStatus.ERROR_SERVER;
        } catch (IOException e) {
            Log.wtf(LOG_TAG, "Error while reading/parsing CSV", e);
            throw new RuntimeException(e);
        } finally {
            if (realm != null) {
                realm.close();
            }
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

            Line line = new Line();
            line.setId(id);
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
                Log.d(LOG_TAG, "No RBL for steig with ID " + id + ", skipping");
                continue;
            }

            Line steigLine = realm.where(Line.class)
                    .equalTo("id", lineId)
                    .findFirst();

            if (steigLine == null) {
                Log.w(LOG_TAG, "Line with ID " + lineId + " not found. Skipping steig with ID " + id);
                continue;
            }

            Steig steig = new Steig();
            steig.setId(id);
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

            Station station = new Station();
            station.setId(id);
            station.setIdForXMLApi(idForXMLApi);
            station.setName(stationName);
            station.setCity(city);
            station.setSteigs(steigsForStation);

            realm.copyToRealm(station);
        }
        realm.commitTransaction();
    }

    private Iterable<CSVRecord> getCSVIterator(Class<? extends Enum<?>> header, String csv) throws IOException {
        return baseCsvFormat.withHeader(header).parse(new StringReader(csv));
    }

    @Override
    protected void onPostExecute(NetworkStatus resultCode) {
        switch (resultCode) {
            case ERROR_SERVER:
                AlertDialogs.serverNotAvailable(ctx);
                break;
            case NO_CONNECTION:
                AlertDialogs.noConnection(ctx);
                break;
        }
    }
}