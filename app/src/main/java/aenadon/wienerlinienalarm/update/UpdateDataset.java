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
import aenadon.wienerlinienalarm.enums.NetworkStatus;
import aenadon.wienerlinienalarm.exceptions.NetworkClientException;
import aenadon.wienerlinienalarm.exceptions.NetworkServerException;
import aenadon.wienerlinienalarm.utils.AlertDialogs;
import aenadon.wienerlinienalarm.utils.ApiProvider;
import retrofit2.Call;
import retrofit2.Response;

public class UpdateDataset extends AsyncTask<Void, Void, NetworkStatus> {

    private static final String LOG_TAG = UpdateDataset.class.getSimpleName();

    private final ProgressDialog loadingDialog;
    private final Context ctx;
    private ApiProvider.CSVApi csvApi;

    private CSVService csvService;

    public UpdateDataset(Context c) {
        ctx = c;
        loadingDialog = new ProgressDialog(ctx);
        loadingDialog.setCancelable(false);
        loadingDialog.setIndeterminate(true);
        loadingDialog.setMessage(ctx.getString(R.string.updating_stations));
        csvApi = ApiProvider.getCSVApi();

        csvService = new CSVService(ctx);
    }

    @Override
    protected NetworkStatus doInBackground(Void... params) {
        try {

            boolean datasetChanged = csvService.checkForChangedDataset();


            Response<String> haltestellenResponse = call(csvApi.getHaltestellenCSV());
            Response<String> steigResponse = call(csvApi.getSteigeCSV());
            Response<String> lineResponse = call(csvApi.getLinienCSV());

            String haltestellenCSV = haltestellenResponse.body();
            String steigCSV = steigResponse.body();
            String lineCSV = lineResponse.body();

            List<String> haltestellenList = new ArrayList<>(Arrays.asList(haltestellenCSV.split("\n")));
            List<String> steigList = new ArrayList<>(Arrays.asList(steigCSV.split("\n")));
            List<String> lineList = new ArrayList<>(Arrays.asList(lineCSV.split("\n")));

            // TODO store results

            return NetworkStatus.SUCCESS;


        } catch (NetworkClientException e) {
            Log.e(LOG_TAG, "ClientError while retrieving Metadata CSV", e);
            return NetworkStatus.NO_CONNECTION;
        } catch (NetworkServerException e) {
            Log.e(LOG_TAG, "ServerError while retrieving Metadata CSV", e);
            return NetworkStatus.ERROR_SERVER;
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