package aenadon.wienerlinienalarm.models.alarm;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Alarm extends RealmObject {

    @PrimaryKey
    private String id;

    private AlarmNotificationInfo alarmNotificationInfo;
    private AlarmStationData alarmStationData;

    public Alarm() {
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
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
}
