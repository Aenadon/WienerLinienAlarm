package aenadon.wienerlinienalarm.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.enums.AlarmType;
import aenadon.wienerlinienalarm.models.alarm.Alarm;
import aenadon.wienerlinienalarm.schedule.AlarmScheduler;
import aenadon.wienerlinienalarm.utils.Keys;
import aenadon.wienerlinienalarm.utils.StringFormatter;
import trikita.log.Log;

public class DialogEditActivity extends PickerActivity {

    private Alarm alarmToEdit;
    private BroadcastReceiver alarmTriggeredReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String alarmId = getIntent().getStringExtra(Keys.Extra.ALARM_ID);
        if (alarmId == null || alarmId.trim().isEmpty()) {
            Log.e("Alarm ID is null!");
            finish();
            return;
        }
        alarmToEdit = super.realm.where(Alarm.class).equalTo("id", alarmId).findFirst();
        if (alarmToEdit == null) {
            Log.e("Alarm with ID " + alarmId + " couldn't be retrieved => is null!");
            finish();
            return;
        }

        fillPickersWithData(alarmToEdit);
        setupViews(alarmToEdit);

        if (alarmToEdit.getAlarmType() == AlarmType.ONETIME) {
            // we only delete onetime alarms directly after
            // triggering ==> kill edit activity if open
            setupAlarmTriggeredReceiver();
        }
    }

    private void setupAlarmTriggeredReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Keys.Intent.REFRESH_LIST);

        alarmTriggeredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
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

    private void fillPickersWithData(Alarm alarm) {
        if (alarm.getAlarmType() == AlarmType.ONETIME) {
            super.datePicker.setPickedDate(alarm.getOnetimeAlarmDate());
        } else {
            super.daysPicker.setPickedDays(alarm.getRecurringChosenDays());
        }
        super.timePicker.setPickedTime(alarm.getAlarmTime());
        super.ringtonePicker.setPickedRingtone(alarm.getPickedRingtone());
        super.vibrationPicker.setPickedMode(alarm.getPickedVibrationMode());
        super.stationSteigPicker.setPickedSteig(alarm.getSteig().getId());
        super.stationSteigPicker.setDisplayName(alarm.getLineDirectionDisplayName());
    }

    private void setupViews(Alarm alarm) {
        if (alarm.getAlarmType() == AlarmType.ONETIME) {
            TextView dateView = findViewById (R.id.dialog_date_text);
            dateView.setText(StringFormatter.formatLocalDate(alarm.getOnetimeAlarmDate()));

            findViewById(R.id.dialog_date_container).setVisibility(View.VISIBLE);
        } else {
            TextView daysView = findViewById (R.id.dialog_days_text);
            daysView.setText(StringFormatter.getRecurringDays(DialogEditActivity.this, alarm.getRecurringChosenDays()));

            findViewById(R.id.dialog_days_container).setVisibility(View.VISIBLE);
        }
        TextView timeView = findViewById (R.id.dialog_time_text);
        TextView ringtoneView = findViewById (R.id.dialog_ringtone_text);
        TextView vibrationView = findViewById (R.id.dialog_vibration_text);
        TextView stationView = findViewById (R.id.dialog_station_text);

        timeView.setText(StringFormatter.formatLocalTime(alarm.getAlarmTime()));
        ringtoneView.setText(StringFormatter.getRingtone(DialogEditActivity.this, alarm.getPickedRingtone()));
        vibrationView.setText(alarm.getPickedVibrationMode().getMessageCode());
        stationView.setText(alarm.getLineDirectionDisplayName());
    }

    public void dismissDialog(View v) {
        finish();
    }

    public void deleteAlarm(View v) {
        new AlertDialog.Builder(DialogEditActivity.this)
                .setTitle(R.string.delete_alarm_title)
                .setMessage(R.string.delete_alarm_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    cancelAlarm();
                    deleteInDatabase();
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void cancelAlarm() {
        AlarmScheduler scheduler = new AlarmScheduler(DialogEditActivity.this, alarmToEdit);
        scheduler.cancelAlarmIfScheduled();
    }

    private void deleteInDatabase() {
        realm.beginTransaction();
        alarmToEdit.deleteFromRealm();
        realm.commitTransaction();
    }

    @Override
    protected boolean isNotEditActivity() {
        return false;
    }

    @Override
    protected int getLayout() {
        return R.layout.dialog_edit_alarm;
    }

    @Override
    protected int getDateView() {
        return R.id.dialog_date_text;
    }

    @Override
    protected int getTimeView() {
        return R.id.dialog_time_text;
    }

    @Override
    protected int getDaysView() {
        return R.id.dialog_days_text;
    }

    @Override
    protected int getRingtoneView() {
        return R.id.dialog_ringtone_text;
    }

    @Override
    protected int getVibrationView() {
        return R.id.dialog_vibration_text;
    }

    @Override
    protected int getStationSteigView() {
        return R.id.dialog_station_text;
    }

    @Override
    protected Alarm getAlarm() {
        return alarmToEdit;
    }
}
