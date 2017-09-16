package aenadon.wienerlinienalarm.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import trikita.log.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import aenadon.wienerlinienalarm.BuildConfig;
import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.adapter.Halteobjekt;
import aenadon.wienerlinienalarm.adapter.StationListAdapter;
import aenadon.wienerlinienalarm.utils.AlertDialogs;
import aenadon.wienerlinienalarm.utils.CSVWorkUtils;
import aenadon.wienerlinienalarm.utils.Const;
import aenadon.wienerlinienalarm.utils.ApiProvider;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class SteigPickerActivity extends AppCompatActivity {

    private final List<String> steige = new ArrayList<>();
    private static final List<Halteobjekt> steigDisplay = new ArrayList<>();
    private ListView list;
    private String stationName;
    private String stationId;
    private ProgressDialog warten;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steig_picker);

        // The waiting dialog to be shown whenever the user should wait
        warten = new ProgressDialog(SteigPickerActivity.this);
        warten.setMessage(getString(R.string.please_wait));
        warten.setIndeterminate(true);
        warten.setCancelable(false);

        warten.show();

        Bundle b = getIntent().getExtras();
        stationName = b.getString(Const.EXTRA_STATION_NAME);
        stationId = b.getString(Const.EXTRA_STATION_ID);

        TextView stationDisplay = (TextView) findViewById(R.id.steig_stationdisplay);
        stationDisplay.setText(stationName); // display the selected station to the user

        list = (ListView) findViewById(R.id.steig_resultlist); // find the list view
        list.setOnItemClickListener(listListener()); // listen for presses

        String wholeCSV = CSVWorkUtils.getCSVfromFile(SteigPickerActivity.this);
        if (wholeCSV != null) {
            populateListView(wholeCSV.split(Const.CSV_FILE_SEPARATOR)[Const.CSV_PART_STEIG]);
        }

    }

    private void populateListView(String csv) {
        if (stationId == null) return;
        String[] rows = csv.split("\n"); // get each row (=station) as separate array entry
        for (int i = 1; i < rows.length; i++) { // first line is table header ==> i = 1 to skip it
            String[] columns = rows[i].split(";"); // get each column for each row as array entry
            if (columns[2].equals(stationId)) { // add only the steigs which correspond to the queried station
                // columns[5] == RBL_NUMMER (the only one required for API query)
                // to avoid double stations, only add the station if the ID doesn't already exist
                String id = columns[5].substring(1, columns[5].length() - 1);
                if (!steige.contains(id)) steige.add(id); // "1234" --> 1234
            }
        }
        //noinspection unchecked
        new GetSteigNames().execute(steige);
    }

    private AdapterView.OnItemClickListener listListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                String steigId = steigDisplay.get(position).getId();
                Halteobjekt h = steigDisplay.get(position);
                String stationDir = h.getName();
                Intent extraData = new Intent().putExtra(Const.EXTRA_STATION_INFO, new String[]{stationName, stationDir, steigId, h.getArrayIndex()});
                setResult(Activity.RESULT_OK, extraData);
                finish();
            }
        };
    }

    private class GetSteigNames extends AsyncTask<List<String>, Void, Integer> {

        List<Halteobjekt> halteobjekts;

        @SuppressWarnings("unchecked")
        @Override
        protected Integer doInBackground(List<String>... params) {
            String apikey = BuildConfig.API_KEY;
            List<String> steigs = params[0];
            steigDisplay.clear();
            for (String steigId : steigs) {
                try {
                    if (steigId.equals("")) continue; // or else we get an unexplainable 400
                    Response<ResponseBody> response = ApiProvider.getRealtimeApi().getRealtime(apikey, steigId).execute();
                    if (response.isSuccessful()) {
                        JSONArray monitors = new JSONObject(response.body().string())
                                .getJSONObject("data")
                                .getJSONArray("monitors");
                        if (monitors.length() < 1) {
                            continue;
                        }
                        for (int arrayIndex = 0; arrayIndex < monitors.length(); arrayIndex++) {
                            JSONObject lineDef = monitors.getJSONObject(arrayIndex)
                                    .getJSONArray("lines")
                                    .getJSONObject(0);


                            String lineName = lineDef.getString("name");
                            String lineDirection = (lineName.substring(0, 1).equals("U")) ?  // if it's a UBAHN get our hardcoded direction instead of HÜTTELDORF          * HÜTTELDORF         4
                                    CSVWorkUtils.getUbahnEndstation(lineName, lineDef.getString("direction")) : lineDef.getString("towards");

                            String lineAndDirName = lineName + " " + lineDirection;
                            steigDisplay.add(new Halteobjekt(lineAndDirName, steigId, Integer.toString(arrayIndex)));
                        }
                    } else {
                        Log.e("API response unsuccessful: " + response.code());
                        return Const.NETWORK_SERVER_ERROR;
                    }
                } catch (IOException | JSONException e) {
                    Log.e("API request/JSON fail", e);
                    return Const.NETWORK_CONNECTION_ERROR;
                }
            }
            Collections.sort(steigDisplay);
            halteobjekts = steigDisplay;
            return Const.NETWORK_SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            warten.dismiss();
            if (resultCode == Const.NETWORK_SERVER_ERROR) {
                AlertDialogs.serverNotAvailable(SteigPickerActivity.this);
            } else if (resultCode == Const.NETWORK_CONNECTION_ERROR) {
                AlertDialogs.noConnection(SteigPickerActivity.this);
            } else if (resultCode == Const.NETWORK_SUCCESS && halteobjekts.isEmpty()) {
                AlertDialogs.noSteigsAvailable(SteigPickerActivity.this);
            } else {
                StationListAdapter sa = new StationListAdapter(getApplicationContext(), halteobjekts);
                list.setAdapter(sa); // set the adapter on the list (==> updates the list automatically)
            }
        }
    }
}
