package aenadon.wienerlinienalarm.utils;

public interface Keys {

    interface Extra {
        String ALARM_MODE = "ALARM_MODE";
        String VIEW_TO_USE = "VIEW_TO_USE";
        String PREV_DATE = "PREV_DATE";
        String PREV_TIME = "PREV_TIME";

        String STATION_INFO = "STATION_INFO";
        String DB_POSITION = "DB_POSITION";
        String STATION_NAME = "stationName";
        String STATION_ID = "stationid";
    }

    interface Prefs {
        String PREF_NAME_BATTERY_REMINDER = "BATTERY_REMINDER";
        String BATTERY_REMINDER_DISMISSED = "BATTERY_REMINDER_DISMISSED";
        String BATTERY_REMINDER_DOZE_DISMISSED = "BATTERY_REMINDER_DOZE_DISMISSED";
    }

}
