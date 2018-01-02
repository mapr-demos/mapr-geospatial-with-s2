package com.mapr.geospatial;

public enum ZoomLevel {

    HIGH(25),
    MEDIUM(15),
    LOW(5);

    private int levelCode;

    ZoomLevel(int levelCode) {
        this.levelCode = levelCode;
    }

    public int getLevel() {
        return levelCode;
    }
}
