package aenadon.wienerlinienalarm;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;

class C {

    // This class is for constants used throughout the app.

    static final String CSV_FILENAME = "halteinfo.csv";
    static final int CSV_PART_VERSION = 0;
    static final int CSV_PART_STATION = 1;
    static final int CSV_PART_STEIG = 2;

    static final int REQUEST_STATION = 0;
    static final int REQUEST_RINGTONE = 1;

    static final long VIBRATION_SHORT = 0;

    static final String STATION_NAME = "stationName";
    static final String STATION_ID = "stationid";

    static final String SHAREDPREFS_FILE = "steige";
    static final String SEPARATOR = ";";
    static final String CSV_FILE_SEPARATOR = "--------------------"; // 20 * -

    // U-Bahn Endstationen
    static final String U1 = "U1";
    static final String U2 = "U2";
    static final String U3 = "U3";
    static final String U4 = "U4";
    static final String U6 = "U6";

    static String getCSVfromFile(Context c) {
        try {
            File csvFile = new File(c.getFilesDir(), C.CSV_FILENAME);
            if (csvFile.exists()) {
                byte[] csvBytes = new byte[(int) csvFile.length()]; // will the file size ever exceed 2GB? let's hope not
                FileInputStream fis = new FileInputStream(csvFile);
                fis.read(csvBytes);
                return new String(csvBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    static String getUbahnEndstation(String line, String direction) {
        String U1H = "Leopoldau";       String U1R = "Reumannplatz";
        String U2H = "Karlsplatz";      String U2R = "Seestadt";
        String U3H = "Simmering";       String U3R = "Ottakring";
        String U4H = "Heiligenstadt";   String U4R = "Hütteldorf";
        String U6H = "Floridsdorf";     String U6R = "Siebenhirten";
        switch (line) {
            case U1: // if H return H else return R
                return direction.equals("H") ? U1H : U1R;
            case U2:
                return direction.equals("H") ? U2H : U2R;
            case U3:
                return direction.equals("H") ? U3H : U3R;
            case U4:
                return direction.equals("H") ? U4H : U4R;
            case U6:
                return direction.equals("H") ? U6H : U6R;
            default:
                return null;
        }
    }



}