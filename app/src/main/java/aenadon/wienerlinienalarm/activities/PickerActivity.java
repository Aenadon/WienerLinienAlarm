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

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.activities.pickers.AlarmPicker;
import aenadon.wienerlinienalarm.activities.pickers.DatePicker;
import aenadon.wienerlinienalarm.activities.pickers.DaysPicker;
import aenadon.wienerlinienalarm.activities.pickers.RingtonePicker;
import aenadon.wienerlinienalarm.activities.pickers.StationSteigPicker;
import aenadon.wienerlinienalarm.activities.pickers.TimePicker;
import aenadon.wienerlinienalarm.activities.pickers.VibrationPicker;
import aenadon.wienerlinienalarm.enums.AlarmType;
import aenadon.wienerlinienalarm.models.alarm.Alarm;
import aenadon.wienerlinienalarm.models.wl_metadata.Station;
import aenadon.wienerlinienalarm.models.wl_metadata.Steig;
import aenadon.wienerlinienalarm.schedule.AlarmScheduler;
import aenadon.wienerlinienalarm.utils.Keys;
import io.realm.Realm;
import trikita.log.Log;


public abstract class PickerActivity extends AppCompatActivity {

    private AlarmType alarmType = AlarmType.ONETIME;

    DatePicker datePicker;
    DaysPicker daysPicker;
    TimePicker timePicker;
    RingtonePicker ringtonePicker;
    VibrationPicker vibrationPicker;
    StationSteigPicker stationSteigPicker;

    private Set<AlarmPicker> visiblePickers = new HashSet<>();
    Realm realm;

    protected abstract boolean isNotEditActivity();
    protected abstract int getLayout();
    protected abstract int getDateView();
    protected abstract int getTimeView();
    protected abstract int getDaysView();
    protected abstract int getRingtoneView();
    protected abstract int getVibrationView();
    protected abstract int getStationSteigView();

    protected abstract Alarm getAlarm();

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

        daysPicker = new DaysPicker(PickerActivity.this, getDaysView());
        ringtonePicker = new RingtonePicker(PickerActivity.this, getRingtoneView());
        vibrationPicker = new VibrationPicker(PickerActivity.this, getVibrationView());
        stationSteigPicker = new StationSteigPicker(PickerActivity.this, getStationSteigView());

        visiblePickers.add(datePicker);
        visiblePickers.add(timePicker);
        visiblePickers.add(daysPicker);
        visiblePickers.add(ringtonePicker);
        visiblePickers.add(vibrationPicker);
        visiblePickers.add(stationSteigPicker);

        alarmType = (AlarmType)getIntent().getSerializableExtra(Keys.Extra.ALARM_TYPE);

        if (isNotEditActivity()) {
            setCurrentModeDatePicker();
        }

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
        if (vibrator == null || !vibrator.hasVibrator()) {
            findViewById(R.id.choose_vibration_container).setVisibility(View.GONE);
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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
        final RadioButton radioOnetime = findViewById(R.id.radio_frequency_one_time);
        final RadioButton radioRecurring = findViewById(R.id.radio_frequency_recurring);

        LinearLayout chooseDateContainer = findViewById(R.id.choose_date_container);
        LinearLayout chooseDaysContainer = findViewById(R.id.choose_days_container);

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

    public void saveAndScheduleAlarm(View v) {
        List<Integer> errors = getErrors();
        if (!errors.isEmpty()) {
            showErrorDialog(errors);
            return;
        }

        realm.beginTransaction();
        Alarm alarm = getAlarm();

        String steigId = stationSteigPicker.getPickedSteig();
        Steig selectedSteig = realm.where(Steig.class).equalTo("id", steigId).findFirst();
        Station selectedStation = null;
        if (selectedSteig != null) {
            selectedStation = realm.where(Station.class).equalTo("id", selectedSteig.getStationId()).findFirst();
        }
        alarm.setStation(selectedStation);
        alarm.setSteig(selectedSteig);
        alarm.setLineDirectionDisplayName(stationSteigPicker.getDisplayName());

        alarm.setAlarmType(alarmType);

        if (alarmType == AlarmType.ONETIME) {
            alarm.setOnetimeAlarmDate(datePicker.getPickedDate());
        } else {
            alarm.setRecurringChosenDays(daysPicker.getPickedDays());
        }
        alarm.setAlarmTime(timePicker.getPickedTime());
        alarm.setPickedRingtone(ringtonePicker.getPickedRingtone());
        alarm.setPickedVibrationMode(vibrationPicker.getPickedMode());

        realm.copyToRealm(alarm);
        realm.commitTransaction();
        Log.v("Saved alarm with id " + alarm.getId() + " into database");

        AlarmScheduler scheduler = new AlarmScheduler(PickerActivity.this, alarm);
        scheduler.cancelAlarmIfScheduled();
        String snackbarMessage = scheduler.scheduleAlarmAndReturnMessage();

        Intent intent = new Intent()
                .putExtra(Keys.Extra.ALARM_TYPE, alarmType)
                .putExtra(Keys.Extra.SNACKBAR_MESSAGE, snackbarMessage);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private List<Integer> getErrors() {
        List<Integer> errors = new ArrayList<>();
        // In Edit mode, the only two things you can mess up are
        // choosing a past time or unselecting all recurring days
        if (isNotEditActivity()) {
            Stream.of(visiblePickers)
                    .filter(AlarmPicker::hasError)
                    .forEach(picker -> errors.add(picker.getErrorStringId()));
        } else {
            if (alarmType == AlarmType.RECURRING && daysPicker.hasError()) {
                errors.add(daysPicker.getErrorStringId());
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
        List<String> errorStrings = Stream.of(errors).map(this::getString).collect(Collectors.toList());

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
