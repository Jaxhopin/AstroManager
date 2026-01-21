package me.jaime.astromanager.enums;

public enum TypeBody {

    GALAXY("Galaxy"),
    NEBULA("Nebula"),
    CLUSTER("Cluster");

    private final String displayName;

    TypeBody(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
