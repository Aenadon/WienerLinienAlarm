package aenadon.wienerlinienalarm.models.wl_metadata;

import aenadon.wienerlinienalarm.enums.Direction;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;


public class Steig extends RealmObject {

    @PrimaryKey
    private String id;
    @Required
    private String rbl;
    @Required
    private Line line;
    @Required
    private String stationId;
    @Required
    private String direction;

    public Steig() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public Direction getDirection() {
        return Direction.valueOf(direction);
    }

    public void setDirection(Direction direction) {
        this.direction = direction.toString();
    }

    public String getRbl() {
        return rbl;
    }

    public void setRbl(String rbl) {
        this.rbl = rbl;
    }
}
