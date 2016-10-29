package aenadon.wienerlinienalarm.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.models.Alarm;
import aenadon.wienerlinienalarm.utils.Const;
import aenadon.wienerlinienalarm.utils.RealmUtils;
import aenadon.wienerlinienalarm.utils.StringDisplay;
import io.realm.Realm;
import io.realm.RealmResults;

public class DialogEditActivity extends AppCompatActivity {

    private RealmResults<Alarm> alarms;
    private int dbPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_alarm);

        int pageNumber = getIntent().getIntExtra("alarmMode", -1);
        dbPosition = getIntent().getIntExtra("dbPosition", -1);

        if (pageNumber == -1 || dbPosition == -1) {
            throw new Error("WTF?"); // no way!!!!
        }

        alarms = RealmUtils.getAlarms(DialogEditActivity.this, pageNumber);
        Alarm alarmElement = alarms.get(dbPosition);

        if (pageNumber == Const.ALARM_ONETIME) {
            // Set date
            ((TextView)findViewById(R.id.date)).setText(StringDisplay.getOnetimeDate(alarmElement.getOneTimeAlarmYear(), alarmElement.getOneTimeAlarmMonth(), alarmElement.getOneTimeAlarmDay()));
        } else {
            // Hide date picker, show days picker
            findViewById(R.id.date_title).setVisibility(View.GONE);
            findViewById(R.id.date_box).setVisibility(View.GONE);
            findViewById(R.id.days_title).setVisibility(View.VISIBLE);
            findViewById(R.id.days_box).setVisibility(View.VISIBLE);
            // Set days
            ((TextView)findViewById(R.id.days)).setText(StringDisplay.getRecurringDays(DialogEditActivity.this, alarmElement.getRecurringChosenDays()));
        }
        // Set time
        ((TextView)findViewById(R.id.time)).setText(StringDisplay.getTime(alarmElement.getAlarmHour(), alarmElement.getAlarmMinute()));
        // Set Ringtone
        ((TextView)findViewById(R.id.ringtone)).setText(StringDisplay.getRingtone(DialogEditActivity.this, alarmElement.getChosenRingtone()));
        // Set Vibration
        ((TextView)findViewById(R.id.vibration)).setText(StringDisplay.getVibration(DialogEditActivity.this, alarmElement.getChosenVibrationMode()));
        // Set station
        ((TextView)findViewById(R.id.station)).setText(StringDisplay.getStation(alarmElement.getStationName(), alarmElement.getStationDirection()));
    }

    public void onClickHandler(View view) {
        switch (view.getId()) {
            case R.id.date:
            case R.id.date_edit:

                break;
            case R.id.days:
            case R.id.days_edit:

                break;
            case R.id.time:
            case R.id.time_edit:

                break;
            case R.id.ringtone:
            case R.id.ringtone_edit:

                break;
            case R.id.vibration:
            case R.id.vibration_edit:

                break;
            case R.id.station:
            case R.id.station_edit:

                break;
            case R.id.dialog_button_cancel:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
            case R.id.dialog_button_delete:
                confirmDelete();
                break;
            case R.id.dialog_button_ok:

                break;
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
