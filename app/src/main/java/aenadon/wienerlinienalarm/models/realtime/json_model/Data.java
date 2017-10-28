package aenadon.wienerlinienalarm.models.realtime.json_model;

import java.util.List;

public class Data {

    private List<Monitor> monitors;

    public List<Monitor> getMonitors() {
        return monitors;
    }

    public void setMonitors(List<Monitor> monitors) {
        this.monitors = monitors;
    }
}
