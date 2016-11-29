package aenadon.wienerlinienalarm.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.utils.AlertDialogs;
import aenadon.wienerlinienalarm.utils.CSVWorkUtils;
import aenadon.wienerlinienalarm.utils.Const;
import aenadon.wienerlinienalarm.utils.RetrofitInfo;
import okhttp3.ResponseBody;
import retrofit2.Response;

class GetApiFiles extends AsyncTask<Void, Void, Boolean> {

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
    protected Boolean doInBackground(Void... params) {
        try {
            File csv = new File(ctx.getFilesDir(), Const.CSV_FILENAME);
            String csvString = CSVWorkUtils.getCSVfromFile(ctx);
            if (!csv.exists() || csvString == null) {
                publishProgress(); // make the progress dialog appear
            }

            // check file version
            Response<ResponseBody> versionResponse = RetrofitInfo.getCSVInfo().create(RetrofitInfo.CSVCalls.class).getVersionCSV().execute();
            String versionResponseString = versionResponse.body().string();

            if (!versionResponse.isSuccessful()) return false;
            if (csv.exists() && csvString != null) {
                String x = csvString.split(Const.CSV_FILE_SEPARATOR)[Const.CSV_PART_VERSION];
                if (x.equals(versionResponseString))
                    return true; // if we already have the latest version, skip the redownload
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
                String combined =
                        versionResponseString      // last update date
                                + Const.CSV_FILE_SEPARATOR +             // separator
                                haltestellenResponse.body().string() // haltestellen CSV
                                + Const.CSV_FILE_SEPARATOR +             // separator
                                steigResponse.body().string();       // steige CSV

                FileOutputStream fos = new FileOutputStream(csv);
                fos.write(combined.getBytes());
                fos.close();
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        warten.show();
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (warten != null) warten.dismiss();

        if (!success) {
            AlertDialogs.serverNotAvailable(ctx);
            ((Activity)ctx).findViewById(R.id.choose_station_button).setEnabled(false); // disable station picker
        }
    }
}
