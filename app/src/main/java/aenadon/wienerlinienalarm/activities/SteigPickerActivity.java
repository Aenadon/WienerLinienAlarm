package aenadon.wienerlinienalarm.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import aenadon.wienerlinienalarm.adapter.SteigListAdapter;
import aenadon.wienerlinienalarm.adapter.SteigWithLineName;
import aenadon.wienerlinienalarm.models.wl_metadata.Station;
import aenadon.wienerlinienalarm.models.wl_metadata.Steig;
import aenadon.wienerlinienalarm.utils.Keys;
import io.realm.Realm;
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

    private static final List<SteigWithLineName> steigDisplay = new ArrayList<>();
    private Station selectedStation;
    private ListView list;
    private ProgressDialog warten;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steig_picker);

        warten = new ProgressDialog(SteigPickerActivity.this);
        warten.setMessage(getString(R.string.please_wait));
        warten.setIndeterminate(true);
        warten.setCancelable(false);

//        warten.show();

        Bundle b = getIntent().getExtras();
        String stationId = b.getString(Keys.Extra.SELECTED_STATION_ID);

        Realm realm = Realm.getDefaultInstance();
        selectedStation = realm.where(Station.class).equalTo("id", stationId).findFirst();
        realm.close();

        TextView stationDisplay = (TextView) findViewById(R.id.steig_stationdisplay);
        stationDisplay.setText(selectedStation.getName());

        list = (ListView) findViewById(R.id.steig_resultlist);
        list.setOnItemClickListener(listListener());

        populateListView();
    }

    private void populateListView() {
        List<Steig> steigs = selectedStation.getSteigs();
        for (Steig steig : steigs) {
            SteigWithLineName steigWithLineName = new SteigWithLineName();
            steigWithLineName.setSteig(steig);
            steigWithLineName.setLineNameAndDirection("U6 Siebenhirten"); // TODO replace with retrieved name
            steigDisplay.add(steigWithLineName);
        }

        SteigListAdapter sa = new SteigListAdapter(SteigPickerActivity.this, steigDisplay);
        list.setAdapter(sa);
    }

    private AdapterView.OnItemClickListener listListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                SteigWithLineName steigWithLineName = steigDisplay.get(position);

                Intent extraData = new Intent()
                        .putExtra(Keys.Extra.LINE_NAME_AND_DIRECTION, steigWithLineName.getLineNameAndDirection())
                        .putExtra(Keys.Extra.SELECTED_STEIG_ID, steigWithLineName.getSteigId());
                setResult(Activity.RESULT_OK, extraData);
                finish();
            }
        };
    }

    /*private class GetSteigNames extends AsyncTask<List<String>, Void, Integer> {

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
    }*/
}
