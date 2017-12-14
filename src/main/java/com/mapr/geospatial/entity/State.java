package com.mapr.geospatial.entity;

import java.util.Objects;

public class State {

    private String _id;
    private String name;
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
        State state = (State) o;
        return Objects.equals(_id, state._id) &&
                Objects.equals(name, state.name) &&
                Objects.equals(code, state.code) &&
                Objects.equals(loc, state.loc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, name, code, loc);
    }

    @Override
    public String toString() {
        return "State{" +
                "_id='" + _id + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", loc=" + loc +
                '}';
    }
}
