package aenadon.wienerlinienalarm.models.alarm;

import io.realm.RealmObject;
import io.realm.annotations.Required;


public class AlarmStationData extends RealmObject {

    @Required
    private String stationName;
    @Required
    private String stationDirection;
    @Required
    private String stationIdForXMLApi;
    @Required
    private String rbl;
    private int jsonStationIndex;

    public AlarmStationData() {
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStationDirection() {
        return stationDirection;
    }

    public void setStationDirection(String stationDirection) {
        this.stationDirection = stationDirection;
    }

    public String getStationIdForXMLApi() {
        return stationIdForXMLApi;
    }

    public void setStationIdForXMLApi(String stationIdForXMLApi) {
        this.stationIdForXMLApi = stationIdForXMLApi;
    }

    public String getRbl() {
        return rbl;
    }

    public void setRbl(String rbl) {
        this.rbl = rbl;
    }

    public int getJsonStationIndex() {
        return jsonStationIndex;
    }

    public void setJsonStationIndex(int jsonStationIndex) {
        this.jsonStationIndex = jsonStationIndex;
    }
}
