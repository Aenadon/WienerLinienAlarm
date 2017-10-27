package aenadon.wienerlinienalarm.schedule;

import android.content.Context;

import java.util.List;

import aenadon.wienerlinienalarm.models.alarm.Alarm;

class BatchScheduler extends AlarmScheduler {

    private List<Alarm> alarms;

    BatchScheduler(Context ctx, List<Alarm> alarms) {
        super(ctx, null);
        this.alarms = alarms;
    }

    void scheduleAlarms() {
        for (Alarm alarm : alarms) {
            super.alarm = alarm;
            scheduleAlarmAndReturnMessage();
        }
    }
}
