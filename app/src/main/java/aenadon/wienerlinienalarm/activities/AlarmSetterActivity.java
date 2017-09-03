package aenadon.wienerlinienalarm.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.models.alarm.LegacyAlarm;
import aenadon.wienerlinienalarm.utils.AlarmUtils;
import aenadon.wienerlinienalarm.utils.AlertDialogs;
import aenadon.wienerlinienalarm.utils.Const;
import aenadon.wienerlinienalarm.utils.Keys;
import aenadon.wienerlinienalarm.utils.Pickers;
import io.realm.Realm;

public class AlarmSetterActivity extends AppCompatActivity {

    private int alarmMode = Const.ALARM_ONETIME;

    private final Pickers.DatePickerFragment datePicker = new Pickers.DatePickerFragment();
    private final Pickers.TimePickerFragment timePicker = new Pickers.TimePickerFragment();
    private final Pickers.DaysPicker daysPicker = new Pickers.DaysPicker();
    private final Pickers.RingtonePicker ringtonePicker = new Pickers.RingtonePicker();
    private final Pickers.VibrationPicker vibrationPicker = new Pickers.VibrationPicker();
    private final Pickers.StationPicker stationPicker = new Pickers.StationPicker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setter);

        // if device has no vibrator, hide the vibration choice
        Vibrator v = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        if (!v.hasVibrator()) {
            findViewById(R.id.choose_vibration_container).setVisibility(View.GONE);
        }

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        alarmMode = getIntent().getIntExtra(Keys.Extra.ALARM_MODE, Const.ALARM_ONETIME); // default: onetime
        pickAlarmFrequency(alarmMode); // initial setup
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Keys.Extra.ALARM_MODE, alarmMode);
        outState.putBundle(Const.BUNDLE_DATE_PICKER, datePicker.saveState());
        outState.putBundle(Const.BUNDLE_TIME_PICKER, timePicker.saveState());
        outState.putBundle(Const.BUNDLE_DAYS_PICKER, daysPicker.saveState());
        outState.putBundle(Const.BUNDLE_RINGTONE_PICKER, ringtonePicker.saveState());
        outState.putBundle(Const.BUNDLE_VIBRATION_PICKER, vibrationPicker.saveState());
        outState.putBundle(Const.BUNDLE_STATION_PICKER, stationPicker.saveState());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        alarmMode = savedInstanceState.getInt(Keys.Extra.ALARM_MODE);
        datePicker.restoreState(AlarmSetterActivity.this, savedInstanceState.getBundle(Const.BUNDLE_DATE_PICKER));
        timePicker.restoreState(AlarmSetterActivity.this, savedInstanceState.getBundle(Const.BUNDLE_TIME_PICKER));
        daysPicker.restoreState(AlarmSetterActivity.this, savedInstanceState.getBundle(Const.BUNDLE_DAYS_PICKER));
        ringtonePicker.restoreState(AlarmSetterActivity.this, savedInstanceState.getBundle(Const.BUNDLE_RINGTONE_PICKER));
        vibrationPicker.restoreState(AlarmSetterActivity.this, savedInstanceState.getBundle(Const.BUNDLE_VIBRATION_PICKER));
        stationPicker.restoreState(AlarmSetterActivity.this, savedInstanceState.getBundle(Const.BUNDLE_STATION_PICKER));

        pickAlarmFrequency(alarmMode);
    }

    // handling all the click events from the view
    public void onClickHandler(View view) {
        switch (view.getId()) {
            case R.id.radio_frequency_one_time:
                pickAlarmFrequency(Const.ALARM_ONETIME);
                break;
            case R.id.radio_frequency_recurring:
                pickAlarmFrequency(Const.ALARM_RECURRING);
                break;
            case R.id.choose_date_button:
            case R.id.choose_date_text:
                Bundle a = new Bundle();
                a.putInt(Const.EXTRA_VIEW_TO_USE, R.id.choose_date_text);
                datePicker.setArguments(a);
                datePicker.show(getFragmentManager(), "DatePickerDialog");
                break;
            case R.id.choose_time_button:
            case R.id.choose_time_text:
                Bundle b = new Bundle();
                b.putInt(Const.EXTRA_VIEW_TO_USE, R.id.choose_time_text);
                timePicker.setArguments(b);
                timePicker.show(getFragmentManager(), "TimePickerDialog");
                break;
            case R.id.choose_days_button:
            case R.id.choose_days_text:
                daysPicker.show(AlarmSetterActivity.this, null, R.id.choose_days_text);
                break;
            case R.id.choose_station_button:
            case R.id.choose_station_text:
                stationPicker.show(AlarmSetterActivity.this, R.id.choose_station_text);
                break;
            case R.id.choose_ringtone_button:
            case R.id.choose_ringtone_text:
                ringtonePicker.show(AlarmSetterActivity.this, null, R.id.choose_ringtone_text);
                break;
            case R.id.choose_vibration_button:
            case R.id.choose_vibration_text:
                vibrationPicker.show(AlarmSetterActivity.this, R.id.choose_vibration_text);
                break;
            case R.id.fab_alarm:
                done();
                break;
        }
    }

    private void pickAlarmFrequency(int setTo) {
        final RadioButton radioOnetime = (RadioButton)findViewById(R.id.radio_frequency_one_time);
        final RadioButton radioRecurring = (RadioButton)findViewById(R.id.radio_frequency_recurring);

        LinearLayout chooseDateContainer = (LinearLayout) findViewById(R.id.choose_date_container);
        LinearLayout chooseDaysContainer = (LinearLayout) findViewById(R.id.choose_days_container);

        // show selected, hide previous
        switch (setTo) {
            case Const.ALARM_ONETIME:
                radioOnetime.setChecked(true);
                radioRecurring.setChecked(false);
                chooseDaysContainer.setVisibility(View.GONE);
                chooseDateContainer.setVisibility(View.VISIBLE);
                break;
            case Const.ALARM_RECURRING:
                radioOnetime.setChecked(false);
                radioRecurring.setChecked(true);
                chooseDateContainer.setVisibility(View.GONE);
                chooseDaysContainer.setVisibility(View.VISIBLE);
                break;
        }
        alarmMode = setTo; // in the end, set the mode as current mode
    }

    private void done() {
        // Errorcheck
        boolean isError = false;
        List<String> errors = new ArrayList<>();

        int[] chosenDate = datePicker.getChosenDate();
        int[] chosenTime = timePicker.getPickedTime();
        boolean[] chosenDays = daysPicker.getPickedDays();
        String chosenRingtone = ringtonePicker.getPickedRingtone();
        int chosenVibratorMode = vibrationPicker.getPickedVibrationMode();

        if (!stationPicker.stationWasSet()) {
            isError = true;
            errors.add(getString(R.string.missing_info_station));
        }
        // Mode-specific checks
        switch (alarmMode) {
            case Const.ALARM_ONETIME:
                if (chosenDate == null) {
                    isError = true;
                    errors.add(getString(R.string.missing_info_date));
                }
                if (chosenDate != null && chosenTime != null) {
                    Calendar now = Calendar.getInstance();
                    Calendar calDate = Calendar.getInstance();
                    calDate.set(chosenDate[0], chosenDate[1], chosenDate[2], chosenTime[0], chosenTime[1], 0); // 0 seconds
                    if (calDate.compareTo(now) < 0) {
                        isError = true;
                        errors.add(getString(R.string.missing_info_past));
                    }
                }
                break;
            case Const.ALARM_RECURRING:
                if (chosenDays == null || Arrays.equals(chosenDays, new boolean[7])) { // compare with empty array
                    isError = true;
                    errors.add(getString(R.string.missing_info_days));
                }
                break;
            default:
                throw new Error("Non-existent alarm mode");
        }
        // General checks
        if (chosenTime == null) {
            isError = true;
            errors.add(getString(R.string.missing_info_time));
        }
        // If error:
        if (isError) {
            AlertDialogs.missingInfo(AlarmSetterActivity.this, TextUtils.join("\n", errors.toArray()));
            return; // if error, we're done here
        }

        LegacyAlarm newAlarm = new LegacyAlarm();
        newAlarm.setAlarmMode(alarmMode);
        switch (alarmMode) {
            case Const.ALARM_ONETIME:
                newAlarm.setOneTimeAlarmYear(chosenDate[0]);
                newAlarm.setOneTimeAlarmMonth(chosenDate[1]);
                newAlarm.setOneTimeAlarmDay(chosenDate[2]);
                break;
            case Const.ALARM_RECURRING:
                newAlarm.setRecurringChosenDays(chosenDays);
                break;
        }
        newAlarm.setAlarmHour(chosenTime[0]);
        newAlarm.setAlarmMinute(chosenTime[1]);

        newAlarm.setChosenRingtone(chosenRingtone);
        newAlarm.setChosenVibrationMode(chosenVibratorMode);

        // {stationName, stationDir, stationId, h.getArrayIndex()}
        newAlarm.setStationName(stationPicker.getPickedStationName());
        newAlarm.setStationDirection(stationPicker.getPickedStationDir());
        newAlarm.setStationId(stationPicker.getPickedStationId());
        newAlarm.setStationArrayIndex(stationPicker.getPickedStationArrayIndex());

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction(); // we're doing it synchronously because it does not take much time
        realm.copyToRealm(newAlarm);
        realm.commitTransaction();

        AlarmUtils.scheduleAlarm(AlarmSetterActivity.this, newAlarm);

        setResult(Activity.RESULT_OK, new Intent().putExtra(Keys.Extra.ALARM_MODE, alarmMode));
        finish(); // we're done here.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Const.REQUEST_STATION:
                    stationPicker.setPickedStation(data);
                    break;
                case Const.REQUEST_RINGTONE:
                    ringtonePicker.setPickedRingtone(AlarmSetterActivity.this, data);
                    break;
            }
        }
    }


}
