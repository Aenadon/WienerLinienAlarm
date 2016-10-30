package aenadon.wienerlinienalarm.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.models.Alarm;
import aenadon.wienerlinienalarm.utils.Const;
import aenadon.wienerlinienalarm.utils.RealmUtils;
import aenadon.wienerlinienalarm.utils.StringDisplay;
import io.realm.Realm;
import io.realm.RealmResults;

public class DialogEditActivity extends AppCompatActivity {

    private RealmResults<Alarm> alarms;
    private Alarm alarmElement;
    private int dbPosition;

    private Pickers.DatePickerFragment datePicker = new Pickers.DatePickerFragment();
    private Pickers.TimePickerFragment timePicker = new Pickers.TimePickerFragment();
    private Pickers.DaysPicker daysPicker = new Pickers.DaysPicker();
    private Pickers.RingtonePicker ringtonePicker = new Pickers.RingtonePicker();
    private Pickers.VibrationPicker vibrationPicker = new Pickers.VibrationPicker();
    private Pickers.StationPicker stationPicker = new Pickers.StationPicker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_alarm);

        int pageNumber = getIntent().getIntExtra("alarmMode", -1);
        dbPosition = getIntent().getIntExtra("dbPosition", -1);

        if (pageNumber == -1 || dbPosition == -1) {
            throw new Error("WTF?"); // no way!!!!
        }

        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (!v.hasVibrator()) {
            findViewById(R.id.dialog_vibration_title).setVisibility(View.GONE);
            findViewById(R.id.dialog_vibration_box).setVisibility(View.GONE);
        }

        alarms = RealmUtils.getAlarms(DialogEditActivity.this, pageNumber);
        alarmElement = alarms.get(dbPosition);

        if (pageNumber == Const.ALARM_ONETIME) {
            // Set date
            ((TextView)findViewById(R.id.dialog_date_text)).setText(StringDisplay.getOnetimeDate(alarmElement.getOneTimeAlarmYear(), alarmElement.getOneTimeAlarmMonth(), alarmElement.getOneTimeAlarmDay()));
        } else {
            // Hide date picker, show days picker
            findViewById(R.id.dialog_date_title).setVisibility(View.GONE);
            findViewById(R.id.dialog_date_box).setVisibility(View.GONE);
            findViewById(R.id.dialog_days_title).setVisibility(View.VISIBLE);
            findViewById(R.id.dialog_days_box).setVisibility(View.VISIBLE);
            // Set days
            ((TextView)findViewById(R.id.dialog_days_text)).setText(StringDisplay.getRecurringDays(DialogEditActivity.this, alarmElement.getRecurringChosenDays()));
        }
        // Set time
        ((TextView)findViewById(R.id.dialog_time_text)).setText(StringDisplay.getTime(alarmElement.getAlarmHour(), alarmElement.getAlarmMinute()));
        // Set Ringtone
        ((TextView)findViewById(R.id.dialog_ringtone_text)).setText(StringDisplay.getRingtone(DialogEditActivity.this, alarmElement.getChosenRingtone()));
        // Set Vibration (only if vibrator is present)
        if (v.hasVibrator()) ((TextView)findViewById(R.id.dialog_vibration_text)).setText(StringDisplay.getVibration(DialogEditActivity.this, alarmElement.getChosenVibrationMode()));
        // Set station
        ((TextView)findViewById(R.id.dialog_station_text)).setText(StringDisplay.getStation(alarmElement.getStationName(), alarmElement.getStationDirection()));
    }

    public void onClickHandler(View view) {
        switch (view.getId()) {
            case R.id.dialog_date_text:
            case R.id.dialog_date_edit:
                Bundle a = new Bundle();
                a.putInt("viewToUse", R.id.dialog_date_text);
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
                b.putInt("viewToUse", R.id.dialog_time_text);
                timePicker.setArguments(b);
                timePicker.show(getFragmentManager(), "TimePickerFragment");
                break;
            case R.id.dialog_ringtone_text:
            case R.id.dialog_ringtone_edit:
                ringtonePicker.show(DialogEditActivity.this, R.id.dialog_ringtone_text);
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
                // TODO this is just a test
                int[] dp = datePicker.getPickedDate();
                String dpx = (dp != null) ? dp[0]+"" : "NULL";
                int[] tp = timePicker.getPickedTime();
                String tpx = (tp != null) ? tp[1]+"" : "NULL";
                boolean[] dyp = daysPicker.getPickedDays();
                String dypx = (dyp != null) ? dyp[0]+"" : "NULL";

                Toast.makeText(DialogEditActivity.this,
                        dpx + "\n" +
                        tpx + "\n" +
                        dypx + "\n" +
                        ringtonePicker.getPickedRingtone() + "\n" +
                        vibrationPicker.getPickedVibrationMode() + "\n" +
                        stationPicker.getPickedStationName() + "\n",
                        Toast.LENGTH_LONG).show();
                break;
        }
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
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        alarms.deleteFromRealm(dbPosition);
                        realm.commitTransaction();

                        // TODO Remove scheduled alarm!!

                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}
