package aenadon.wienerlinienalarm.models.alarm;

import java.util.Date;
import java.util.List;

import aenadon.wienerlinienalarm.enums.AlarmType;
import aenadon.wienerlinienalarm.enums.VibrationMode;
import aenadon.wienerlinienalarm.enums.Weekday;
import io.realm.RealmObject;
import io.realm.annotations.Required;

public class AlarmNotificationInfo extends RealmObject {

    @Required
    private String alarmType;

    private Date onetimeAlarmDate;
    private Byte recurringChosenDays;

    private int alarmHour;
    private int alarmMinute;

    private String chosenRingtone;
    private String chosenVibrationMode;

    public AlarmType getAlarmType() {
        return AlarmType.valueOf(alarmType);
    }

    public void setAlarmType(AlarmType alarmType) {
        this.alarmType = alarmType.toString();
    }

    public Date getOnetimeAlarmDate() {
        return onetimeAlarmDate;
    }

    public void setOnetimeAlarmDate(Date onetimeAlarmDate) {
        this.onetimeAlarmDate = onetimeAlarmDate;
    }

    public List<Weekday> getRecurringChosenDays() {
        return Weekday.weekdaysFromByte(recurringChosenDays);
    }

    public void setRecurringChosenDays(List<Weekday> recurringChosenDays) {
        this.recurringChosenDays = Weekday.byteFromWeekdays(recurringChosenDays);
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
}
