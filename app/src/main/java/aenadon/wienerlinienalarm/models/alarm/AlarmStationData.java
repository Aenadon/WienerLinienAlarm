package aenadon.wienerlinienalarm.models.alarm;

import aenadon.wienerlinienalarm.models.wl_metadata.Station;
import aenadon.wienerlinienalarm.models.wl_metadata.Steig;
import io.realm.RealmObject;
import io.realm.annotations.Required;


public class AlarmStationData extends RealmObject {

    @Required
    private Station station;
    @Required
    private Steig steig;

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
}
