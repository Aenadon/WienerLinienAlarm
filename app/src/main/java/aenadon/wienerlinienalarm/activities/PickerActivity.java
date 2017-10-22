package aenadon.wienerlinienalarm.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.activities.pickers.*;
import aenadon.wienerlinienalarm.enums.AlarmType;
import aenadon.wienerlinienalarm.models.alarm.Alarm;
import aenadon.wienerlinienalarm.models.wl_metadata.Station;
import aenadon.wienerlinienalarm.models.wl_metadata.Steig;
import aenadon.wienerlinienalarm.utils.Keys;
import io.realm.Realm;
import trikita.log.Log;


public abstract class PickerActivity extends AppCompatActivity {

    protected AlarmType alarmType = AlarmType.ONETIME;

    protected DatePicker datePicker;
    protected DaysPicker daysPicker;
    protected TimePicker timePicker;
    protected RingtonePicker ringtonePicker;
    protected VibrationPicker vibrationPicker;
    protected StationSteigPicker stationSteigPicker;

    private Set<AlarmPicker> visiblePickers = new HashSet<>();
    private Realm realm;

    protected abstract int getDateView();
    protected abstract int getTimeView();
    protected abstract int getDaysView();
    protected abstract int getRingtoneView();
    protected abstract int getVibrationView();
    protected abstract int getStationSteigView();
    protected abstract int getLayout();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());

        hideVibratorIfUnavailable();
        setupActionBar();

        datePicker = new DatePicker();
        datePicker.setArguments(getDatePickerBundle());

        timePicker = new TimePicker();
        timePicker.setArguments(getTimePickerBundle());

        daysPicker = new DaysPicker(PickerActivity.this, null, getDaysView());
        ringtonePicker = new RingtonePicker(PickerActivity.this, null, getRingtoneView());
        vibrationPicker = new VibrationPicker(PickerActivity.this, getVibrationView());
        stationSteigPicker = new StationSteigPicker(PickerActivity.this, getStationSteigView());

        visiblePickers.add(datePicker);
        visiblePickers.add(timePicker);
        visiblePickers.add(daysPicker);
        visiblePickers.add(ringtonePicker);
        visiblePickers.add(vibrationPicker);
        visiblePickers.add(stationSteigPicker);

        initializeAlarmMode();

        realm = Realm.getDefaultInstance();
    }

    private Bundle getDatePickerBundle() {
        Bundle datePickerBundle = new Bundle();
        datePickerBundle.putInt(Keys.Extra.VIEW_TO_USE, getDateView());
        return datePickerBundle;
    }

    private Bundle getTimePickerBundle() {
        Bundle timePickerBundle = new Bundle();
        timePickerBundle.putInt(Keys.Extra.VIEW_TO_USE, getTimeView());
        return timePickerBundle;
    }

    private void hideVibratorIfUnavailable() {
        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        boolean vibratorUnavailable = !vibrator.hasVibrator();
        if (vibratorUnavailable) {
            findViewById(R.id.choose_vibration_container).setVisibility(View.GONE);
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initializeAlarmMode() {
        int alarmModeInt = getIntent().getIntExtra(Keys.Extra.ALARM_MODE, AlarmType.ONETIME.ordinal());
        alarmType = AlarmType.values()[alarmModeInt];
        setCurrentModeDatePicker();
    }

    public void switchVisibleDatePicker(View v) {
        if (alarmType == AlarmType.ONETIME) {
            if (v.getId() == R.id.radio_frequency_one_time) {
                return;
            }
            alarmType = AlarmType.RECURRING;
        } else {
            if (v.getId() == R.id.radio_frequency_recurring) {
                return;
            }
            alarmType = AlarmType.ONETIME;
        }

        setCurrentModeDatePicker();
    }

    private void setCurrentModeDatePicker() {
        final RadioButton radioOnetime = (RadioButton)findViewById(R.id.radio_frequency_one_time);
        final RadioButton radioRecurring = (RadioButton)findViewById(R.id.radio_frequency_recurring);

        LinearLayout chooseDateContainer = (LinearLayout) findViewById(R.id.choose_date_container);
        LinearLayout chooseDaysContainer = (LinearLayout) findViewById(R.id.choose_days_container);

        switch (alarmType) {
            case ONETIME:
                radioOnetime.setChecked(true);
                radioRecurring.setChecked(false);
                chooseDaysContainer.setVisibility(View.GONE);
                chooseDateContainer.setVisibility(View.VISIBLE);

                visiblePickers.remove(daysPicker);
                visiblePickers.add(datePicker);
                break;
            case RECURRING:
                radioOnetime.setChecked(false);
                radioRecurring.setChecked(true);
                chooseDateContainer.setVisibility(View.GONE);
                chooseDaysContainer.setVisibility(View.VISIBLE);

                visiblePickers.remove(datePicker);
                visiblePickers.add(daysPicker);
                break;
        }
    }

    public void launchDatePicker(View v) {
        if (!datePicker.isAdded()) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(datePicker, "DatePicker");
            transaction.commit();
        }
    }

    public void launchTimePicker(View v) {
        if (!timePicker.isAdded()) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(timePicker, "TimePicker");
            transaction.commit();
        }
    }

    public void launchWeekdaysPicker(View v) {
        daysPicker.show();
    }

    public void launchRingtonePicker(View v) {
        ringtonePicker.show();
    }

    public void launchVibrationPicker(View v) {
        vibrationPicker.show();
    }

    public void launchStationSteigPicker(View v) {
        stationSteigPicker.show();
    }

    public void saveAlarm(View v) {
        List<Integer> errors = getErrors();
        if (!errors.isEmpty()) {
            showErrorDialog(errors);
            return;
        }

        realm.beginTransaction();
        Alarm newAlarm = new Alarm();

        String steigId = stationSteigPicker.getPickedSteig();
        Steig selectedSteig = realm.where(Steig.class).equalTo("id", steigId).findFirst();
        Station selectedStation = null;
        if (selectedSteig != null) {
            selectedStation = realm.where(Station.class).equalTo("id", selectedSteig.getStationId()).findFirst();
        }
        newAlarm.setStation(selectedStation);
        newAlarm.setSteig(selectedSteig);
        newAlarm.setLineDirectionDisplayName(stationSteigPicker.getDisplayName());

        newAlarm.setAlarmType(alarmType);

        if (alarmType == AlarmType.ONETIME) {
            newAlarm.setOnetimeAlarmDate(datePicker.getPickedDate());
        } else {
            newAlarm.setRecurringChosenDays(daysPicker.getPickedDays());
        }
        newAlarm.setAlarmTime(timePicker.getPickedTime());
        newAlarm.setPickedRingtone(ringtonePicker.getPickedRingtone());
        newAlarm.setPickedVibrationMode(vibrationPicker.getPickedMode());

        realm.copyToRealm(newAlarm);
        realm.commitTransaction();
        Log.v("Saved alarm with id " + newAlarm.getId() + " into database");

        // TODO Schedule alarm
    }

    private List<Integer> getErrors() {
        List<Integer> errors = new ArrayList<>();
        for (AlarmPicker picker : visiblePickers) {
            if (picker.hasError()) {
                errors.add(picker.getErrorStringId());
            }
        }
        if (alarmType == AlarmType.ONETIME) {
            LocalDate pickedDate = datePicker.getPickedDate();
            LocalTime pickedTime = timePicker.getPickedTime();
            if (pickedDate != null && pickedTime != null) {
                LocalDateTime dateTime = LocalDateTime.of(pickedDate, pickedTime);
                if (dateTime.isBefore(LocalDateTime.now())) {
                    errors.add(R.string.missing_info_past);
                }
            }
        }
        return errors;
    }

    private void showErrorDialog(List<Integer> errors) {
        List<String> errorStrings = new ArrayList<>();
        for (Integer errorCode : errors) {
            errorStrings.add(getString(errorCode));
        }
        String errorBody = TextUtils.join("\n", errorStrings);
        new AlertDialog.Builder(PickerActivity.this)
                .setTitle(R.string.missing_info_title)
                .setMessage(errorBody)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Keys.RequestCode.SELECT_STEIG:
                    stationSteigPicker.setPickedSteig(data);
                    break;
                case Keys.RequestCode.SELECT_RINGTONE:
                    ringtonePicker.setPickedRingtone(PickerActivity.this, data);
                    break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBundle(Keys.Bundle.DATE_PICKER, datePicker.saveState());
        outState.putBundle(Keys.Bundle.TIME_PICKER, timePicker.saveState());
        outState.putBundle(Keys.Bundle.DAYS_PICKER, daysPicker.saveState());
        outState.putBundle(Keys.Bundle.RINGTONE_PICKER, ringtonePicker.saveState());
        outState.putBundle(Keys.Bundle.VIBRATION_PICKER, vibrationPicker.saveState());
        outState.putBundle(Keys.Bundle.STATION_PICKER, stationSteigPicker.saveState());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        datePicker.restoreState(PickerActivity.this, savedInstanceState.getBundle(Keys.Bundle.DATE_PICKER));
        timePicker.restoreState(PickerActivity.this, savedInstanceState.getBundle(Keys.Bundle.TIME_PICKER));
        daysPicker.restoreState(PickerActivity.this, savedInstanceState.getBundle(Keys.Bundle.DAYS_PICKER));
        ringtonePicker.restoreState(PickerActivity.this, savedInstanceState.getBundle(Keys.Bundle.RINGTONE_PICKER));
        vibrationPicker.restoreState(PickerActivity.this, savedInstanceState.getBundle(Keys.Bundle.VIBRATION_PICKER));
        stationSteigPicker.restoreState(PickerActivity.this, savedInstanceState.getBundle(Keys.Bundle.STATION_PICKER));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}
