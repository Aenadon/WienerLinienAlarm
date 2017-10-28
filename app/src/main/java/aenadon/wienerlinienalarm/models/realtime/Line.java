package aenadon.wienerlinienalarm.models.realtime;

public class Line {

    private String name;
    private String towards;
    private String direction;
    private boolean barrierFree;
    private boolean realtimeSupported;
    private boolean trafficJam;

    private Departures departures;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTowards() {
        return towards;
    }

    public void setTowards(String towards) {
        this.towards = towards;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean isBarrierFree() {
        return barrierFree;
    }

    public void setBarrierFree(boolean barrierFree) {
        this.barrierFree = barrierFree;
    }

    public boolean isRealtimeSupported() {
        return realtimeSupported;
    }

    public void setRealtimeSupported(boolean realtimeSupported) {
        this.realtimeSupported = realtimeSupported;
    }

    public Departures getDepartures() {
        return departures;
    }

    public void setDepartures(Departures departures) {
        this.departures = departures;
    }

    public boolean isTrafficJam() {
        return trafficJam;
    }

    public void setTrafficJam(boolean trafficJam) {
        this.trafficJam = trafficJam;
    }
}
