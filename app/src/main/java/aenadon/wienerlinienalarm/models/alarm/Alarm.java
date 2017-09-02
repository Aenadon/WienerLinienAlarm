package aenadon.wienerlinienalarm.models.alarm;

import java.util.UUID;

import aenadon.wienerlinienalarm.enums.AlarmType;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Alarm extends RealmObject {

    @PrimaryKey
    @Required
    private String id;
    @Required
    private String alarmMode;
    private AlarmData alarmData;

    public Alarm() {
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public AlarmType getAlarmMode() {
        return AlarmType.valueOf(alarmMode);
    }

    public void setAlarmMode(AlarmType alarmMode) {
        this.alarmMode = alarmMode.toString();
    }

    public AlarmData getAlarmData() {
        return alarmData;
    }

    public void setAlarmData(AlarmData alarmData) {
        this.alarmData = alarmData;
    }
}
