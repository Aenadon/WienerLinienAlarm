package aenadon.wienerlinienalarm.utils;


import android.content.Context;

import java.io.File;
import java.io.FileInputStream;

public class CSVWorkUtils {

    // This class contains some methods for working with the CSV files

    public static String getCSVfromFile(Context c) {
        try {
            File csvFile = new File(c.getFilesDir(), Const.CSV_FILENAME);
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
                throw new Error("Non-existant U-Bahn line");
        }
    }

}
