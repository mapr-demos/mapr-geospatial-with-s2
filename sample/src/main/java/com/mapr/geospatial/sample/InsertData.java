package com.mapr.geospatial.sample;

import com.google.common.base.Preconditions;
import com.google.common.geometry.*;
import com.mapr.geospatial.lib.S2Helper;
import com.mapr.geospatial.lib.ZoomLevel;
import com.mapr.geospatial.sample.entity.Coordinate;
import com.mapr.geospatial.sample.entity.Location;
import com.mapr.geospatial.sample.entity.Point;
import com.mapr.geospatial.sample.entity.State;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import static com.google.common.geometry.S2LatLng.fromDegrees;
import static org.apache.commons.codec.CharEncoding.UTF_8;

@Slf4j
public class InsertData {

    private static final String AIRPORTS_TABLE_NAME = "/apps/airports";
    private static final String STATES_TABLE_NAME = "/apps/states";

    private static final String POINTS_SAMPLE_DATA = "points";
    private static final String STATES_SAMPLE_DATA = "states";

    // Set the state in which you want to find airports
    // Available variants:
    // WY, PA, OH, NM, MD, OR, WI, ND, NV, GA, AR, KS, NE, UT, MS, OK, WV, MI, CO, NG
    // WA, CT, MA, ID, MO, AL, SC, NH, SD, IL, TN, IN, IA, AZ, MN, DC, VA, TX, VT, DE, MT
    private static final String LOOKED_STATE = "NV";

    private static final double SEARCH_RADIUS_IN_METERS = 20000;

    private static final String DRIVER_NAME = "ojai:mapr:";

    private static final ObjectMapper mapper = new ObjectMapper();

    // Create an OJAI connection to MapR cluster
    private static final Connection connection = DriverManager.getConnection(DRIVER_NAME);

    // Get an instance of OJAI
    private static final DocumentStore airports = connection.getStore(AIRPORTS_TABLE_NAME);

    // Get an instance of states storage
    private static final DocumentStore states = connection.getStore(STATES_TABLE_NAME);

    public static void main(String[] args) throws Exception {
        try {
            S2Helper helper = new S2Helper();

            // Insert sample data to db (airports, states)
            File airportsFile
                = getResourceFile(InsertData.class, POINTS_SAMPLE_DATA);

            insertDataFromFile(connection, airports, airportsFile);

            File statesFile =
                getResourceFile(InsertData.class, STATES_SAMPLE_DATA);

            insertDataFromFile(connection, states, statesFile);

            printDelimeter();

            // ------------------- Sample 1 - Find in Rectangle area -------------------

            log.info("Find all airports in the Wyoming");

            S2LatLngRect rect = S2LatLngRect.fromPointPair(
                fromDegrees(44.984924, -111.044691),
                fromDegrees(41.003994, -104.057992)
            );

            List<String> queries = helper.getQueriesForRegion("cellId", rect, ZoomLevel.HIGH);
            //find all cells from range
            List<Point> points = getPoints(queries);

            log.info("Number of airports: {}", points.size());
            printDelimeter();

            points.stream()
                .map(Point::getValue)
                .forEach(x -> log.info(x.toString()));

            printDelimeter();

            // ------------------- Sample 2 - Find in Circle area -------------------

            log.info("All airports that are located at less than {} m from the reservoir in NYC Central Park", SEARCH_RADIUS_IN_METERS);

            S2LatLng centerPoint = fromDegrees(40.782865, -73.965355);

            List<String> queriesForCircles = helper.getQueriesForCircleSearchRegion("cellId", centerPoint, SEARCH_RADIUS_IN_METERS, ZoomLevel.HIGH);

            List<Point> circle = getPoints(queriesForCircles);

            log.info("Number of airports: {}", circle.size());

            printDelimeter();

            circle.stream()
                .map(Point::getValue)
                .forEach(x -> log.info(x.toString()));

            printDelimeter();

            // ------------------- Sample 3 - Find in Polygon region -------------------

            log.info("Find all airports in the {}", LOOKED_STATE);

            final Query query = connection.newQuery()
                .where(
                    connection.newCondition()
                        .is("code", QueryCondition.Op.EQUAL, LOOKED_STATE)
                        .build())
                .limit(1)
                .build();

            DocumentStream statesDocs = states.findQuery(query);

            State state
                = statesDocs.iterator().next().toJavaBean(State.class);

            S2Polygon statePolygon = createPolygon(state.getLoc());

            List<String> queriesForPolygon = helper.getQueriesForRegion("cellId", statePolygon, ZoomLevel.HIGH);

            List<Point> pointsInPolygon = getPoints(queriesForPolygon);

            log.info("Number of airports in state {} : {}", LOOKED_STATE, pointsInPolygon.size());

            printDelimeter();

            pointsInPolygon.stream()
                .map(Point::getValue)
                .forEach(x -> log.info(x.toString()));

        } finally {
            purgeTable(airports);
            purgeTable(states);
            airports.close();
            states.close();
            connection.close();
        }
    }

    /**
     * Return all corresponding points
     */
    private static List<Point> getPoints(List<String> queries) throws IOException {
        List<Point> points = new ArrayList<>();
        for (String query : queries) {

            DocumentStream stream = airports.findQuery(
                connection.newQuery()
                    .where(query)
                    .build()
            );

            for (Document document : stream) {
                Point pointDto = mapper.readValue(document.asJsonString(), Point.class);
                points.add(pointDto);
            }
        }
        return points;
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
    private static void purgeTable(DocumentStore table) {
        for (Document userDocument : table.find()) {
            table.delete(userDocument.getId());
        }
        table.flush();
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

    private static void printDelimeter() {
        log.info("\n__*************************************************************************__\n");
    }

    /**
     * Creates polygon representation of the state based on a location
     */
    private static S2Polygon createPolygon(Location location) {
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
}
