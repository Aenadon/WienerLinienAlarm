package aenadon.wienerlinienalarm.models.realtime.json_model;

import java.util.List;

public class Monitor {

    private List<JsonLine> lines;

    public List<JsonLine> getLines() {
        return lines;
    }

    public void setLines(List<JsonLine> lines) {
        this.lines = lines;
    }
}
