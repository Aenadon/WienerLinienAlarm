package aenadon.wienerlinienalarm.adapter;

import android.support.annotation.NonNull;

public class Halteobjekt implements Comparable<Halteobjekt> {
    // Helper class for populating list view (ArrayList of Halteobjekts)
    private final String name;
    private final String id;
    private final String arrayIndex;

    public Halteobjekt(String name, String id, String arrayIndex) {
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
