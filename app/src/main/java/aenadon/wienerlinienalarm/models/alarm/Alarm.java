package aenadon.wienerlinienalarm.models.alarm;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

import java.util.Set;
import java.util.UUID;

import aenadon.wienerlinienalarm.enums.AlarmType;
import aenadon.wienerlinienalarm.enums.VibrationMode;
import aenadon.wienerlinienalarm.enums.Weekday;
import aenadon.wienerlinienalarm.models.wl_metadata.Line;
import aenadon.wienerlinienalarm.models.wl_metadata.Station;
import aenadon.wienerlinienalarm.models.wl_metadata.Steig;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Alarm extends RealmObject {

    @PrimaryKey
    private String id;

    @Required
    private String alarmType;

    private Integer onetimeAlarmYear;
    private Integer onetimeAlarmMonth;
    private Integer onetimeAlarmDay;

    private Byte recurringChosenDays;

    private int alarmHour;
    private int alarmMinute;

    private String pickedRingtone;
    private String pickedVibrationMode;

    private Station station;
    private Steig steig;
    private String lineDirectionDisplayName;

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

    public String getPickedRingtone() {
        return pickedRingtone;
    }

    public void setPickedRingtone(String pickedRingtone) {
        this.pickedRingtone = pickedRingtone;
    }

    public VibrationMode getPickedVibrationMode() {
        return VibrationMode.valueOf(pickedVibrationMode);
    }

    public void setPickedVibrationMode(VibrationMode pickedVibrationMode) {
        this.pickedVibrationMode = pickedVibrationMode.toString();
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Steig getSteig() {
        return steig;
    }

    public void setSteig(Steig steig) {
        this.steig = steig;
    }

    public String getLineDirectionDisplayName() {
        return lineDirectionDisplayName;
    }

    public void setLineDirectionDisplayName(String lineDirectionDisplayName) {
        this.lineDirectionDisplayName = lineDirectionDisplayName;
    }

    public String getRbl() {
        return steig.getRbl();
    }

    public Line getLine() {
        return steig.getLine();
    }
}
