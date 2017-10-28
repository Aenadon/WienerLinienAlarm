package aenadon.wienerlinienalarm.models.realtime;

import java.util.List;

public class Monitor {

    private List<Line> lines;

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }
}
