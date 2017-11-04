package aenadon.wienerlinienalarm.utils;

public interface Keys {

    interface Extra {
        String ALARM_TYPE = "ALARM_TYPE";
        String ALARM_ID = "ALARM_ID";
        String SELECTED_STATION_ID = "SELECTED_STATION_ID";
        String SELECTED_STEIG_ID = "SELECTED_STEIG_ID";
        String LINE_NAME_AND_DIRECTION = "LINE_NAME_AND_DIRECTION";
        String VIEW_TO_USE = "VIEW_TO_USE";
        String SELECTED_STATION_NAME = "SELECTED_STATION_NAME";
        String SNACKBAR_MESSAGE = "SNACKBAR_MESSAGE";
        String NOTIFICATION_ID = "NOTIFICATION_ID";
        String RETRIES_COUNT = "RETRIES_COUNT";
    }

    interface Prefs {
        String FILE_BATTERY_REMINDER = "FILE_BATTERY_REMINDER";
        String FILE_SCHEDULED_ALARMS = "FILE_SCHEDULED_ALARMS";
        String FILE_AUTOINCREMENT_ID = "FILE_AUTOINCREMENT_ID";

        String KEY_BATTERY_REMINDER_DISMISSED = "KEY_BATTERY_REMINDER_DISMISSED";
        String KEY_AUTOINCREMENT_ID = "KEY_AUTOINCREMENT_ID";
    }

    interface Intent {
        String DATASET_UPDATED = "DATASET_UPDATED";
        String REFRESH_LIST = "REFRESH_LIST";
    }

    interface RequestCode {
        int EDIT_ALARM = 0;
        int SELECT_STEIG = 1;
        int SELECT_RINGTONE = 2;
    }

    interface Bundle {
        String DATE_PICKER = "BUNDLE_DATE_PICKER";
        String TIME_PICKER = "BUNDLE_TIME_PICKER";
        String DAYS_PICKER = "BUNDLE_DAYS_PICKER";
        String RINGTONE_PICKER = "BUNDLE_RINGTONE_PICKER";
        String VIBRATION_PICKER = "BUNDLE_VIBRATION_PICKER";
        String STATION_PICKER = "BUNDLE_STATION_PICKER";
        String ALARM_TYPE = "BUNDLE_ALARM_TYPE";
    }
}
