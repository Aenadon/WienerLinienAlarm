package aenadon.wienerlinienalarm.models.alarm;

import java.util.Date;

import io.realm.annotations.Required;

public class OnetimeAlarmData extends AlarmData {

    @Required
    private Date alarmDate;

    public OnetimeAlarmData() {
    }

    public Date getAlarmDate() {
        return alarmDate;
    }

    public void setAlarmDate(Date alarmDate) {
        this.alarmDate = alarmDate;
    }
}
