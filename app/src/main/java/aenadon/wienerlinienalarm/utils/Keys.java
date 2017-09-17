package aenadon.wienerlinienalarm.utils;

public interface Keys {

    interface Extra {
        String ALARM_MODE = "ALARM_MODE";
        String SELECTED_STATION_ID = "SELECTED_STATION_ID";
        String SELECTED_STEIG_ID = "SELECTED_STEIG_ID";

        String LINE_NAME_AND_DIRECTION = "LINE_NAME_AND_DIRECTION";

        String VIEW_TO_USE = "VIEW_TO_USE";
        String PREV_DATE = "PREV_DATE";
        String PREV_TIME = "PREV_TIME";

        String STATION_INFO = "STATION_INFO";
        String DB_POSITION = "DB_POSITION";
        String STATION_NAME = "stationName";
    }

    interface Prefs {
        String PREF_NAME_BATTERY_REMINDER = "BATTERY_REMINDER";
        String BATTERY_REMINDER_DISMISSED = "BATTERY_REMINDER_DISMISSED";
        String BATTERY_REMINDER_DOZE_DISMISSED = "BATTERY_REMINDER_DOZE_DISMISSED";
    }

    interface Intent {
        String REFRESH_LIST = "REFRESH_LIST";
    }

    interface RequestCode {
        int EDIT_ALARM = 0;
        int SELECT_STEIG = 1;
    }

}
