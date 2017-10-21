package aenadon.wienerlinienalarm.utils;

import aenadon.wienerlinienalarm.R;

@Deprecated
public interface Const {

    // This class is for constants used throughout the app.

    // CSV
    String CSV_FILENAME = "halteinfo.csv";

    // LegacyAlarm types
    int ALARM_ONETIME = 0;
    int ALARM_RECURRING = 1;

    // onActivityForResult request codes
    int REQUEST_STATION = 0;
    int REQUEST_RINGTONE = 1;

    // Vibration modes
    int VIBRATION_NONE = 0;
    int VIBRATION_SHORT = 1;
    int VIBRATION_MEDIUM = 2;
    int VIBRATION_LONG = 3;

    // Duration of vibration modes
    long[] VIBRATION_DURATION = {
            0,
            100,
            250,
            500
    };

    // Strings for the vibration modes
    int[] VIBRATION_STRINGS = {
            R.string.alarm_none,
            R.string.alarm_vibration_short,
            R.string.alarm_vibration_medium,
            R.string.alarm_vibration_long
    };

    // Key names for extra variables in bundles/intents
    String EXTRA_VIEW_TO_USE = "VIEW_TO_USE";
    String EXTRA_PREV_DATE = "PREV_DATE";
    String EXTRA_PREV_TIME = "PREV_TIME";

    String EXTRA_DB_POSITION = "DB_POSITION";

    String EXTRA_ALARM_ID = "";

    String INTENT_REFRESH_LIST = "INTENT_REFRESH_LIST";

    // SharedPreferences names
    String PREFS_SCHEDULED_ALARMS = "SCHEDULED_ALARMS_LIST";
    String PREFS_AUTOINCREMENT_ID = "AUTOINCREMENT";

    String BUNDLE_DATE_PICKER = "BUNDLE_DATE_PICKER";
    String BUNDLE_TIME_PICKER = "BUNDLE_TIME_PICKER";
    String BUNDLE_DAYS_PICKER = "BUNDLE_DAYS_PICKER";
    String BUNDLE_RINGTONE_PICKER = "BUNDLE_RINGTONE_PICKER";
    String BUNDLE_VIBRATION_PICKER = "BUNDLE_VIBRATION_PICKER";
    String BUNDLE_STATION_PICKER = "BUNDLE_STATION_PICKER";

}