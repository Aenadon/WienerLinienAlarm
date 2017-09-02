package aenadon.wienerlinienalarm.models.alarm;

import aenadon.wienerlinienalarm.enums.VibrationMode;
import io.realm.RealmObject;


public abstract class AlarmData extends RealmObject {

    private int alarmHour;
    private int alarmMinute;

    private String chosenRingtone;
    private String chosenVibrationMode;

    private AlarmStationData alarmStationData;

    public AlarmData() {
    }

    public int getAlarmHour() {
        return alarmHour;
    }

    public void setAlarmHour(int alarmHour) {
        this.alarmHour = alarmHour;
    }

    public int getAlarmMinute() {
        return alarmMinute;
    }

    public void setAlarmMinute(int alarmMinute) {
        this.alarmMinute = alarmMinute;
    }

    public String getChosenRingtone() {
        return chosenRingtone;
    }

    public void setChosenRingtone(String chosenRingtone) {
        this.chosenRingtone = chosenRingtone;
    }

    public VibrationMode getChosenVibrationMode() {
        return VibrationMode.valueOf(chosenVibrationMode);
    }

    public void setChosenVibrationMode(VibrationMode chosenVibrationMode) {
        this.chosenVibrationMode = chosenVibrationMode.toString();
    }

    public AlarmStationData getAlarmStationData() {
        return alarmStationData;
    }

    public void setAlarmStationData(AlarmStationData alarmStationData) {
        this.alarmStationData = alarmStationData;
    }
}
