package com.mapr.geospatial;

import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2PolygonBuilder;
import com.mapr.geospatial.entity.Airport;
import com.mapr.geospatial.entity.Coordinate;
import com.mapr.geospatial.entity.Location;
import com.mapr.geospatial.entity.State;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.DocumentStore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class GeoDbUtils {

    /**
     * Returns all states that intersected with state
     */
    static List<String> getAllIntersectedStatesWith(S2Polygon statePolygon, DocumentStore states) {
        List<String> intersectedStates = new ArrayList<>();

        for (Document document : states.find()) {
            State tmp = document.toJavaBean(State.class);
            S2Polygon tmpPolygon = createPolygon(tmp.getLoc());

            if (statePolygon.intersects(tmpPolygon)) {
                intersectedStates.add(tmp.getName());
            }
        }
        return intersectedStates;
    }

    /**
     * Returns list of airports that belongs to the state
     */
    static List<Airport> getAllAirportsFromState(DocumentStream airports, S2Polygon statePolygon) {
        List<Airport> resAirports = new ArrayList<>();

        for (Document doc : airports) {
            Airport airport = doc.toJavaBean(Airport.class);

            if (isPoint(airport)) {

                Coordinate airportCoordinate
                        = getCoordinateFrom(airport);

                S2Point airportPoint
                        = S2LatLng.fromDegrees(airportCoordinate.getLatitude(), airportCoordinate.getLongitude()).toPoint();

                if (statePolygon.contains(airportPoint)) {
                    resAirports.add(airport);
                }
            }
        }

        return resAirports;
    }

    /**
     * Returns Coordinates of the airport
     */
    private static Coordinate getCoordinateFrom(Airport airport) {
        return airport.getLoc().getCoordinates().get(0).get(0);
    }

    /**
     * Checks type of the Coordinates
     */
    private static boolean isPoint(Airport airport) {
        return airport.getLoc().getType().equals("Point");
    }

    /**
     * Creates polygon representation of the state based on a location
     */
    static S2Polygon createPolygon(Location location) {
        S2PolygonBuilder polygonBuilder = new S2PolygonBuilder();

        for (List<Coordinate> coordinates : location.getCoordinates()) {
            polygonBuilder.addPolygon(createPolygonFromCoordinates(coordinates));
        }

        return polygonBuilder.assemblePolygon();
    }

    /**
     * Creates polygon representation of the state based on a coordinates
     */
    private static S2Polygon createPolygonFromCoordinates(List<Coordinate> coordinates) {
        S2PolygonBuilder polygonBuilder = new S2PolygonBuilder();

        Iterator<Coordinate> it = coordinates.iterator();

        Coordinate first = it.next();
        Coordinate previous = first;
        Coordinate current = first;
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

    static List<Airport> getNearAirports(DocumentStream airports, S2LatLng point, double distance) {
        List<Airport> result = new ArrayList<>();

        for (Document doc : airports) {
            Airport airport = doc.toJavaBean(Airport.class);
            if (isPoint(airport)) {
                Coordinate airportCoordinate
                        = getCoordinateFrom(airport);
                S2LatLng s2LatLng
                        = S2LatLng.fromDegrees(airportCoordinate.getLatitude(), airportCoordinate.getLongitude());

                if (point.getEarthDistance(s2LatLng) <= distance) {
                    result.add(airport);
                }
            }
        }
        return result;
    }

    /**
     * Deletes all data from storage
     */
    static void purgeTable(DocumentStore store) {
        for (Document userDocument : store.find()) {
            store.delete(userDocument.getId());
        }
        store.flush();
    }
}
