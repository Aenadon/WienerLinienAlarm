package aenadon.wienerlinienalarm.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.adapter.AlarmListAdapter;
import aenadon.wienerlinienalarm.models.Alarm;
import aenadon.wienerlinienalarm.utils.StringDisplay;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Realm.init(MainActivity.this); // for all database related stuff

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, AlarmSetterActivity.class), 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // mViewPager.setAdapter(mSectionsPagerAdapter); // maybe inefficient but unnoticeably slower and works!
            mSectionsPagerAdapter.notifyDataSetChanged();
            // tabLayout.getTabAt(data.getIntExtra("mode",0)).select(); // displays the tab which was opened before alarm creation
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickHandler(View view) {
        switch (view.getId()) {
            case R.id.date:
            case R.id.date_edit:

                break;
            case R.id.days:
            case R.id.days_edit:

                break;
            case R.id.time:
            case R.id.time_edit:

                break;
            case R.id.ringtone:
            case R.id.ringtone_edit:

                break;
            case R.id.vibration:
            case R.id.vibration_edit:

                break;
            case R.id.station:
            case R.id.station_edit:

                break;
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<AlarmMenuFragment> fragments = new ArrayList<>();

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pageNumber) {
            // getItem is called to instantiate the fragment for the given page.
            fragments.add(AlarmMenuFragment.getInstance(pageNumber));
            return fragments.get(pageNumber);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public void notifyDataSetChanged() {
            for (AlarmMenuFragment a : fragments) {
                a.updateAdapter(); // also update the underlying data lists
            }
            super.notifyDataSetChanged();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.activity_one_time_alarms);
                case 1:
                    return getString(R.string.activity_recurring_alarms);
            }
            return null;
        }
    }

    public static class AlarmMenuFragment extends Fragment {

        private RealmResults<Alarm> alarms;
        private AlarmListAdapter adapter;

        public void updateAdapter() {
            adapter.notifyDataSetChanged();
        }

        public static AlarmMenuFragment getInstance(int pageNumber) {
            AlarmMenuFragment fragment = new AlarmMenuFragment();
            Bundle args = new Bundle();
            args.putInt("PAGE_NUMBER", pageNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            ListView list = (ListView) rootView.findViewById(R.id.alarm_list);
            int pageNumber = getArguments().getInt("PAGE_NUMBER");
            adapter = new AlarmListAdapter(getActivity(), pageNumber);
            alarms = adapter.getAlarms();
            list.setAdapter(adapter);
            list.setOnItemClickListener(clicker(pageNumber));
            return rootView;
        }

        private AdapterView.OnItemClickListener clicker(final int pageNumber) {
            return new AdapterView.OnItemClickListener(){
                @SuppressLint("InflateParams")
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    // Display a dialog with info on the selected alarm
                    LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);

                    Alarm alarmElement = alarms.get(position);
                    final View dialogLayout = inflater.inflate(R.layout.dialog_edit_alarm, null);;

                    if (pageNumber == 0) {
                        // Set date
                        ((TextView)dialogLayout.findViewById(R.id.date)).setText(StringDisplay.getOnetimeDate(alarmElement.getOneTimeAlarmYear(), alarmElement.getOneTimeAlarmMonth(), alarmElement.getOneTimeAlarmDay()));
                    } else {
                        // Hide date picker, show days picker
                        dialogLayout.findViewById(R.id.date_title).setVisibility(View.GONE);
                        dialogLayout.findViewById(R.id.date_box).setVisibility(View.GONE);
                        dialogLayout.findViewById(R.id.days_title).setVisibility(View.VISIBLE);
                        dialogLayout.findViewById(R.id.days_box).setVisibility(View.VISIBLE);
                        // Set days
                        ((TextView)dialogLayout.findViewById(R.id.days)).setText(StringDisplay.getRecurringDays(getActivity(), alarmElement.getRecurringChosenDays()));
                    }
                    // Set time
                    ((TextView)dialogLayout.findViewById(R.id.time)).setText(StringDisplay.getTime(alarmElement.getAlarmHour(), alarmElement.getAlarmMinute()));
                    // Set Ringtone
                    ((TextView)dialogLayout.findViewById(R.id.ringtone)).setText(StringDisplay.getRingtone(getActivity(), alarmElement.getChosenRingtone()));
                    // Set Vibration
                    ((TextView)dialogLayout.findViewById(R.id.vibration)).setText(StringDisplay.getVibration(getActivity(), alarmElement.getChosenVibrationMode()));
                    // Set station
                    ((TextView)dialogLayout.findViewById(R.id.station)).setText(StringDisplay.getStation(alarmElement.getStationName(), alarmElement.getStationDirection()));

                    new AlertDialog.Builder(getActivity())
                            .setIcon(R.drawable.ic_settings)
                            .setTitle(getString(R.string.alarm_settings))
                            .setView(dialogLayout)
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String a =((EditText)dialogLayout.findViewById(R.id.station_search_edittext)).getText().toString();
                                    Toast.makeText(getActivity(), a, Toast.LENGTH_LONG).show();
                                    // TODO save edit
                                }
                            })
                            .setNeutralButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle(getString(R.string.delete_alarm_title))
                                            .setMessage(getString(R.string.delete_alarm_message))
                                            .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Realm realm = Realm.getDefaultInstance();
                                                    realm.beginTransaction();
                                                    alarms.deleteFromRealm(position);
                                                    realm.commitTransaction();
                                                    adapter.notifyDataSetChanged();
                                                    // TODO Remove scheduled alarm!!
                                                }
                                            })
                                            .setNegativeButton(getString(R.string.cancel), null)
                                            .show();
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show();

                }
            };
        }
    }
}
