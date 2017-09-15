package aenadon.wienerlinienalarm.update;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.enums.Direction;
import aenadon.wienerlinienalarm.enums.NetworkStatus;
import aenadon.wienerlinienalarm.enums.TransportType;
import aenadon.wienerlinienalarm.exceptions.NetworkClientException;
import aenadon.wienerlinienalarm.exceptions.NetworkServerException;
import aenadon.wienerlinienalarm.models.wl_metadata.Line;
import aenadon.wienerlinienalarm.models.wl_metadata.Station;
import aenadon.wienerlinienalarm.models.wl_metadata.Steig;
import aenadon.wienerlinienalarm.utils.AlertDialogs;
import aenadon.wienerlinienalarm.utils.ApiProvider;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;

public class UpdateDatasetService extends AsyncTask<Void, Void, NetworkStatus> {

    private static final String LOG_TAG = UpdateDatasetService.class.getSimpleName();

    private final Context ctx;
    private final ProgressDialog loadingDialog;
    private Realm realm;

    private final ApiProvider.CSVApi csvApi;
    private CheckForUpdateService CheckForUpdateService;

    public UpdateDatasetService(Context c) {
        ctx = c;
        loadingDialog = new ProgressDialog(ctx);
        loadingDialog.setCancelable(false);
        loadingDialog.setIndeterminate(true);
        loadingDialog.setMessage(ctx.getString(R.string.updating_stations));

        csvApi = ApiProvider.getCSVApi();
        CheckForUpdateService = new CheckForUpdateService(ctx);
    }

    @Override
    protected NetworkStatus doInBackground(Void... params) {
        try {
            boolean datasetUnchanged = !CheckForUpdateService.datasetChanged();
            if (datasetUnchanged) {
                return NetworkStatus.SUCCESS;
            }

            Response<String> haltestellenResponse = call(csvApi.getHaltestellenCSV());
            Response<String> steigResponse = call(csvApi.getSteigeCSV());
            Response<String> lineResponse = call(csvApi.getLinienCSV());

            String haltestellenCSV = haltestellenResponse.body();
            String steigCSV = steigResponse.body();
            String lineCSV = lineResponse.body();

            List<String> lineList = new ArrayList<>(Arrays.asList(lineCSV.split("\n")));
            List<String> steigList = new ArrayList<>(Arrays.asList(steigCSV.split("\n")));
            List<String> haltestellenList = new ArrayList<>(Arrays.asList(haltestellenCSV.split("\n")));

            realm = Realm.getDefaultInstance(); // need to create instance in the same thread
            processLineData(lineList);
            processSteigData(steigList);
            processStationData(haltestellenList);

            return NetworkStatus.SUCCESS;
        } catch (NetworkClientException e) {
            Log.e(LOG_TAG, "ClientError while retrieving Metadata CSV", e);
            return NetworkStatus.NO_CONNECTION;
        } catch (NetworkServerException e) {
            Log.e(LOG_TAG, "ServerError while retrieving Metadata CSV", e);
            return NetworkStatus.ERROR_SERVER;
        }
    }

    private void processLineData(List<String> lineData) {
        realm.beginTransaction();
        for (int i = 1; i < lineData.size(); i++) { // i = 1 ==> skip CSV column headers
            String[] lineInfo = lineData.get(i).split(";");

            String id = lineInfo[0];
            String lineName = lineInfo[1];
            int sortOrder = Integer.parseInt(lineInfo[2]);
            boolean realtimeEnabled = "1".equals(lineInfo[3]);
            TransportType type = TransportType.findByTypeString(removeQuotes(lineInfo[4]));

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

    private void processSteigData(List<String> steigData) {
        realm.beginTransaction();
        for (int i = 1; i < steigData.size(); i++) { // i = 1 ==> skip CSV column headers
            String[] steigInfo = steigData.get(i).split(";");

            String id = steigInfo[0];
            String lineId = steigInfo[1];
            String stationId = steigInfo[2];
            Direction direction = Direction.valueOf(removeQuotes(steigInfo[3]));
            String rbl = removeQuotes(steigInfo[5]);

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

    private void processStationData(List<String> stationData) {
        realm.beginTransaction();
        for (int i = 1; i < stationData.size(); i++) { // i = 1 ==> skip CSV column headers
            String[] stationInfo = stationData.get(i).split(";");

            String id = stationInfo[0];
            String idForXMLApi = stationInfo[2];
            String stationName = stationInfo[3];
            String city = stationInfo[4];

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

    private String removeQuotes(String string) {
        return string.replace("\"", "");
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

    @Override
    protected void onPostExecute(NetworkStatus resultCode) {
        if (loadingDialog != null) loadingDialog.dismiss();

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