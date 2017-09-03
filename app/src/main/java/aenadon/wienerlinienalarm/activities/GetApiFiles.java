package aenadon.wienerlinienalarm.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.enums.NetworkStatus;
import aenadon.wienerlinienalarm.utils.AlertDialogs;
import aenadon.wienerlinienalarm.utils.CSVWorkUtils;
import aenadon.wienerlinienalarm.utils.Const;
import aenadon.wienerlinienalarm.utils.ApiProvider;
import okhttp3.ResponseBody;
import retrofit2.Response;

class GetApiFiles extends AsyncTask<Void, Void, NetworkStatus> {

    private final ProgressDialog loadingDialog;
    private final Context ctx;

    GetApiFiles(Context c) {
        ctx = c;
        loadingDialog = new ProgressDialog(ctx);
        loadingDialog.setCancelable(false);
        loadingDialog.setIndeterminate(true);
        loadingDialog.setMessage(ctx.getString(R.string.updating_stations));
    }

    @Override
    protected NetworkStatus doInBackground(Void... params) {
        try {
            File csv = new File(ctx.getFilesDir(), Const.CSV_FILENAME);
            String csvString = CSVWorkUtils.getCSVfromFile(ctx);
            if (!csv.exists() || csvString == null) {
                publishProgress(); // make the progress dialog appear
            }

            // check file version
            Response<ResponseBody> versionResponse = ApiProvider.getCSVApi().getVersionCSV().execute();
            String versionResponseString = versionResponse.body().string();

            if (!versionResponse.isSuccessful()) {
                return NetworkStatus.ERROR_SERVER;
            }

            if (csv.exists() && csvString != null) {
                String x = csvString.split(Const.CSV_FILE_SEPARATOR)[Const.CSV_PART_VERSION];
                if (x.equals(versionResponseString))
                    return NetworkStatus.SUCCESS;
            }

            Response<ResponseBody> haltestellenResponse = ApiProvider.getCSVApi().getHaltestellenCSV().execute();
            Response<ResponseBody> steigResponse = ApiProvider.getCSVApi().getSteigeCSV().execute();

            if (!haltestellenResponse.isSuccessful() || !steigResponse.isSuccessful()) {
                throw new IOException("At least one server response not successful " +
                        "(" + haltestellenResponse.code() + "/" + steigResponse.code() + ")"); // [...] (403/403)
            } else {
                if (csv.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    csv.delete();
                }
                String haltestellenCSV = haltestellenResponse.body().string();
                String steigCSV = steigResponse.body().string();

                List<String> haltestellenList = new ArrayList<>(Arrays.asList(haltestellenCSV.split("\n")));
                List<String> steigList = new ArrayList<>(Arrays.asList(steigCSV.split("\n")));

                for (int i = steigList.size() - 1; i >= 0; i--) { // backwards because removing elements alters the indexes
                    String[] columns = steigList.get(i).split(";");
                    if (columns[5].equals("\"\"")) { // columns[5] == RBL_NUMMER -> if no RBL_NUMMER, then remove it
                        steigList.remove(i);
                    }
                }

                steigCSV = TextUtils.join("\n", steigList.toArray());

                for (int i = haltestellenList.size() - 1; i >= 0; i--) {
                    String[] columns = haltestellenList.get(i).split(";");
                    if (!steigCSV.contains(columns[0])) {
                        haltestellenList.remove(i);
                    }
                }

                haltestellenCSV = TextUtils.join("\n", haltestellenList.toArray());

                String combined =
                        versionResponseString      // last update date
                                + Const.CSV_FILE_SEPARATOR +
                                haltestellenCSV
                                + Const.CSV_FILE_SEPARATOR +
                                steigCSV;

                FileOutputStream fos = new FileOutputStream(csv);
                fos.write(combined.getBytes());
                fos.close();
                return NetworkStatus.SUCCESS;
            }

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return NetworkStatus.NO_CONNECTION;
        }
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