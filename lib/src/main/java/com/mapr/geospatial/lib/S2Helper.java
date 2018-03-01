package com.mapr.geospatial.lib;

import com.google.common.geometry.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.geometry.S2.M_PI;
import static com.google.common.geometry.S2LatLng.fromDegrees;

public class S2Helper {

    private final static Integer ZOOM_LEVEL = 25;

    /**
     * Generate query for searching points in the Rectangle region
     *
     * @param columnName name of the column which contains cell Id
     * @return query for searching the points in the region
     */
    public String getQueryForRectangle(String columnName, GPoint lowerLeft, GPoint upperRight) {
        S2LatLngRect rect = S2LatLngRect.fromPointPair(
            fromDegrees(lowerLeft.getLatitude(), lowerLeft.getLongitude()),
            fromDegrees(upperRight.getLatitude(), upperRight.getLongitude())
        );
        List<S2CellId> cellIds = getS2CellIds(rect, ZOOM_LEVEL);
        return generateQueries(columnName, cellIds);
    }

    /**
     * Generate query for searching points in the circle region with determined radius
     *
     * @param columnName  name of the column which contains cell Id
     * @param centerPoint coordinate of the center of the circle search zone
     * @param radius      the radius of the search zone in meters
     * @return query for searching the points in the region
     */
    public String getQueryForCircle(String columnName, GPoint centerPoint, double radius) {
        S2LatLng center = fromDegrees(centerPoint.getLatitude(), centerPoint.getLongitude());
        double radius_radians = earthMetersToRadians(radius);
        S2Cap region = S2Cap.fromAxisHeight(
            center.normalized().toPoint(),
            (radius_radians * radius_radians) / 2);
        List<S2CellId> cellIds = getS2CellIds(region, ZOOM_LEVEL);
        return generateQueries(columnName, cellIds);
    }

    /**
     * Generate query for searching points in the circle region with determined radius
     *
     * @param columnName name of the column which contains cell Id
     * @param points     coordinates of the polygon
     * @return query for searching the points in the region
     */
    public String getQueryForPolygon(String columnName, List<GPoint> points) {
        S2Polygon polygon = createPolygon(points);
        List<S2CellId> cellIds = getS2CellIds(polygon, ZOOM_LEVEL);
        return generateQueries(columnName, cellIds);
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

    private static S2Polygon createPolygon(List<GPoint> points) {
        S2PolygonBuilder polygonBuilder = new S2PolygonBuilder();
        polygonBuilder.addPolygon(createPolygonFromCoordinates(points));
        return polygonBuilder.assemblePolygon();
    }

    /**
     * Creates polygon representation of the state based on a coordinates
     */
    private static S2Polygon createPolygonFromCoordinates(List<GPoint> coordinates) {
        S2PolygonBuilder polygonBuilder = new S2PolygonBuilder();

        Iterator<GPoint> it = coordinates.iterator();

        GPoint first = it.next();
        GPoint previous = first;
        GPoint current = first;
        while (it.hasNext()) {
            current = it.next();
            S2Point previousPoint
                = S2LatLng.fromDegrees(previous.getLatitude(), previous.getLongitude()).toPoint();
            S2Point currentPoint
                = S2LatLng.fromDegrees(current.getLatitude(), current.getLongitude()).toPoint();

            polygonBuilder.addEdge(previousPoint, currentPoint);
            previous = current;
        }

        S2Point lastPoint
            = S2LatLng.fromDegrees(current.getLatitude(), current.getLongitude()).toPoint();
        S2Point firstPoint
            = S2LatLng.fromDegrees(first.getLatitude(), first.getLongitude()).toPoint();

        polygonBuilder.addEdge(lastPoint, firstPoint);

        return polygonBuilder.assemblePolygon();
    }

    private String generateQueries(String columnName, List<S2CellId> cellIds) {
        String query = "{\"$or\": [";
        for (S2CellId cellId : cellIds) {
            Long bmin = cellId.rangeMin().id();
            Long bmax = cellId.rangeMax().id();

            query += "{\"$and\": [{\"$ge\":{\"" + columnName + "\":" + bmin + "}}," +
                "{\"$le\":{\"" + columnName + "\":" + bmax + "}}]},";
        }
        return query.substring(0, query.length() - 1) + "]}";
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
