package aenadon.wienerlinienalarm.models;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Alarm extends RealmObject {

    @PrimaryKey
    private String id; // Unique ID (constructor)

    private int alarmMode; // Onetime or Recurring?

    // Onetime.
    private Integer oneTimeAlarmYear;
    private Integer oneTimeAlarmMonth;
    private Integer oneTimeAlarmDay;

    // Recurring.
    private Byte recurringChosenDays; //1+16

    // Time
    private int alarmHour;
    private int alarmMinute;

    // Alarm mode
    private String chosenRingtone; // can be null
    private int chosenVibrationMode; // constant values, see C

    // STATION DATA
    @Required
    private String stationName;
    @Required
    private String stationDirection;
    @Required
    private String stationId;
    private int stationArrayIndex;

    /////

    public Alarm() {
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    // no setter for ID because it shouldn't be set manually

    public int getAlarmMode() {
        return alarmMode;
    }

    public void setAlarmMode(int alarmMode) {
        this.alarmMode = alarmMode;
    }

    public Integer getOneTimeAlarmYear() {
        return oneTimeAlarmYear;
    }

    public void setOneTimeAlarmYear(Integer oneTimeAlarmYear) {
        this.oneTimeAlarmYear = oneTimeAlarmYear;
    }

    public Integer getOneTimeAlarmMonth() {
        return oneTimeAlarmMonth;
    }

    public void setOneTimeAlarmMonth(Integer oneTimeAlarmMonth) {
        this.oneTimeAlarmMonth = oneTimeAlarmMonth;
    }

    public Integer getOneTimeAlarmDay() {
        return oneTimeAlarmDay;
    }

    public void setOneTimeAlarmDay(Integer oneTimeAlarmDay) {
        this.oneTimeAlarmDay = oneTimeAlarmDay;
    }

    public boolean[] getRecurringChosenDays() {
        boolean[] days = new boolean[7];

        for (int i = 0; i < 7; i++) {
            byte x = (byte)Math.pow(2,i);
            days[i] = (recurringChosenDays & x) == x;
        }

        return days;
    }

    public void setRecurringChosenDays(boolean[] recurringChosenDays) {
        byte chosenDays = 0;
        for (int i = 0; i < 7; i++) {
            if (recurringChosenDays[i]) {
                chosenDays += Math.pow(2, i);
            }
        }
        this.recurringChosenDays = chosenDays;
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

    public int getChosenVibrationMode() {
        return chosenVibrationMode;
    }

    public void setChosenVibrationMode(int chosenVibrationMode) {
        this.chosenVibrationMode = chosenVibrationMode;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStationDirection() {
        return stationDirection;
    }

    public void setStationDirection(String stationDirection) {
        this.stationDirection = stationDirection;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public int getStationArrayIndex() {
        return stationArrayIndex;
    }

    public void setStationArrayIndex(int stationArrayIndex) {
        this.stationArrayIndex = stationArrayIndex;
    }


}
