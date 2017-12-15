package com.mapr.geospatial;

import com.google.common.base.Preconditions;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2PolygonBuilder;
import com.mapr.geospatial.entity.Airport;
import com.mapr.geospatial.entity.Coordinate;
import com.mapr.geospatial.entity.State;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.Connection;
import org.ojai.store.DocumentStore;
import org.ojai.store.DriverManager;
import org.ojai.store.Query;
import org.ojai.store.QueryCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

public class InsertData {

    private static final Logger log = LoggerFactory.getLogger(InsertData.class);

    private static final String AIRPORTS_TABLE_NAME = "/apps/airports";
    private static final String STATES_TABLE_NAME = "/apps/states";

    private static final String DRIVER_NAME = "ojai:mapr:";

    private static final String AIRPORTS_SAMPLE_DATA = "airports";
    private static final String STATES_SAMPLE_DATA = "states";

    private static final String LOOKED_STATE = "WY";

    public static void main(String[] args) throws Exception {

        // Create an OJAI connection to MapR cluster
        final Connection connection = DriverManager.getConnection(DRIVER_NAME);

        // Get an instance of airports storage
        final DocumentStore airports = connection.getStore(AIRPORTS_TABLE_NAME);

        // Get an instance of states storage
        final DocumentStore states = connection.getStore(STATES_TABLE_NAME);

        try {

            File airportsFile
                    = getResourceFile(InsertData.class, AIRPORTS_SAMPLE_DATA);

            insertDataFromFile(connection, airports, airportsFile);

            File statesFile =
                    getResourceFile(InsertData.class, STATES_SAMPLE_DATA);

            insertDataFromFile(connection, states, statesFile);

            final Query query = connection.newQuery()
                    .where(
                            connection.newCondition()
                                    .is("code", QueryCondition.Op.EQUAL, LOOKED_STATE)   // Build an OJAI QueryCondition
                                    .build())
                    .limit(1)
                    .build();

            DocumentStream statesDocs = states.findQuery(query);

            // Inclusion

            State state
                    = statesDocs.iterator().next().toJavaBean(State.class);

            S2Polygon statePolygon = createPolygonFromCoordinates(state);

            List<Airport> resAirports
                    = getAllAirportsFromState(airports.find(), statePolygon);

            log.info("Quantity of airports in the {} : {}", LOOKED_STATE, resAirports.size());
            log.info(Arrays.toString(resAirports.toArray()));

            // Intersection

            List<String> intersectedStates
                    = getAllIntersectedStatesWith(statePolygon, states);

            log.info("Intersected states with {}: {}", LOOKED_STATE, Arrays.toString(intersectedStates.toArray()));

//            printAllTables(airports);
//            printAllTables(states);

        } finally {
            // remove all data from db
            purgeTable(airports);
            purgeTable(states);

            airports.close();
            states.close();

            connection.close();
        }
    }

    /**
     * Returns all states that intersected with state
     */
    private static List<String> getAllIntersectedStatesWith(S2Polygon statePolygon, DocumentStore states) {
        List<String> intersectedStates = new ArrayList<>();

        for (Document document : states.find()) {
            State tmp = document.toJavaBean(State.class);
            S2Polygon tmpPolygon = createPolygonFromCoordinates(tmp);

            if (statePolygon.intersects(tmpPolygon)) {
                intersectedStates.add(tmp.getName());
            }
        }
        return intersectedStates;
    }

    /**
     * Returns list of airports that belongs to the state
     */
    private static List<Airport> getAllAirportsFromState(DocumentStream airports, S2Polygon statePolygon) {
        List<Airport> resAirports = new ArrayList<>();

        for (Document doc : airports) {
            Airport airport = doc.toJavaBean(Airport.class);

            Coordinate airportCoordinate
                    = airport.getLoc().getCoordinates().get(0);

            S2Point airportPoint
                    = S2LatLng.fromDegrees(airportCoordinate.getLatitude(), airportCoordinate.getLongitude()).toPoint();

            if (statePolygon.contains(airportPoint)) {
                resAirports.add(airport);
            }
        }

        return resAirports;
    }

    /**
     * Creates polygon representation of the state
     */
    private static S2Polygon createPolygonFromCoordinates(State state) {
        S2PolygonBuilder polygonBuilder = new S2PolygonBuilder();

        Iterator<Coordinate> it
                = state.getLoc().getCoordinates().iterator();

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

    /**
     * Prints into log all the documents from storage
     */
    private static void printAllTables(DocumentStore store) {
        log.info("All records from db:");
        for (Document entries : store.find()) {
            log.info(entries.toString());
        }
    }

    /**
     * Reads a file and insert data to db. One line in file is one insert to db.
     * Data in the file must be in json format.
     */
    private static void insertDataFromFile(Connection connection,
                                           DocumentStore store, File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file, UTF_8);
        while (scanner.hasNext()) {
            store.insert(connection.newDocument(scanner.nextLine()));
        }
    }

    /**
     * Deletes all data from storage
     */
    private static void purgeTable(DocumentStore store) {
        for (Document userDocument : store.find()) {
            store.delete(userDocument.getId());
        }
        store.flush();
    }

    /**
     * Returns file from resources folder
     */
    private static File getResourceFile(Class clazz, String fileName) {
        ClassLoader classLoader = clazz.getClassLoader();
        URL coordinatesUrl = classLoader.getResource(fileName);
        Preconditions.checkNotNull(coordinatesUrl, "Cannot find file " + fileName);
        return new File(coordinatesUrl.getFile());
    }
}
