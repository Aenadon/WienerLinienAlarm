package aenadon.wienerlinienalarm.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import aenadon.wienerlinienalarm.adapter.SteigListAdapter;
import aenadon.wienerlinienalarm.adapter.SteigWithDestination;
import aenadon.wienerlinienalarm.enums.Direction;
import aenadon.wienerlinienalarm.enums.TransportType;
import aenadon.wienerlinienalarm.models.routing_xml.XmlSteig;
import aenadon.wienerlinienalarm.models.routing_xml.xml_model.RoutingXMLRequest;
import aenadon.wienerlinienalarm.models.wl_metadata.Line;
import aenadon.wienerlinienalarm.models.wl_metadata.Station;
import aenadon.wienerlinienalarm.models.wl_metadata.Steig;
import aenadon.wienerlinienalarm.utils.Keys;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.utils.ApiProvider;
import retrofit2.Response;
import trikita.log.Log;

public class SteigPickerActivity extends AppCompatActivity {

    private static final List<SteigWithDestination> steigsOnDisplay = new ArrayList<>();
    private Station selectedStation;
    private ListView list;
    SteigListAdapter steigListAdapter;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steig_picker);
        realm = Realm.getDefaultInstance();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String stationId = getIntent().getStringExtra(Keys.Extra.SELECTED_STATION_ID);

        selectedStation = realm.where(Station.class).equalTo("id", stationId).findFirst();

        TextView stationDisplay = findViewById(R.id.steig_stationdisplay);
        stationDisplay.setText(selectedStation.getName());

        list = findViewById(R.id.steig_resultlist);
        list.setOnItemClickListener(listListener());

        @SuppressLint("InflateParams") View emptyView = getLayoutInflater().inflate(R.layout.list_loading_placeholder, null);
        ((ViewGroup)list.getParent()).addView(emptyView);
        list.setEmptyView(emptyView);

        steigListAdapter = new SteigListAdapter(SteigPickerActivity.this, steigsOnDisplay);
        populateListView();
    }

    private void populateListView() {
        steigsOnDisplay.clear();
        List<Steig> steigs = selectedStation.getSteigs();
        for (Steig steig : steigs) {
            SteigWithDestination steigWithDestination = new SteigWithDestination();
            steigWithDestination.setSteig(steig);
            steigsOnDisplay.add(steigWithDestination);
        }

        ApiProvider.getRoutingApi().getXMLStationInfo(selectedStation.getIdForXMLApi()).enqueue(new Callback<RoutingXMLRequest>() {
            @Override
            public void onResponse(@NonNull Call<RoutingXMLRequest> call, @NonNull Response<RoutingXMLRequest> response) {
                if (!response.isSuccessful()) {
                    replaceLoadingSpinnerWithError(R.string.server_broken_text);
                    Log.e("XML Response error (Error code " + response.code() + ")",
                            "Server message: " + response.message());
                    return;
                }

                List<XmlSteig> xmlSteigs = XmlSteig.getStationLines(response.body());

                if (xmlSteigs.isEmpty()) {
                    replaceLoadingSpinnerWithError(R.string.no_steigs_found);
                    Log.w("No XML lines found for station with ID " + selectedStation.getId() + " / DIVA " + selectedStation.getIdForXMLApi());
                    return;
                }

                List<SteigWithDestination> clonedSteigDisplay = new ArrayList<>(steigsOnDisplay);

                for (SteigWithDestination steigWithDestination : clonedSteigDisplay) {
                    Steig steig = steigWithDestination.getSteig();
                    String lineNameAndDirection = steigDestinationName(steig, xmlSteigs);
                    if (lineNameAndDirection == null) {
                        steigsOnDisplay.remove(steigWithDestination); // same references in both arrays
                        continue;
                    }
                    steigWithDestination.setLineNameAndDirection(lineNameAndDirection);
                }
                Collections.sort(steigsOnDisplay);
                list.setAdapter(steigListAdapter);
            }

            private void replaceLoadingSpinnerWithError(int errorStringCode) {
                ListView list = findViewById(R.id.steig_resultlist);
                list.setVisibility(View.GONE);

                TextView errorMessage = findViewById(R.id.steig_list_error);
                errorMessage.setText(errorStringCode);
                errorMessage.setVisibility(View.VISIBLE);
            }

            private String steigDestinationName(Steig steig, List<XmlSteig> xmlSteigs) {
                String destinationName = null;
                Line lineOfSteig = steig.getLine();

                if (TransportType.TRAM_WLB.equals(lineOfSteig.getTransportType())) {
                    String line = lineOfSteig.getLineName();
                    String destination = getWLBDestination(steig);
                    destinationName = line + " " + destination;
                } else {
                    for (XmlSteig xmlSteig : xmlSteigs) {
                        if (steigEqualsXmlSteig(steig, xmlSteig)) {
                            destinationName = xmlSteig.toString();
                            break;
                        }
                    }
                }
                return destinationName;
            }

            // Needs to be hardcoded because the XML API doesn't return those
            private String getWLBDestination(Steig steig) {
                Direction direction = steig.getDirection();
                if (Direction.H.equals(direction)) {
                    return "Baden Josefspl.";
                } else {
                    return "Wien Oper";
                }
            }

            private boolean steigEqualsXmlSteig(Steig steig, XmlSteig xmlSteig) {
                return steig.getLine().getLineName().equals(xmlSteig.getLine()) &&
                        steig.getDirection().toString().equals(xmlSteig.getDirection());
            }

            @Override
            public void onFailure(@NonNull Call<RoutingXMLRequest> call, @NonNull Throwable t) {
                replaceLoadingSpinnerWithError(R.string.no_connection_text);
                Log.e("Client error while retrieving XML Response", t);
            }
        });
    }

    private AdapterView.OnItemClickListener listListener() {
        return (parent, view, position, id) -> {
            SteigWithDestination steigWithDestination = steigsOnDisplay.get(position);

            Intent extraData = new Intent()
                    .putExtra(Keys.Extra.LINE_NAME_AND_DIRECTION, steigWithDestination.getLineNameAndDirection())
                    .putExtra(Keys.Extra.SELECTED_STATION_NAME, selectedStation.getName())
                    .putExtra(Keys.Extra.SELECTED_STEIG_ID, steigWithDestination.getSteigId());
            setResult(Activity.RESULT_OK, extraData);
            finish();
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}
