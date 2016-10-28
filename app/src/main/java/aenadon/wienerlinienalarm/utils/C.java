package aenadon.wienerlinienalarm.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;

import aenadon.wienerlinienalarm.R;

public class C {

    // This class is for constants used throughout the app.

    public static final String CSV_FILENAME = "halteinfo.csv";
    public static final int CSV_PART_VERSION = 0;
    public static final int CSV_PART_STATION = 1;
    public static final int CSV_PART_STEIG = 2;

    public static final int ALARM_ONETIME = 0;
    public static final int ALARM_RECURRING = 1;

    public static final int REQUEST_STATION = 0;
    public static final int REQUEST_RINGTONE = 1;

    public static final int VIBRATION_NONE = 0;
    public static final int VIBRATION_SHORT = 1;
    public static final int VIBRATION_MEDIUM = 2;
    public static final int VIBRATION_LONG = 3;

    public static final long[] VIBRATION_DURATION = {
            0,
            100,
            250,
            500
    };

    public static final int[] VIBRATION_STRINGS = {
            R.string.alarm_no_vibration_chosen,
            R.string.alarm_vibration_short,
            R.string.alarm_vibration_medium,
            R.string.alarm_vibration_long
    };

    public static final String STATION_NAME = "stationName";
    public static final String STATION_ID = "stationid";

    public static final String CSV_FILE_SEPARATOR = "--------------------"; // 20 * -

    public static String getCSVfromFile(Context c) {
        try {
            File csvFile = new File(c.getFilesDir(), C.CSV_FILENAME);
            if (csvFile.exists()) {
                byte[] csvBytes = new byte[(int) csvFile.length()]; // will the file size ever exceed 2GB? let's hope not
                FileInputStream fis = new FileInputStream(csvFile);
                //noinspection ResultOfMethodCallIgnored
                fis.read(csvBytes);
                return new String(csvBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getUbahnEndstation(String line, String direction) {
        final String U1 = "U1";
        final String U2 = "U2";
        final String U3 = "U3";
        final String U4 = "U4";
        final String U6 = "U6";
        String H = "H";

        String U1H = "Leopoldau";       String U1R = "Reumannplatz";
        String U2H = "Karlsplatz";      String U2R = "Seestadt";
        String U3H = "Simmering";       String U3R = "Ottakring";
        String U4H = "Heiligenstadt";   String U4R = "Hütteldorf";
        String U6H = "Floridsdorf";     String U6R = "Siebenhirten";
        switch (line) {
            case U1: // if H return H else return R
                return direction.equals(H) ? U1H : U1R;
            case U2:
                return direction.equals(H) ? U2H : U2R;
            case U3:
                return direction.equals(H) ? U3H : U3R;
            case U4:
                return direction.equals(H) ? U4H : U4R;
            case U6:
                return direction.equals(H) ? U6H : U6R;
            default:
                return null;
        }
    }



}