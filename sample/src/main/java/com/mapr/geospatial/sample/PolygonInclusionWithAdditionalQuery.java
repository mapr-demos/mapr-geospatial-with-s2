package com.mapr.geospatial.sample;

import com.google.common.base.Preconditions;
import com.mapr.geospatial.lib.GPoint;
import com.mapr.geospatial.lib.S2Helper;
import com.mapr.geospatial.sample.entity.Coordinate;
import com.mapr.geospatial.sample.entity.Point;
import com.mapr.geospatial.sample.entity.State;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.apache.commons.codec.CharEncoding.UTF_8;

@Slf4j
public class PolygonInclusionWithAdditionalQuery {

    private static final String AIRPORTS_TABLE_NAME = "/apps/airports";
    private static final String STATES_TABLE_NAME = "/apps/states";

    private static final String POINTS_SAMPLE_DATA = "points";
    private static final String STATES_SAMPLE_DATA = "states";

    // Set the state in which you want to find airports
    // Available variants:
    // WY, PA, OH, NM, MD, OR, WI, ND, NV, GA, AR, KS, NE, UT, MS, OK, WV, MI, CO, NG
    // WA, CT, MA, ID, MO, AL, SC, NH, SD, IL, TN, IN, IA, AZ, MN, DC, VA, TX, VT, DE, MT
    private static final String LOOKED_STATE = "MI";

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
                = getResourceFile(PolygonInclusionWithAdditionalQuery.class, POINTS_SAMPLE_DATA);

            insertDataFromFile(connection, airports, airportsFile);

            File statesFile =
                getResourceFile(PolygonInclusionWithAdditionalQuery.class, STATES_SAMPLE_DATA);

            insertDataFromFile(connection, states, statesFile);

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

            List<GPoint> points = convertCoordinatesToGPoints(state.getLoc().getCoordinates().get(0));

            String queryForPolygon = helper.getQueryForPolygon("cellId", points);


            List<Point> airportsPoints = new ArrayList<>();
            DocumentStream stream = airports.findQuery(
                connection.newQuery()
                    .where(queryForPolygon)
                    .where(
                        connection.newCondition()
                            .is("value.type", QueryCondition.Op.EQUAL, "International")
                            .build())
                    .build()
            );

            for (Document document : stream) {
                Point pointDto = mapper.readValue(document.asJsonString(), Point.class);
                airportsPoints.add(pointDto);
            }

            log.info("Number of airports in state {} : {}", LOOKED_STATE, airportsPoints.size());

            airportsPoints.stream()
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

    private static List<GPoint> convertCoordinatesToGPoints(List<Coordinate> coordinates) {
        List<GPoint> points = new ArrayList<>();
        for (Coordinate point : coordinates) {
            points.add(new GPoint(point.getLatitude(), point.getLongitude()));
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
        store.flush();
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
}
