package aenadon.wienerlinienalarm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import aenadon.wienerlinienalarm.models.Alarm;
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
                startActivityForResult(new Intent(getApplication(), AlarmSetterActivity.class), 0);
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
            adapter.notifyDataSetChanged(); // TODO not working?
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
                    final View dialogLayout;

                    if (pageNumber == 0) {
                        dialogLayout = inflater.inflate(R.layout.dialog_edit_alarm_onetime, null);

                        // Set date
                        Calendar cal = Calendar.getInstance();
                        cal.set(alarmElement.getOneTimeAlarmYear(), alarmElement.getOneTimeAlarmMonth(), alarmElement.getOneTimeAlarmDay());
                        ((TextView)dialogLayout.findViewById(R.id.date)).setText(
                                DateFormat.getDateInstance().format(cal.getTimeInMillis())
                        );
                        // Set time
                        ((TextView)dialogLayout.findViewById(R.id.time)).setText(
                                String.format(Locale.ENGLISH, "%02d:%02d", alarmElement.getAlarmHour(), alarmElement.getAlarmMinute())
                        );
                        // Set Ringtone
                        String ringtoneUriString = alarmElement.getChosenRingtone();
                        String ringtoneToDisplay = getString(R.string.alarm_no_ringtone_chosen);
                        if (ringtoneUriString != null) {
                            Uri ringtoneUri = Uri.parse(alarmElement.getChosenRingtone());
                            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
                            ringtoneToDisplay = ringtone.getTitle(getActivity());
                        }
                        ((TextView)dialogLayout.findViewById(R.id.ringtone)).setText(ringtoneToDisplay);
                        // Set Vibration
                        String vibraModeToDisplay = "";
                        switch (alarmElement.getChosenVibrationMode()) {
                            case C.VIBRATION_NONE:
                                vibraModeToDisplay = getString(R.string.alarm_no_vibration_chosen);
                                break;
                            case C.VIBRATION_SHORT:
                                vibraModeToDisplay = getString(R.string.alarm_vibration_short);
                                break;
                            case C.VIBRATION_MEDIUM:
                                vibraModeToDisplay = getString(R.string.alarm_vibration_medium);
                                break;
                            case C.VIBRATION_LONG:
                                vibraModeToDisplay = getString(R.string.alarm_vibration_long);
                                break;
                        }
                        ((TextView)dialogLayout.findViewById(R.id.vibration)).setText(vibraModeToDisplay);
                        // Set station
                        ((TextView)dialogLayout.findViewById(R.id.station)).setText(alarmElement.getStationName() + "\n" + alarmElement.getStationDirection());
                    } else {
                        dialogLayout = inflater.inflate(R.layout.dialog_edit_alarm_recurring, null);
                    }

                    new AlertDialog.Builder(getActivity())
                            .setIcon(R.drawable.ic_edit)
                            .setTitle(getString(R.string.edit_alarm))
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
