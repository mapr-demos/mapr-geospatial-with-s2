package com.mapr.geospatial.entity;

import java.util.List;

public class Location {

    private String type;
    private List<Coordinate> coordinates;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (type != null ? !type.equals(location.type) : location.type != null) return false;
        return coordinates != null ? coordinates.equals(location.coordinates) : location.coordinates == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (coordinates != null ? coordinates.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Location{" +
                "type='" + type + '\'' +
                ", airports=" + coordinates +
                '}';
    }
}
