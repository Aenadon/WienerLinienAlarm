package aenadon.wienerlinienalarm.models.alarm;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

import java.util.Set;
import java.util.UUID;

import aenadon.wienerlinienalarm.enums.AlarmType;
import aenadon.wienerlinienalarm.enums.Weekday;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Alarm extends RealmObject {

    @PrimaryKey
    private String id;

    @Required
    private String alarmType;

    private AlarmNotificationInfo alarmNotificationInfo;
    private AlarmStationData alarmStationData;

    public Alarm() {
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public AlarmType getAlarmType() {
        return AlarmType.valueOf(alarmType);
    }

    public void setAlarmType(AlarmType alarmType) {
        this.alarmType = alarmType.toString();
    }

    public AlarmNotificationInfo getAlarmNotificationInfo() {
        return alarmNotificationInfo;
    }

    public void setAlarmNotificationInfo(AlarmNotificationInfo alarmNotificationInfo) {
        this.alarmNotificationInfo = alarmNotificationInfo;
    }

    public AlarmStationData getAlarmStationData() {
        return alarmStationData;
    }

    public void setAlarmStationData(AlarmStationData alarmStationData) {
        this.alarmStationData = alarmStationData;
    }

    public LocalTime getAlarmTime() {
        return alarmNotificationInfo.getAlarmTime();
    }

    public LocalDate getOnetimeDate() {
        return alarmNotificationInfo.getOnetimeAlarmDate();
    }

    public Set<Weekday> getRecurringDays() {
        return alarmNotificationInfo.getRecurringChosenDays();
    }
}
