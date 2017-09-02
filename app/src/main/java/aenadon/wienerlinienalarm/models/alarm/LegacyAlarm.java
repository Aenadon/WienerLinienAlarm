package aenadon.wienerlinienalarm.models.alarm;


import android.util.Log;

import java.util.Calendar;
import java.util.UUID;

import aenadon.wienerlinienalarm.utils.AlarmUtils;
import aenadon.wienerlinienalarm.utils.Const;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

@Deprecated
public class LegacyAlarm extends RealmObject {

    @SuppressWarnings("CanBeFinal") // "The only restriction a RealmObject has is that fields are not allowed to be final, transient' or volatile."
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

    // LegacyAlarm mode
    private String chosenRingtone; // can be null
    private int chosenVibrationMode; // constant values, see Const

    // STATION DATA
    @Required
    private String stationName;
    @Required
    private String stationDirection;
    @Required
    private String stationId;
    private int stationArrayIndex;

    /////

    public LegacyAlarm() {
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

    // EXTRA
    public int[] getOneTimeDateAsArray() {
        return new int[]{oneTimeAlarmYear, oneTimeAlarmMonth, oneTimeAlarmDay};
    }
    // EXTRA
    public void setOneTimeDateAsArray(int[] date) {
        oneTimeAlarmYear = date[0];
        oneTimeAlarmMonth = date[1];
        oneTimeAlarmDay = date[2];
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

    // EXTRA
    public int[] getTimeAsArray() {
        return new int[]{alarmHour, alarmMinute};
    }
    // EXTRA
    public void setTimeAsArray(int[] time) {
        alarmHour = time[0];
        alarmMinute = time[1];
    }

    // EXTRA
    public long getAlarmInstantMillis() {
        String LOG_TAG = LegacyAlarm.class.getSimpleName();
        Calendar c = Calendar.getInstance();
        switch(alarmMode) {
            case Const.ALARM_ONETIME:
                c.set(oneTimeAlarmYear, oneTimeAlarmMonth, oneTimeAlarmDay, alarmHour, alarmMinute);
                return c.getTimeInMillis();
            case Const.ALARM_RECURRING:
                int currentWeekday = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7; // adjusts the given day automatically to the used format

                boolean[] days = getRecurringChosenDays();
                int offset = -1;
                for (int i = currentWeekday; i <= days.length + currentWeekday; i++) {
                    if (days[i % 7]) {
                        if (i % 7 == currentWeekday) {
                            Calendar alarmTime = Calendar.getInstance();
                            alarmTime.set(Calendar.HOUR_OF_DAY, alarmHour);
                            alarmTime.set(Calendar.MINUTE, alarmMinute);
                            alarmTime.set(Calendar.SECOND, 0);

                            Calendar realTime = Calendar.getInstance();

                            Log.d(LOG_TAG, "Alarmtime: " + alarmTime.getTimeInMillis() + "\n Realtime: " + realTime.getTimeInMillis());
                            if (realTime.getTimeInMillis() > alarmTime.getTimeInMillis() - AlarmUtils.tenSeconds) {
                                offset = 7;
                            } else {
                                offset = 0;
                            }
                            break;
                        } else {
                            offset = i - currentWeekday;
                            break;
                        }
                    }
                }
                if (offset == -1) {
                    throw new Error("Day comparison went wrong - offset did not change!");
                }

                Log.d(LOG_TAG, "Offset: " + offset);
                c.add(Calendar.DAY_OF_MONTH, offset);
                c.set(Calendar.HOUR_OF_DAY, alarmHour);
                c.set(Calendar.MINUTE, alarmMinute);
                return c.getTimeInMillis();
            default:
                throw new Error("Non-existent alarm mode");
        }
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

    public void setStationInfoAsArray(String[] info) {
        stationName = info[0];
        stationDirection = info[1];
        stationId = info[2];
        stationArrayIndex = Integer.parseInt(info[3]);
    }


}
