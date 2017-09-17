package aenadon.wienerlinienalarm.models.alarm;

import aenadon.wienerlinienalarm.models.wl_metadata.Station;
import aenadon.wienerlinienalarm.models.wl_metadata.Steig;
import io.realm.RealmObject;
import io.realm.annotations.Required;


public class AlarmStationData extends RealmObject {

    private Station station;
    private Steig steig;
    private String lineDirectionName;

    public AlarmStationData() {
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Steig getSteig() {
        return steig;
    }

    public void setSteig(Steig steig) {
        this.steig = steig;
    }

    public String getLineDirectionName() {
        return lineDirectionName;
    }

    public void setLineDirectionName(String lineDirectionName) {
        this.lineDirectionName = lineDirectionName;
    }

    public String getRbl() {
        return steig.getRbl();
    }

    public String getStringRepresentation() {
        return station.getName() + "\n" + lineDirectionName;
    }
}
