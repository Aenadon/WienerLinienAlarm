package aenadon.wienerlinienalarm.models.realtime;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import aenadon.wienerlinienalarm.models.realtime.json_model.Departure;
import aenadon.wienerlinienalarm.models.realtime.json_model.DepartureTime;
import aenadon.wienerlinienalarm.models.realtime.json_model.Departures;
import aenadon.wienerlinienalarm.models.realtime.json_model.JsonLine;
import aenadon.wienerlinienalarm.models.realtime.json_model.Monitor;
import aenadon.wienerlinienalarm.models.realtime.json_model.RealtimeData;
import aenadon.wienerlinienalarm.models.realtime.json_model.Vehicle;
import aenadon.wienerlinienalarm.models.wl_metadata.Line;
import trikita.log.Log;

public class SteigDeparture {

    private String towards;
    private boolean barrierFree;
    private boolean realtimeSupported;
    private boolean trafficJam;

    private ZonedDateTime realtimeDeparture;
    private ZonedDateTime plannedDeparture;

    public String getTowards() {
        return towards;
    }

    private void setTowards(String towards) {
        this.towards = towards;
    }

    public boolean isBarrierFree() {
        return barrierFree;
    }

    private void setBarrierFree(boolean barrierFree) {
        this.barrierFree = barrierFree;
    }

    public boolean isRealtimeSupported() {
        return realtimeSupported;
    }

    private void setRealtimeSupported(boolean realtimeSupported) {
        this.realtimeSupported = realtimeSupported;
    }

    public boolean isTrafficJam() {
        return trafficJam;
    }

    private void setTrafficJam(boolean trafficJam) {
        this.trafficJam = trafficJam;
    }

    public ZonedDateTime getRealtimeDeparture() {
        return realtimeDeparture;
    }

    private void setRealtimeDeparture(ZonedDateTime realtimeDeparture) {
        this.realtimeDeparture = realtimeDeparture;
    }

    public ZonedDateTime getPlannedDeparture() {
        return plannedDeparture;
    }

    private void setPlannedDeparture(ZonedDateTime plannedDeparture) {
        this.plannedDeparture = plannedDeparture;
    }

    public static List<SteigDeparture> getDepartures(RealtimeData realtimeData, Line line) {
        List<SteigDeparture> steigDepartures = new ArrayList<>();
        List<Monitor> monitors = realtimeData.getData().getMonitors();

        Monitor monitorForCorrectLine = null;
        for (Monitor monitor : monitors) {
            List<JsonLine> jsonLines = monitor.getLines();
            for (JsonLine jsonLine : jsonLines) {
                if (jsonLine.getName().equals(line.getLineName())) {
                    monitorForCorrectLine = monitor;
                    break;
                }
            }
        }
        if (monitorForCorrectLine == null) {
            Log.e("Monitor for correct line was not found!");
            return new ArrayList<>();
        }
        JsonLine selectedJsonLine = monitorForCorrectLine.getLines().get(0);
        List<Departure> jsonDepartures = selectedJsonLine.getDepartures().getDeparture();

        for (Departure jsonDeparture : jsonDepartures) {
            SteigDeparture steigDeparture = new SteigDeparture();

            Vehicle vehicle = jsonDeparture.getVehicle();
            if (vehicle == null) {
                steigDeparture.setTowards(selectedJsonLine.getTowards());
                steigDeparture.setBarrierFree(selectedJsonLine.isBarrierFree());
                steigDeparture.setRealtimeSupported(selectedJsonLine.isRealtimeSupported());
                steigDeparture.setTrafficJam(selectedJsonLine.isTrafficJam());
            } else {
                steigDeparture.setTowards(vehicle.getTowards());
                steigDeparture.setBarrierFree(vehicle.isBarrierFree());
                steigDeparture.setRealtimeSupported(vehicle.isRealtimeSupported());
                steigDeparture.setTrafficJam(vehicle.isTrafficJam());
            }

            DepartureTime departureTime = jsonDeparture.getDepartureTime();
            ZonedDateTime timePlanned = ZonedDateTime.parse(departureTime.getTimePlanned(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            ZonedDateTime timeReal = ZonedDateTime.parse(departureTime.getTimeReal(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            steigDeparture.setPlannedDeparture(timePlanned);
            steigDeparture.setRealtimeDeparture(timeReal);

            steigDepartures.add(steigDeparture);
        }
        return steigDepartures;
    }
}
