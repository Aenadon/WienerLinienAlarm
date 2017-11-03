package aenadon.wienerlinienalarm.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aenadon.wienerlinienalarm.BuildConfig;
import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.adapter.AlarmListAdapter;
import aenadon.wienerlinienalarm.enums.AlarmType;
import aenadon.wienerlinienalarm.update.UpdateDatasetTask;
import aenadon.wienerlinienalarm.utils.Keys;
import trikita.log.Log;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter tabAdapter;
    private ViewPager tabContainer;

    private BroadcastReceiver alarmTriggeredReceiver;

    private static List<AlarmType> alarmTypes = Arrays.asList(AlarmType.values());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateStationData();
        showBatteryWarningDialog();
        setupViews();
        setupAlarmTriggeredReceiver();
    }

    private void updateStationData() {
        new UpdateDatasetTask(getApplicationContext()).execute();
    }

    private void showBatteryWarningDialog() {
        boolean isBatteryReminderDismissed = isBatteryReminderDismissed();

        if (shouldShowDozeDialog(isBatteryReminderDismissed)) {
            batteryWarningWithDoze().show();
        } else if (!isBatteryReminderDismissed) {
            batteryWarningWithoutDoze().show();
        }
    }

    private boolean shouldShowDozeDialog(boolean batteryReminderDismissed) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            return false;
        }
        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);

        boolean hasNotWhitelistedApp;
        if (pm != null) {
            hasNotWhitelistedApp = !pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID);
        } else {
            Log.e("PowerManager is null");
            hasNotWhitelistedApp = true;
        }

        return hasNotWhitelistedApp && !batteryReminderDismissed;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private Dialog batteryWarningWithDoze() {
        View batteryReminderDialog = View.inflate(MainActivity.this, R.layout.checkbox, null);
        final CheckBox batteryReminderCheckbox = batteryReminderDialog.findViewById(R.id.battery_reminder_checkbox);

        String messageBody = getString(R.string.battery_optimization_warning_dialog_text) +
                "\n\n" + getString(R.string.battery_optimization_warning_doze);

        return new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.battery_optimization_warning_dialog_title))
                .setMessage(messageBody)
                .setView(batteryReminderDialog)
                .setPositiveButton(R.string.allow, (dialog, which) -> {
                    Intent batterySettings = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivity(batterySettings);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    if (batteryReminderCheckbox.isChecked()) {
                        dismissBatteryReminder();
                    }
                })
                .create();
    }

    private Dialog batteryWarningWithoutDoze() {
        View batteryReminderDialog = View.inflate(MainActivity.this, R.layout.checkbox, null);
        final CheckBox batteryReminderCheckbox = batteryReminderDialog.findViewById(R.id.battery_reminder_checkbox);

        String messageBody = getString(R.string.battery_optimization_warning_dialog_text);

        return new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.battery_optimization_warning_dialog_title))
                .setMessage(messageBody)
                .setView(batteryReminderDialog)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    if (batteryReminderCheckbox.isChecked()) {
                        dismissBatteryReminder();
                    }
                })
                .create();
    }

    private SharedPreferences getBatteryReminderPrefs() {
        return getSharedPreferences(Keys.Prefs.FILE_BATTERY_REMINDER, MODE_PRIVATE);
    }

    private boolean isBatteryReminderDismissed() {
        return getBatteryReminderPrefs().getBoolean(Keys.Prefs.KEY_BATTERY_REMINDER_DISMISSED, false);
    }

    private void dismissBatteryReminder() {
        getBatteryReminderPrefs().edit().putBoolean(Keys.Prefs.KEY_BATTERY_REMINDER_DISMISSED, true).apply();
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        tabContainer = findViewById(R.id.container);
        tabContainer.setAdapter(tabAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(tabContainer);

        FloatingActionButton fab = findViewById(R.id.fab_main);
        fab.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, AlarmSetterActivity.class);
            AlarmType currentlyOpenTab = alarmTypes.get(tabContainer.getCurrentItem());
            i.putExtra(Keys.Extra.ALARM_TYPE, currentlyOpenTab);
            startActivityForResult(i, 0);
        });
    }

    private void setupAlarmTriggeredReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Keys.Intent.REFRESH_LIST);

        alarmTriggeredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                tabAdapter.notifyDataSetChanged();
            }
        };
        registerReceiver(alarmTriggeredReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alarmTriggeredReceiver != null) {
            unregisterReceiver(alarmTriggeredReceiver);
            alarmTriggeredReceiver = null;
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
        if (data != null) {
            String snackbarMessage = data.getStringExtra(Keys.Extra.SNACKBAR_MESSAGE);
            if (snackbarMessage != null) {
                Snackbar.make(findViewById(R.id.fab_main), snackbarMessage, Snackbar.LENGTH_LONG).show();
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
            Stream.of(fragments).forEach(AlarmMenuFragment::updateUnderlyingList);
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
            ListView list = rootView.findViewById(R.id.alarm_list);
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
            return (parent, view, position, id) -> {
                Intent i = new Intent(getActivity(), DialogEditActivity.class);
                i.putExtra(Keys.Extra.ALARM_TYPE, alarmType);
                i.putExtra(Keys.Extra.ALARM_ID, adapter.getAlarmId(position));
                startActivityForResult(i, Keys.RequestCode.EDIT_ALARM);
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
