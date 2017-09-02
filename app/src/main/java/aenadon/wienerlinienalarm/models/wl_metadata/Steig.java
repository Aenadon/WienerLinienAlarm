package aenadon.wienerlinienalarm.models.wl_metadata;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Steig extends RealmObject {

    @PrimaryKey
    private String rbl;
    private String lineId;
    private String direction;

    public Steig() {
    }

    public Steig(String rbl, String lineId, String direction) {
        this.rbl = rbl;
        this.lineId = lineId;
        this.direction = direction;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getRbl() {
        return rbl;
    }

    public void setRbl(String rbl) {
        this.rbl = rbl;
    }
}
