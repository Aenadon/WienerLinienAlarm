package aenadon.wienerlinienalarm.utils;

import aenadon.wienerlinienalarm.R;

@Deprecated
public interface Const {
    // LegacyAlarm types
    int ALARM_ONETIME = 0;
    int ALARM_RECURRING = 1;

    // Duration of vibration modes
    long[] VIBRATION_DURATION = {
            0,
            100,
            250,
            500
    };

    String EXTRA_ALARM_ID = "";

    String INTENT_REFRESH_LIST = "INTENT_REFRESH_LIST";

    // SharedPreferences names
    String PREFS_SCHEDULED_ALARMS = "SCHEDULED_ALARMS_LIST";
    String PREFS_AUTOINCREMENT_ID = "AUTOINCREMENT";

}