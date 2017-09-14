package aenadon.wienerlinienalarm.models.wl_metadata;

import aenadon.wienerlinienalarm.enums.TransportType;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;


public class Line extends RealmObject {

    @PrimaryKey
    private String id;
    @Required
    private String lineName;
    private int lineSortOrder;
    private boolean realtimeEnabled;
    @Required
    private String transportType;

    public Line() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
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

    public TransportType getTransportType() {
        return TransportType.findByTypeString(transportType);
    }

    public void setTransportType(TransportType type) {
        this.transportType = type.getTypeString();
    }
}
