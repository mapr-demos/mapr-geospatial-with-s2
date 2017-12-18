package com.mapr.geospatial.entity;

import java.util.List;
import java.util.Objects;

public class Location {

    private String type;
    private List<List<Coordinate>> coordinates;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<List<Coordinate>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Coordinate>> coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(type, location.type) &&
                Objects.equals(coordinates, location.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, coordinates);
    }

    @Override
    public String toString() {
        return "Location{" +
                "type='" + type + '\'' +
                ", coordinates=" + coordinates +
                '}';
    }
}
