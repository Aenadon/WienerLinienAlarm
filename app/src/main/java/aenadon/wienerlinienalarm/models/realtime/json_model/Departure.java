package aenadon.wienerlinienalarm.models.realtime.json_model;

class Departure {

    private DepartureTime departureTime;
    private Vehicle vehicle;

    public DepartureTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(DepartureTime departureTime) {
        this.departureTime = departureTime;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}
