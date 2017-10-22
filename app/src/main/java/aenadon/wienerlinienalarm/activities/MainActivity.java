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
import java.util.Arrays;
import java.util.List;

import aenadon.wienerlinienalarm.BuildConfig;
import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.adapter.AlarmListAdapter;
import aenadon.wienerlinienalarm.enums.AlarmType;
import aenadon.wienerlinienalarm.update.UpdateDatasetService;
import aenadon.wienerlinienalarm.utils.Keys;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter tabAdapter;
    private ViewPager tabContainer;

    private BroadcastReceiver refreshReceiver;

    private static List<AlarmType> alarmTypes = Arrays.asList(AlarmType.values());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateStationData();
        showBatteryWarningDialog();
        setupViews();
        setupAlarmReceiver();
    }

    private void updateStationData() {
        new UpdateDatasetService(MainActivity.this).execute();
    }

    private void showBatteryWarningDialog() {
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

    private void setupViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        tabContainer = (ViewPager) findViewById(R.id.container);
        tabContainer.setAdapter(tabAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(tabContainer);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AlarmSetterActivity.class);
                AlarmType currentlyOpenTab = alarmTypes.get(tabContainer.getCurrentItem());
                i.putExtra(Keys.Extra.ALARM_TYPE, currentlyOpenTab);
                startActivityForResult(i, 0);
            }
        });
    }

    private void setupAlarmReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Keys.Intent.REFRESH_LIST);

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                tabAdapter.notifyDataSetChanged();
            }
        };
        registerReceiver(refreshReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshReceiver != null) {
            unregisterReceiver(refreshReceiver);
            refreshReceiver = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            tabAdapter.notifyDataSetChanged();
            if (data != null) {
                AlarmType alarmType = (AlarmType)data.getSerializableExtra(Keys.Extra.ALARM_TYPE);
                tabContainer.setCurrentItem(alarmType.ordinal());
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
            fragments.add(AlarmMenuFragment.getInstance(pageNumber));
            return fragments.get(pageNumber);
        }

        @Override
        public int getCount() {
            return alarmTypes.size();
        }

        @Override
        public void notifyDataSetChanged() {
            for (AlarmMenuFragment a : fragments) {
                a.updateUnderlyingList();
            }
            super.notifyDataSetChanged();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(alarmTypes.get(position).getMessageCode());
        }
    }

    public static class AlarmMenuFragment extends Fragment {

        private AlarmType alarmType;
        private AlarmListAdapter adapter;

        public void updateUnderlyingList() {
            adapter.notifyDataSetChanged();
        }

        public static AlarmMenuFragment getInstance(int pageNumber) {
            AlarmMenuFragment fragment = new AlarmMenuFragment();
            Bundle args = new Bundle();

            AlarmType passedAlarmType = alarmTypes.get(pageNumber);
            args.putSerializable(Keys.Extra.ALARM_TYPE, passedAlarmType);

            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            ListView list = (ListView) rootView.findViewById(R.id.alarm_list);
            alarmType = (AlarmType)getArguments().getSerializable(Keys.Extra.ALARM_TYPE);
            adapter = new AlarmListAdapter(getActivity(), alarmType);
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
                    i.putExtra(Keys.Extra.ALARM_TYPE, alarmType);
                    i.putExtra(Keys.Extra.ALARM_ID, adapter.getAlarmId(position));
                    startActivityForResult(i, Keys.RequestCode.EDIT_ALARM);
                }
            };
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Activity.RESULT_OK && requestCode == Keys.RequestCode.EDIT_ALARM) {
                adapter.notifyDataSetChanged();
            }
        }
    }
}
