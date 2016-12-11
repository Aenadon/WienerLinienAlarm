package aenadon.wienerlinienalarm.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Calendar;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.models.Alarm;
import aenadon.wienerlinienalarm.utils.AlarmUtils;
import aenadon.wienerlinienalarm.utils.AlertDialogs;
import aenadon.wienerlinienalarm.utils.Const;
import aenadon.wienerlinienalarm.utils.Pickers;
import aenadon.wienerlinienalarm.utils.RealmUtils;
import aenadon.wienerlinienalarm.utils.StringDisplay;
import io.realm.Realm;
import io.realm.RealmResults;

public class DialogEditActivity extends AppCompatActivity {

    private RealmResults<Alarm> alarms;
    private Alarm alarmElement;
    private int dbPosition;

    private final Realm realm = Realm.getDefaultInstance();

    private final Pickers.DatePickerFragment datePicker = new Pickers.DatePickerFragment();
    private final Pickers.TimePickerFragment timePicker = new Pickers.TimePickerFragment();
    private final Pickers.DaysPicker daysPicker = new Pickers.DaysPicker();
    private final Pickers.RingtonePicker ringtonePicker = new Pickers.RingtonePicker();
    private final Pickers.VibrationPicker vibrationPicker = new Pickers.VibrationPicker();
    private final Pickers.StationPicker stationPicker = new Pickers.StationPicker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_alarm);

        int pageNumber = getIntent().getIntExtra(Const.EXTRA_ALARM_MODE, -1);
        dbPosition = getIntent().getIntExtra(Const.EXTRA_DB_POSITION, -1);

        if (pageNumber == -1 || dbPosition == -1) {
            throw new Error("WTF?"); // no way!!!!
        }

        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (!v.hasVibrator()) {
            findViewById(R.id.dialog_vibration_title).setVisibility(View.GONE);
            findViewById(R.id.dialog_vibration_box).setVisibility(View.GONE);
        }

        alarms = RealmUtils.getAlarms(DialogEditActivity.this, pageNumber);
        alarmElement = realm.copyFromRealm(alarms.get(dbPosition));

        switch (pageNumber) {
            case Const.ALARM_ONETIME:
                // Set date
                ((TextView)findViewById(R.id.dialog_date_text)).setText(StringDisplay.getOnetimeDate(alarmElement.getOneTimeAlarmYear(), alarmElement.getOneTimeAlarmMonth(), alarmElement.getOneTimeAlarmDay()));
                break;
            case Const.ALARM_RECURRING:
                // Hide date picker, show days picker
                findViewById(R.id.dialog_date_title).setVisibility(View.GONE);
                findViewById(R.id.dialog_date_box).setVisibility(View.GONE);
                findViewById(R.id.dialog_days_title).setVisibility(View.VISIBLE);
                findViewById(R.id.dialog_days_box).setVisibility(View.VISIBLE);
                // Set days
                ((TextView)findViewById(R.id.dialog_days_text)).setText(StringDisplay.getRecurringDays(DialogEditActivity.this, alarmElement.getRecurringChosenDays()));
                break;
            default:
                throw new Error("Non-existent alarm mode");
        }
        // Set time
        ((TextView)findViewById(R.id.dialog_time_text)).setText(StringDisplay.getTime(alarmElement.getAlarmHour(), alarmElement.getAlarmMinute()));
        // Set Ringtone
        ((TextView)findViewById(R.id.dialog_ringtone_text)).setText(StringDisplay.getRingtone(DialogEditActivity.this, alarmElement.getChosenRingtone()));
        // Set Vibration (only if vibrator is present)
        if (v.hasVibrator()) ((TextView)findViewById(R.id.dialog_vibration_text)).setText(StringDisplay.getVibration(DialogEditActivity.this, alarmElement.getChosenVibrationMode()));
        // Set station
        ((TextView)findViewById(R.id.dialog_station_text)).setText(StringDisplay.getStation(alarmElement.getStationName(), alarmElement.getStationDirection()));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Const.INTENT_REFRESH_LIST);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
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
        datePicker.restoreState(DialogEditActivity.this, savedInstanceState.getBundle(Const.BUNDLE_DATE_PICKER));
        timePicker.restoreState(DialogEditActivity.this, savedInstanceState.getBundle(Const.BUNDLE_TIME_PICKER));
        daysPicker.restoreState(DialogEditActivity.this, savedInstanceState.getBundle(Const.BUNDLE_DAYS_PICKER));
        ringtonePicker.restoreState(DialogEditActivity.this, savedInstanceState.getBundle(Const.BUNDLE_RINGTONE_PICKER));
        vibrationPicker.restoreState(DialogEditActivity.this, savedInstanceState.getBundle(Const.BUNDLE_VIBRATION_PICKER));
        stationPicker.restoreState(DialogEditActivity.this, savedInstanceState.getBundle(Const.BUNDLE_STATION_PICKER));
    }

    public void onClickHandler(View view) {
        switch (view.getId()) {
            case R.id.dialog_date_text:
            case R.id.dialog_date_edit:
                Bundle a = new Bundle();
                a.putInt(Const.EXTRA_VIEW_TO_USE, R.id.dialog_date_text);
                a.putIntArray(Const.EXTRA_PREV_DATE, alarmElement.getOneTimeDateAsArray());
                datePicker.setArguments(a);
                datePicker.show(getFragmentManager(), "DatePickerFragment");
                break;
            case R.id.dialog_days_text:
            case R.id.dialog_days_edit:
                daysPicker.show(DialogEditActivity.this, alarmElement.getRecurringChosenDays(), R.id.dialog_days_text);
                break;
            case R.id.dialog_time_text:
            case R.id.dialog_time_edit:
                Bundle b = new Bundle();
                b.putInt(Const.EXTRA_VIEW_TO_USE, R.id.dialog_time_text);
                b.putIntArray(Const.EXTRA_PREV_TIME, alarmElement.getTimeAsArray());
                timePicker.setArguments(b);
                timePicker.show(getFragmentManager(), "TimePickerFragment");
                break;
            case R.id.dialog_ringtone_text:
            case R.id.dialog_ringtone_edit:
                ringtonePicker.show(DialogEditActivity.this, alarmElement.getChosenRingtone(), R.id.dialog_ringtone_text);
                break;
            case R.id.dialog_vibration_text:
            case R.id.dialog_vibration_edit:
                vibrationPicker.show(DialogEditActivity.this, R.id.dialog_vibration_text);
                break;
            case R.id.dialog_station_text:
            case R.id.dialog_station_edit:
                stationPicker.show(DialogEditActivity.this, R.id.dialog_station_text);
                break;
            case R.id.dialog_button_cancel:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
            case R.id.dialog_button_delete:
                confirmDelete();
                break;
            case R.id.dialog_button_ok:
                done();
                break;
        }
    }

    private void done() {
        // Errorcheck
        boolean isError = false;
        String error = ""; // only one error at a time possible

        int[] chosenDate = datePicker.getChosenDate();
        int[] chosenTime = timePicker.getPickedTime();
        boolean[] chosenDays = daysPicker.getPickedDays();

        // Mode-specific checks
        switch (alarmElement.getAlarmMode()) {
            case Const.ALARM_ONETIME:
                Calendar now = Calendar.getInstance();
                Calendar calDate = Calendar.getInstance();

                int[] tempChosenDate = (chosenDate != null) ? chosenDate : alarmElement.getOneTimeDateAsArray();
                int[] tempChosenTime = (chosenTime != null) ? chosenTime : alarmElement.getTimeAsArray();

                calDate.set(tempChosenDate[0], tempChosenDate[1], tempChosenDate[2], tempChosenTime[0], tempChosenTime[1], 0); // 0 seconds
                if (calDate.compareTo(now) < 0) {
                    isError = true;
                    error = getString(R.string.missing_info_past);
                }
                break;
            case Const.ALARM_RECURRING:
                boolean[] tempChosenDays = (chosenDays != null) ? chosenDays : alarmElement.getRecurringChosenDays();
                if (Arrays.equals(tempChosenDays, new boolean[7])) { // compare with empty array
                    isError = true;
                    error = getString(R.string.missing_info_days);
                }
                break;
            default:
                throw new Error("Non-existent alarm mode");
        }
        // If error:
        if (isError) {
            AlertDialogs.missingInfo(DialogEditActivity.this, error);
            return; // if error, we're done here
        }

        realm.beginTransaction();
        alarmElement = realm.copyToRealmOrUpdate(alarmElement);

        AlarmUtils.cancelAlarm(DialogEditActivity.this, alarmElement); // cancel the old alarm

        switch(alarmElement.getAlarmMode()) {
            case Const.ALARM_ONETIME:
                if (datePicker.dateChanged(alarmElement)) alarmElement.setOneTimeDateAsArray(datePicker.getChosenDate());
                break;
            case Const.ALARM_RECURRING:
                if (daysPicker.daysChanged(alarmElement)) alarmElement.setRecurringChosenDays(daysPicker.getPickedDays());
                break;
            default:
                throw new Error("Non-existent alarm mode");
        }
        if (timePicker.timeChanged(alarmElement)) alarmElement.setTimeAsArray(timePicker.getPickedTime());
        if (ringtonePicker.ringtoneChanged(alarmElement)) alarmElement.setChosenRingtone(ringtonePicker.getPickedRingtone());
        if (vibrationPicker.vibrationChanged(alarmElement)) alarmElement.setChosenVibrationMode(vibrationPicker.getPickedVibrationMode());
        if (stationPicker.stationChanged(alarmElement)) alarmElement.setStationInfoAsArray(stationPicker.getStationInfoAsArray());

        AlarmUtils.scheduleAlarm(DialogEditActivity.this, alarmElement); // and schedule the changed one

        realm.commitTransaction();

        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Const.REQUEST_RINGTONE:
                    ringtonePicker.setPickedRingtone(DialogEditActivity.this, data);
                    break;
                case Const.REQUEST_STATION:
                    stationPicker.setPickedStation(data);
                    break;
            }
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(DialogEditActivity.this)
                .setTitle(getString(R.string.delete_alarm_title))
                .setMessage(getString(R.string.delete_alarm_message))
                .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (alarmElement.isValid()) {
                            AlarmUtils.cancelAlarm(DialogEditActivity.this, alarmElement);

                            realm.beginTransaction();
                            alarms.deleteFromRealm(dbPosition);
                            realm.commitTransaction();
                        }

                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}
