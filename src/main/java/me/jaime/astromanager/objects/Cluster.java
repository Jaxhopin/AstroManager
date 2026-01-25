package me.jaime.astromanager.objects;

import me.jaime.astromanager.enums.TypeBody;

public class Cluster extends CelestialBody {


    public Cluster(String name, String catalogId, double rightAscension, double declination, double magnitude) {
        super(name, catalogId, rightAscension, declination, magnitude, TypeBody.CLUSTER);
    }
}
