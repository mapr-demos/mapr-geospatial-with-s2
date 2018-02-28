package com.mapr.geospatial.lib;

import com.google.common.geometry.*;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.geometry.S2.M_PI;

public class S2Helper {

    /**
     * Generate queryies for searching points in the region
     *
     * @param columnName name of the column which contains cell Id
     * @param region     place in which to look for points
     * @param level      the level of search accuracy
     * @return List of queries for searching the points in the region
     */
    public List<String> getQueriesForRegion(String columnName, S2Region region, ZoomLevel level) {
        List<S2CellId> cellIds = getS2CellIds(region, level.getLevel());
        return generateQueries(columnName, cellIds);
    }

    /**
     * Generate queries for searching points in the circle region with determined radius
     *
     * @param columnName  name of the column which contains cell Id
     * @param centerPoint сoordinates of the center of the circle search zone
     * @param radius      the radius of the search zone in meters
     * @param level       the level of search accuracy
     * @return List of queries for searching the points in the circle region
     */
    public List<String> getQueriesForCircleSearchRegion(String columnName, S2LatLng centerPoint, double radius, ZoomLevel level) {
        double radius_radians = earthMetersToRadians(radius);
        S2Cap region = S2Cap.fromAxisHeight(
            centerPoint.normalized().toPoint(),
            (radius_radians * radius_radians) / 2);
        return getQueriesForRegion(columnName, region, level);
    }

    /**
     * Generate cell Id based on the latitude/longitude degrees
     *
     * @param lat latitude in degrees
     * @param lng longitude in degrees
     * @return cellId long representation
     */
    public Long generateCellIdFromDegrees(double lat, double lng) {
        return S2CellId.fromLatLng(S2LatLng.fromDegrees(lat, lng)).id();
    }

    private List<String> generateQueries(String columnName, List<S2CellId> cellIds) {
        List<String> queries = new ArrayList<>();
        for (S2CellId cellId : cellIds) {
            Long bmin = cellId.rangeMin().id();
            Long bmax = cellId.rangeMax().id();

            String query =
                "{\"$and\": " +
                    "[" +
                    "{\"$ge\":{\"" + columnName + "\":" + bmin + "}}," +
                    "{\"$le\":{\"" + columnName + "\":" + bmax + "}}" +
                    "]}";
            queries.add(query);
        }
        return queries;
    }

    private ArrayList<S2CellId> getS2CellIds(S2Region region, int level) {
        ArrayList<S2CellId> covering = new ArrayList<>();
        S2RegionCoverer coverer = new S2RegionCoverer();
        coverer.setLevelMod(level);
        coverer.getCovering(region, covering);
        return covering;
    }

    private double earthMetersToRadians(double meters) {
        double kEarthCircumferenceMeters = 1000 * 40075.017;
        return (2 * M_PI) * (meters / kEarthCircumferenceMeters);
    }
}
