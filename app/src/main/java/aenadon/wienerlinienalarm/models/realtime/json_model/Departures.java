package aenadon.wienerlinienalarm.models.realtime.json_model;

import java.util.List;

public class Departures {

    private List<Departure> departure;

    public List<Departure> getDeparture() {
        return departure;
    }

    public void setDeparture(List<Departure> departure) {
        this.departure = departure;
    }
}
