package aenadon.wienerlinienalarm.adapter;

import aenadon.wienerlinienalarm.models.wl_metadata.Steig;

public class SteigWithLineName {

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
}
