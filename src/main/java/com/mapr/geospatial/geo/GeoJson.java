package com.mapr.geospatial.geo;

public class GeoJson {

    private String _id;
    private String name;
    private String type;
    private double latitude;
    private double longitude;


    public String get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeoJson geoJson = (GeoJson) o;

        if (Double.compare(geoJson.latitude, latitude) != 0) return false;
        if (Double.compare(geoJson.longitude, longitude) != 0) return false;
        if (_id != null ? !_id.equals(geoJson._id) : geoJson._id != null) return false;
        if (name != null ? !name.equals(geoJson.name) : geoJson.name != null) return false;
        return type != null ? type.equals(geoJson.type) : geoJson.type == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = _id != null ? _id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "GeoJson{" +
                "_id='" + _id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
