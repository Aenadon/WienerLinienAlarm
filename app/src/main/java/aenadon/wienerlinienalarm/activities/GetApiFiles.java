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
import aenadon.wienerlinienalarm.utils.AlertDialogs;
import aenadon.wienerlinienalarm.utils.CSVWorkUtils;
import aenadon.wienerlinienalarm.utils.Const;
import aenadon.wienerlinienalarm.utils.RetrofitInfo;
import okhttp3.ResponseBody;
import retrofit2.Response;

class GetApiFiles extends AsyncTask<Void, Void, Integer> {

    private ProgressDialog warten;
    private Context ctx;

    GetApiFiles(Context c) {
        ctx = c;
        warten = new ProgressDialog(ctx);
        warten.setCancelable(false);
        warten.setIndeterminate(true);
        warten.setMessage(ctx.getString(R.string.updating_stations));
    }

    @Override
    protected Integer doInBackground(Void... params) {
        try {
            File csv = new File(ctx.getFilesDir(), Const.CSV_FILENAME);
            String csvString = CSVWorkUtils.getCSVfromFile(ctx);
            if (!csv.exists() || csvString == null) {
                publishProgress(); // make the progress dialog appear
            }

            // check file version
            Response<ResponseBody> versionResponse = RetrofitInfo.getCSVInfo().create(RetrofitInfo.CSVCalls.class).getVersionCSV().execute();
            String versionResponseString = versionResponse.body().string();

            if (!versionResponse.isSuccessful()) {
                return Const.NETWORK_SERVER_ERROR;
            }

            if (csv.exists() && csvString != null) {
                String x = csvString.split(Const.CSV_FILE_SEPARATOR)[Const.CSV_PART_VERSION];
                if (x.equals(versionResponseString))
                    return Const.NETWORK_SUCCESS; // if we already have the latest version, we're done here
            }

            Response<ResponseBody> haltestellenResponse = RetrofitInfo.getCSVInfo().create(RetrofitInfo.CSVCalls.class).getHaltestellenCSV().execute();
            Response<ResponseBody> steigResponse = RetrofitInfo.getCSVInfo().create(RetrofitInfo.CSVCalls.class).getSteigeCSV().execute();

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

                List<String> items = new ArrayList<>(Arrays.asList(haltestellenCSV.split("\n")));

                for (int i = 0; i < items.size(); i++) {
                    String[] columns = items.get(i).split(";");
                    if (!steigCSV.contains(columns[0])) {
                        items.remove(i);
                    }
                }

                haltestellenCSV = TextUtils.join("\n", items.toArray());

                String combined =
                        versionResponseString      // last update date
                                + Const.CSV_FILE_SEPARATOR +
                                haltestellenCSV
                                + Const.CSV_FILE_SEPARATOR +
                                steigCSV;

                FileOutputStream fos = new FileOutputStream(csv);
                fos.write(combined.getBytes());
                fos.close();
                return Const.NETWORK_SUCCESS;
            }

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return Const.NETWORK_CONNECTION_ERROR;
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        warten.show();
    }

    @Override
    protected void onPostExecute(Integer resultCode) {
        if (warten != null) warten.dismiss();

        switch (resultCode) {
            case Const.NETWORK_SERVER_ERROR:
                AlertDialogs.serverNotAvailable(ctx);
                break;
            case Const.NETWORK_CONNECTION_ERROR:
                AlertDialogs.noConnection(ctx);
                break;
        }
    }
}