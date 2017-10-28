package aenadon.wienerlinienalarm.models.realtime;

import java.util.List;

class Departures {

    private List<Departure> departure;

    public List<Departure> getDeparture() {
        return departure;
    }

    public void setDeparture(List<Departure> departure) {
        this.departure = departure;
    }
}
