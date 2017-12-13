package com.mapr.geospatial.entity;

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

        if (_id != null ? !_id.equals(state._id) : state._id != null) return false;
        if (name != null ? !name.equals(state.name) : state.name != null) return false;
        if (code != null ? !code.equals(state.code) : state.code != null) return false;
        return loc != null ? loc.equals(state.loc) : state.loc == null;
    }

    @Override
    public int hashCode() {
        int result = _id != null ? _id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (loc != null ? loc.hashCode() : 0);
        return result;
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
