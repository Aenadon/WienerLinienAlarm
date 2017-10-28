package aenadon.wienerlinienalarm.models.realtime;

import org.threeten.bp.ZonedDateTime;

import java.util.ArrayList;
import java.util.List;

import aenadon.wienerlinienalarm.models.realtime.json_model.Monitor;
import aenadon.wienerlinienalarm.models.realtime.json_model.RealtimeData;
import aenadon.wienerlinienalarm.models.wl_metadata.Line;

public class RealtimeInfo {

    private String towards;
    private boolean barrierFree;
    private boolean realtimeSupported;
    private boolean trafficJam;

    private List<ZonedDateTime> realtimeDepartures;
    private List<ZonedDateTime> plannedDepartures;

    public String getTowards() {
        return towards;
    }

    public void setTowards(String towards) {
        this.towards = towards;
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

    public boolean isTrafficJam() {
        return trafficJam;
    }

    public void setTrafficJam(boolean trafficJam) {
        this.trafficJam = trafficJam;
    }

    public List<ZonedDateTime> getRealtimeDepartures() {
        return realtimeDepartures;
    }

    public void setRealtimeDepartures(List<ZonedDateTime> realtimeDepartures) {
        this.realtimeDepartures = realtimeDepartures;
    }

    public List<ZonedDateTime> getPlannedDepartures() {
        return plannedDepartures;
    }

    public void setPlannedDepartures(List<ZonedDateTime> plannedDepartures) {
        this.plannedDepartures = plannedDepartures;
    }

    public static List<RealtimeInfo> getAllRealtimeInfo(RealtimeData realtimeData, Line line) {
        // TODO implement
        return null;
    }
}
