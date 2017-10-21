package aenadon.wienerlinienalarm.models.alarm;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

import java.util.Date;
import java.util.List;
import java.util.Set;

import aenadon.wienerlinienalarm.enums.AlarmType;
import aenadon.wienerlinienalarm.enums.VibrationMode;
import aenadon.wienerlinienalarm.enums.Weekday;
import io.realm.RealmObject;
import io.realm.annotations.Required;

public class AlarmNotificationInfo extends RealmObject {

    @Required
    private String alarmType;

    private int onetimeAlarmYear;
    private int onetimeAlarmMonth;
    private int onetimeAlarmDay;

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

    public LocalDate getOnetimeAlarmDate() {
        return LocalDate.of(
                onetimeAlarmYear,
                onetimeAlarmMonth,
                onetimeAlarmDay
        );
    }

    public void setOnetimeAlarmDate(LocalDate onetimeAlarmDate) {
        this.onetimeAlarmYear = onetimeAlarmDate.getYear();
        this.onetimeAlarmMonth = onetimeAlarmDate.getMonthValue();
        this.onetimeAlarmDay = onetimeAlarmDate.getDayOfMonth();
    }

    public Set<Weekday> getRecurringChosenDays() {
        return Weekday.weekdaysFromByte(recurringChosenDays);
    }

    public void setRecurringChosenDays(Set<Weekday> recurringChosenDays) {
        this.recurringChosenDays = Weekday.byteFromWeekdays(recurringChosenDays);
    }

    public LocalTime getAlarmTime() {
        return LocalTime.of(alarmHour, alarmMinute);
    }

    public void setAlarmTime(LocalTime alarmTime) {
        this.alarmHour = alarmTime.getHour();
        this.alarmMinute = alarmTime.getMinute();
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
