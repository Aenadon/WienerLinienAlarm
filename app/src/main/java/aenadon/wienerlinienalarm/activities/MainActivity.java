package aenadon.wienerlinienalarm.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import aenadon.wienerlinienalarm.BuildConfig;
import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.adapter.AlarmListAdapter;
import aenadon.wienerlinienalarm.utils.Const;
import io.realm.Realm;

import static aenadon.wienerlinienalarm.utils.Const.EXTRA_ALARM_MODE;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    // makes sure to refresh the list when an alarm goes off
    private BroadcastReceiver refreshReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new GetApiFiles(MainActivity.this).execute(); // get CSV files/check for updates on them

        showBatteryWarning();

        Realm.init(MainActivity.this); // for all database related stuff

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AlarmSetterActivity.class);
                i.putExtra(Const.EXTRA_ALARM_MODE, mViewPager.getCurrentItem());
                startActivityForResult(i, 0);
            }
        });

        // receiver to refresh view after alarm goes off
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Const.INTENT_REFRESH_LIST);

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mSectionsPagerAdapter.notifyDataSetChanged();
            }
        };
        registerReceiver(refreshReceiver, intentFilter);
    }

    private void showBatteryWarning() {
        final SharedPreferences batteryReminderPrefs = MainActivity.this.getPreferences(MODE_PRIVATE);
        final String BATTERY_REMINDER = "BATTERY_REMINDER";
        final String BATTERY_REMINDER_DOZE = "BATTERY_REMINDER_DOZE";

        boolean batteryReminderDismissed = batteryReminderPrefs.getBoolean(BATTERY_REMINDER, false);
        final boolean batteryReminderDozeDismissed = batteryReminderPrefs.getBoolean(BATTERY_REMINDER_DOZE, false);

        View batteryReminderDialog = View.inflate(MainActivity.this, R.layout.checkbox, null);
        final CheckBox batteryReminderCheckbox = (CheckBox)batteryReminderDialog.findViewById(R.id.battery_reminder_checkbox);
        // we need to have 2 separate objects of the view because we can't
        // assign the same instance of the view to two dialog boxes at once
        View batteryReminderDialog2 = View.inflate(MainActivity.this, R.layout.checkbox, null);
        final CheckBox batteryReminderCheckbox2 = (CheckBox)batteryReminderDialog2.findViewById(R.id.battery_reminder_checkbox);

        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID) && !batteryReminderDozeDismissed) {

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.doze_message_title))
                    .setMessage(getString(R.string.doze_message_text))
                    .setView(batteryReminderDialog)
                    .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent batterySettings = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                            startActivity(batterySettings);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (batteryReminderCheckbox.isChecked()) {
                                batteryReminderPrefs.edit().putBoolean(BATTERY_REMINDER_DOZE, true).apply();
                            }
                        }
                    })
                    .show();
        }
        if (!batteryReminderDismissed) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.battery_message_title))
                    .setMessage(getString(R.string.battery_message_text))
                    .setView(batteryReminderDialog2)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (batteryReminderCheckbox2.isChecked()) {
                                batteryReminderPrefs.edit().putBoolean(BATTERY_REMINDER, true).apply();
                            }
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        // kill the receiver on activity destruction
        if (refreshReceiver != null) {
            unregisterReceiver(refreshReceiver);
            refreshReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            mSectionsPagerAdapter.notifyDataSetChanged();
            if (data != null) {
                mViewPager.setCurrentItem(data.getIntExtra(EXTRA_ALARM_MODE, 0));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<AlarmMenuFragment> fragments = new ArrayList<>();

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

        private int alarmMode;
        private AlarmListAdapter adapter;

        public void updateAdapter() {
            adapter.notifyDataSetChanged();
        }

        public static AlarmMenuFragment getInstance(int pageNumber) {
            AlarmMenuFragment fragment = new AlarmMenuFragment();
            Bundle args = new Bundle();
            args.putInt(EXTRA_ALARM_MODE, pageNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            ListView list = (ListView) rootView.findViewById(R.id.alarm_list);
            alarmMode = getArguments().getInt(EXTRA_ALARM_MODE);
            adapter = new AlarmListAdapter(getActivity(), alarmMode);
            list.setAdapter(adapter);

            // http://stackoverflow.com/a/17807347/3673616
            @SuppressLint("InflateParams") View emptyView = inflater.inflate(R.layout.list_placeholder, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            emptyView.setLayoutParams(params);
            ((LinearLayout)emptyView).setGravity(Gravity.CENTER);
            ((ViewGroup)list.getParent()).addView(emptyView);
            list.setEmptyView(emptyView);

            list.setOnItemClickListener(launchDialogEditor());
            return rootView;
        }
        private AdapterView.OnItemClickListener launchDialogEditor() {
            return new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(getActivity(), DialogEditActivity.class);
                    i.putExtra(EXTRA_ALARM_MODE, alarmMode);
                    i.putExtra(Const.EXTRA_DB_POSITION, position);
                    startActivityForResult(i, Const.REQUEST_EDIT_ALARM);
                }
            };
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Activity.RESULT_OK && requestCode == Const.REQUEST_EDIT_ALARM) {
                adapter.notifyDataSetChanged();
            }
        }
    }
}
