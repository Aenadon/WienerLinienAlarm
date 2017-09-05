package aenadon.wienerlinienalarm.models.wl_metadata;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Line extends RealmObject {

    @PrimaryKey
    private String id;
    private String line;
    private int lineSortOrder;
    private boolean realtimeEnabled;
    private String type;

    public Line() {
    }

    public Line(String id, String line, int lineSortOrder, boolean realtimeEnabled, String type) {
        this.id = id;
        this.line = line;
        this.lineSortOrder = lineSortOrder;
        this.realtimeEnabled = realtimeEnabled;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public int getLineSortOrder() {
        return lineSortOrder;
    }

    public void setLineSortOrder(int lineSortOrder) {
        this.lineSortOrder = lineSortOrder;
    }

    public boolean isRealtimeEnabled() {
        return realtimeEnabled;
    }

    public void setRealtimeEnabled(boolean realtimeEnabled) {
        this.realtimeEnabled = realtimeEnabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
