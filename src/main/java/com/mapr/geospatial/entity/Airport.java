package com.mapr.geospatial.entity;

import java.util.Objects;

public class Airport {

    private String _id;
    private String name;
    private String type;
    private String code;
    private Location loc;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airport airport = (Airport) o;
        return Objects.equals(_id, airport._id) &&
                Objects.equals(name, airport.name) &&
                Objects.equals(type, airport.type) &&
                Objects.equals(code, airport.code) &&
                Objects.equals(loc, airport.loc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, name, type, code, loc);
    }

    @Override
    public String toString() {
        return "Airport{" +
                "_id='" + _id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", code='" + code + '\'' +
                ", loc=" + loc +
                '}';
    }
}
