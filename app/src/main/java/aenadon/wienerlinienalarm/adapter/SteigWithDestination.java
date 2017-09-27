package aenadon.wienerlinienalarm.adapter;

import android.support.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aenadon.wienerlinienalarm.models.wl_metadata.Steig;

public class SteigWithDestination implements Comparable<SteigWithDestination> {

    private String lineNameAndDirection;
    private Steig steig;

    public String getLineNameAndDirection() {
        return lineNameAndDirection;
    }

    public void setLineNameAndDirection(String lineNameAndDirection) {
        this.lineNameAndDirection = lineNameAndDirection;
    }

    public Steig getSteig() {
        return steig;
    }

    public void setSteig(Steig steig) {
        this.steig = steig;
    }

    public String getSteigId() {
        return steig.getId();
    }

    @Override
    public int compareTo(@NonNull SteigWithDestination o) {
        String lnad1 = this.lineNameAndDirection;
        String lnad2 = o.getLineNameAndDirection();

        if (lnad1 == null && lnad2 == null) {
            return 0;
        } else if (lnad1 == null) {
            return 1;
        } else if (lnad2 == null) {
            return -1;
        }

        Pattern pattern = Pattern.compile("^[NU]?(\\d*)");
        Matcher matcherForLnad1 = pattern.matcher(lnad1);
        Matcher matcherForLnad2 = pattern.matcher(lnad2);

        if (matcherForLnad1.find() && matcherForLnad2.find()) {
            String lineNumber1String = matcherForLnad1.group(1);
            String lineNumber2String = matcherForLnad2.group(1);

            if (allStringsHaveText(lineNumber1String, lineNumber2String)) {
                int lineNumber1 = Integer.parseInt(lineNumber1String);
                int lineNumber2 = Integer.parseInt(lineNumber2String);

                return lineNumber1 - lineNumber2;
            }
        }
        return lnad1.compareTo(lnad2);
    }

    private boolean allStringsHaveText(String... strings) {
        for (String s : strings) {
            if (s == null || s.equals("")) {
                return false;
            }
        }
        return true;
    }
}
