package me.jaime.astromanager.objects;

import me.jaime.astromanager.enums.TypeBody;

public class Galaxy extends CelestialBody {

    public Galaxy(String name, String catalogId, double rightAscension, double declinacion, double magnitude) {
        super(name, catalogId, rightAscension, declinacion, magnitude, TypeBody.GALAXY);
    }

}
