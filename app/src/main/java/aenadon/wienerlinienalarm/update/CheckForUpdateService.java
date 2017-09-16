package aenadon.wienerlinienalarm.update;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import aenadon.wienerlinienalarm.exceptions.NetworkClientException;
import aenadon.wienerlinienalarm.exceptions.NetworkServerException;
import aenadon.wienerlinienalarm.utils.ApiProvider;
import retrofit2.Response;

class CheckForUpdateService {

    private static final String PREF_NAME_CSV_LAST_UPDATED = "CSV_LAST_UPDATED";
    private static final String PREF_KEY_CSV_LAST_UPDATED = "lastUpdated";

    private Context ctx;
    private ApiProvider.CSVApi csvApi;

    CheckForUpdateService(Context ctx) {
        this.ctx = ctx;

        csvApi = ApiProvider.getCSVApi();
    }

    boolean datasetChanged() throws NetworkClientException, NetworkServerException {
        try {
            Response<String> csvLastUpdatedCall = csvApi.getVersionCSV().execute();
            if (!csvLastUpdatedCall.isSuccessful()) {
                throw new NetworkServerException(csvLastUpdatedCall.code());
            }
            return lastUpdatedHasChanged(csvLastUpdatedCall.body());
        } catch (IOException e) {
            throw new NetworkClientException(e.getMessage(), e);
        }
    }

    private boolean lastUpdatedHasChanged(String newLastUpdatedCsv) {
        SharedPreferences prefs = getCsvLastUpdatedPrefs();
        String oldLastUpdatedCsv = prefs.getString(PREF_KEY_CSV_LAST_UPDATED, "");
        boolean hasChanged = !oldLastUpdatedCsv.equals(newLastUpdatedCsv);
        if (hasChanged) {
            setCsvLastUpdated(newLastUpdatedCsv);
        }
        return hasChanged;
    }

    private void setCsvLastUpdated(String lastUpdated) {
        SharedPreferences.Editor editor = getCsvLastUpdatedPrefs().edit();
        editor.putString(PREF_KEY_CSV_LAST_UPDATED, lastUpdated);
        editor.apply();
    }

    private SharedPreferences getCsvLastUpdatedPrefs() {
        return ctx.getSharedPreferences(PREF_NAME_CSV_LAST_UPDATED, Context.MODE_PRIVATE);
    }



}
