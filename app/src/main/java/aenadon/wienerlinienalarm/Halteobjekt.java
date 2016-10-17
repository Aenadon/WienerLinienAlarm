package aenadon.wienerlinienalarm;

class Halteobjekt implements Comparable<Halteobjekt> {
    // Helper class for populating list view (ArrayList of Halteobjekts)
    private String name;
    private String id;

    Halteobjekt(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public int compareTo(Halteobjekt h) {
        return this.name.compareTo(h.getName());
    }
}
