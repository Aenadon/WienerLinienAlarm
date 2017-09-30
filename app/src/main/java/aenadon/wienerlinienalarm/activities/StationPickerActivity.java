package aenadon.wienerlinienalarm.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.adapter.StationListAdapter;
import aenadon.wienerlinienalarm.models.wl_metadata.Station;
import aenadon.wienerlinienalarm.utils.Keys;
import hugo.weaving.DebugLog;
import io.realm.Realm;

public class StationPickerActivity extends AppCompatActivity {

    private final List<Station> stationsToDisplay = new ArrayList<>();
    private final List<Station> stationsCompleteList = new ArrayList<>();
    private StationListAdapter stationAdapter;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_picker);
        realm = Realm.getDefaultInstance();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        stationsCompleteList.addAll(realm.where(Station.class).findAllSorted("name"));
        stationsToDisplay.addAll(stationsCompleteList);

        ListView list = (ListView) findViewById(R.id.station_resultlist);
        list.setOnItemClickListener(new StationListListener());

        stationAdapter = new StationListAdapter(StationPickerActivity.this, stationsToDisplay);
        list.setAdapter(stationAdapter);

        EditText queryBox = (EditText) findViewById(R.id.station_search_edittext);
        queryBox.addTextChangedListener(new StationSearchTextWatcher());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Keys.RequestCode.SELECT_STEIG) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK, data);
                finish();
            } else if (resultCode == Keys.ResultCode.NO_STEIGS_AVAILABLE) {
                Snackbar.make(findViewById(android.R.id.content), R.string.no_steigs_found, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class StationSearchTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence inputText, int start, int before, int count) { }

        @Override
        @DebugLog
        public void afterTextChanged(Editable s) {
            stationsToDisplay.clear();

            String inputText = s.toString();
            if (inputText.length() > 0) {
                for (Station station : stationsCompleteList) {
                    if (stationNameContains(station, inputText)){
                        stationsToDisplay.add(station);
                    }
                }
            } else {
                stationsToDisplay.addAll(stationsCompleteList);
            }
            stationAdapter.notifyDataSetChanged();
        }

        private boolean stationNameContains(Station station, CharSequence inputText) {
            String stationName = station.getName().toLowerCase().trim();
            String input = inputText.toString().toLowerCase().trim();
            return stationName.contains(input);
        }
    }

    private class StationListListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(parent.getWindowToken(), 0);

            Intent selectSteigIntent = new Intent(StationPickerActivity.this, SteigPickerActivity.class);
            selectSteigIntent.putExtra(Keys.Extra.SELECTED_STATION_ID, stationsToDisplay.get(position).getId());
            startActivityForResult(selectSteigIntent, Keys.RequestCode.SELECT_STEIG);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}
