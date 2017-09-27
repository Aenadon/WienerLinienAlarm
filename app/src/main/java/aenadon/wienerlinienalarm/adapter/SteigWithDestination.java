package aenadon.wienerlinienalarm.adapter;

import android.support.annotation.NonNull;

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

        return lnad1.compareTo(lnad2);
    }


}
