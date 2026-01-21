package me.jaime.astromanager.objects;

import me.jaime.astromanager.enums.TypeBody;

public class CelestialBody {

    private String name;
    private String catalogId;
    private double ra;
    private double dec;
    private double magnitude;
    private TypeBody typeBody;

    public CelestialBody(String name, String catalogId, double rightAscension, double declination, double magnitude, TypeBody typeBody) {
        setName(name);
        setCatalogId(catalogId);
        setRA(rightAscension);
        setDEC(declination);
        setMagnitude(magnitude);
        setTypeBody(typeBody);
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public String getName() {
        return name;
    }

    public double getRA() {
        return ra;
    }

    public double getDEC() {
        return dec;
    }

    public String getCatalogId() {
        return catalogId;
    }

    public TypeBody getTypeBody() {
        return typeBody;
    }

    public String getType() {
        return getTypeBody().toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRA(double rightAscension) {
        this.ra = rightAscension;
    }

    public void setDEC(double declination) {
        this.dec = declination;
    }

    public void setTypeBody(TypeBody typeBody) {
        this.typeBody = typeBody;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }
}