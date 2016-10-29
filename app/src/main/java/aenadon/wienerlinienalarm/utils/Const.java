package aenadon.wienerlinienalarm.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;

import aenadon.wienerlinienalarm.R;

public interface Const {

    // This class is for constants used throughout the app.

    String CSV_FILENAME = "halteinfo.csv";
    int CSV_PART_VERSION = 0;
    int CSV_PART_STATION = 1;
    int CSV_PART_STEIG = 2;

    int ALARM_ONETIME = 0;
    int ALARM_RECURRING = 1;

    int REQUEST_STATION = 0;
    int REQUEST_RINGTONE = 1;
    int REQUEST_EDIT_ALARM = 2;

    int VIBRATION_NONE = 0;
    int VIBRATION_SHORT = 1;
    int VIBRATION_MEDIUM = 2;
    int VIBRATION_LONG = 3;

    long[] VIBRATION_DURATION = {
            0,
            100,
            250,
            500
    };

    int[] VIBRATION_STRINGS = {
            R.string.alarm_no_vibration_chosen,
            R.string.alarm_vibration_short,
            R.string.alarm_vibration_medium,
            R.string.alarm_vibration_long
    };

    String STATION_NAME = "stationName";
    String STATION_ID = "stationid";

    String CSV_FILE_SEPARATOR = "--------------------"; // 20 * -



}