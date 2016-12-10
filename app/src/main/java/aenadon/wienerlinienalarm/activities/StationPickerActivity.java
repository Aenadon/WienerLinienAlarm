package aenadon.wienerlinienalarm.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.adapter.Halteobjekt;
import aenadon.wienerlinienalarm.adapter.StationListAdapter;
import aenadon.wienerlinienalarm.utils.CSVWorkUtils;
import aenadon.wienerlinienalarm.utils.Const;

public class StationPickerActivity extends AppCompatActivity {

    List<Halteobjekt> stationsDisplay = new ArrayList<>();
    List<Halteobjekt> stationsOriginal = new ArrayList<>();
    ListView list;
    StationListAdapter sa;
    ProgressDialog warten;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_picker);

        // The waiting dialog to be shown whenever the user should wait
        warten = new ProgressDialog(StationPickerActivity.this);
        warten.setMessage(getString(R.string.please_wait));
        warten.setIndeterminate(true);
        warten.setCancelable(false);

        list = (ListView) findViewById (R.id.station_resultlist); // find the list view
        list.setOnItemClickListener(listListener()); // listen for presses

        EditText queryBox = (EditText) findViewById(R.id.station_search_edittext); // initialize the search box
        queryBox.addTextChangedListener(editTextChangeListener()); // listen for text changes

        String wholeCSV = CSVWorkUtils.getCSVfromFile(StationPickerActivity.this);
        if (wholeCSV != null) {
            populateListView(wholeCSV.split(Const.CSV_FILE_SEPARATOR)[Const.CSV_PART_STATION]);
        }
    }

    private void populateListView(String csv) {
        warten.show();
        String[] rows = csv.split("\n"); // get each row (=station) as separate array entry
        for (String row : rows) {
            String[] columns = row.split(";"); // get each column for each row as array entry
            String rawStationName = columns[3]; // "Station"
            String stationId = columns[0]; // 123456789
            stationsOriginal.add(new Halteobjekt( // create an Halteobjekt for every station entry
                    rawStationName.substring(1, rawStationName.length() - 1), // "Station" --> Station
                    stationId,
                    null // we don't need array index for the stations
            ));
        }
        Collections.sort(stationsOriginal);
        stationsDisplay.addAll(stationsOriginal); // copy the complete list to another variable so the original list remains unaltered
        Collections.sort(stationsDisplay);
        sa = new StationListAdapter(StationPickerActivity.this, stationsDisplay); // give our displaylist to the adapter
        list.setAdapter(sa); // set the adapter on the list (==> updates the list automatically)
        warten.dismiss();
    }

    // this listens for text changes
    private TextWatcher editTextChangeListener() {
        return new TextWatcher() { // and tell it what it has to do when the user types in something
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { // when the user changed the text inside (==> typed something)
                stationsDisplay.clear(); // clear the displayed list
                if (s.length() > 0) { // if the contents of the box are > 0
                    for (Halteobjekt h : stationsOriginal) { // loop through every object in the (original!) list of all stations
                        if (h.getName().toLowerCase().contains(s.toString().toLowerCase().trim())){ // and pick the matches
                            stationsDisplay.add(h); // and add them to the displayed list
                        }
                    }
                    sa.notifyDataSetChanged(); // tell the adapter that the list has changed
                } else {
                    stationsDisplay.addAll(stationsOriginal); // if user deleted everything, restore the original list
                    sa.notifyDataSetChanged(); // tell the adapter that the list has changed
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {/* not needed */}
            @Override public void afterTextChanged(Editable s) {/* not needed */}
        };
    }

    private AdapterView.OnItemClickListener listListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(parent
                        .getWindowToken(), 0);

                String stationName; String stationId;
                stationName = stationsDisplay.get(position).getName();
                stationId = stationsDisplay.get(position).getId(); // pass name and id to the SteigPickerActivity so he can offer the steigs

                Intent i = new Intent(getApplicationContext(), SteigPickerActivity.class);
                i.putExtra(Const.EXTRA_STATION_NAME, stationName).putExtra(Const.EXTRA_STATION_ID, stationId);
                startActivityForResult(i, 0);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }
}
