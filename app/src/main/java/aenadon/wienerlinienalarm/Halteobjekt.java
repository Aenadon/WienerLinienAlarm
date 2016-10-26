package aenadon.wienerlinienalarm;

import android.support.annotation.NonNull;

class Halteobjekt implements Comparable<Halteobjekt> {
    // Helper class for populating list view (ArrayList of Halteobjekts)
    private String name;
    private String id;
    private String arrayIndex;

    Halteobjekt(String name, String id, String arrayIndex) {
        this.name = name;
        this.id = id;
        this.arrayIndex = arrayIndex;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getArrayIndex() {
        return arrayIndex;
    }

    @Override
    public int compareTo(@NonNull Halteobjekt h) {
        return this.name.compareTo(h.getName());
    }
}
