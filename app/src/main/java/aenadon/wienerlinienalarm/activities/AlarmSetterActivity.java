package aenadon.wienerlinienalarm.activities;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.models.alarm.Alarm;

public class AlarmSetterActivity extends PickerActivity {

    @Override
    protected boolean isNotEditActivity() {
        return true;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_alarm_setter;
    }

    @Override
    protected int getDateView() {
        return R.id.choose_date_text;
    }

    @Override
    protected int getTimeView() {
        return R.id.choose_time_text;
    }

    @Override
    protected int getDaysView() {
        return R.id.choose_days_text;
    }

    @Override
    protected int getRingtoneView() {
        return R.id.choose_ringtone_text;
    }

    @Override
    protected int getVibrationView() {
        return R.id.choose_vibration_text;
    }

    @Override
    protected int getStationSteigView() {
        return R.id.choose_station_text;
    }

    @Override
    protected Alarm getAlarm() {
        return new Alarm();
    }
}
