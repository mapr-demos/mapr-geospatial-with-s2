package com.mapr.geospatial.sample;

import com.google.common.base.Preconditions;
import com.mapr.geospatial.lib.GPoint;
import com.mapr.geospatial.lib.S2Helper;
import com.mapr.geospatial.sample.entity.Point;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.Connection;
import org.ojai.store.DocumentStore;
import org.ojai.store.DriverManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.apache.commons.codec.CharEncoding.UTF_8;

@Slf4j
public class Proximity {

    private static final String AIRPORTS_TABLE_NAME = "/apps/airports";

    private static final String POINTS_SAMPLE_DATA = "points";

    private static final String DRIVER_NAME = "ojai:mapr:";

    private static final double SEARCH_RADIUS_IN_METERS = 20000;

    private static final ObjectMapper mapper = new ObjectMapper();

    // Create an OJAI connection to MapR cluster
    private static final Connection connection = DriverManager.getConnection(DRIVER_NAME);

    // Get an instance of OJAI
    private static final DocumentStore airports = connection.getStore(AIRPORTS_TABLE_NAME);

    public static void main(String[] args) throws Exception {

        try {
            S2Helper helper = new S2Helper();

            // Insert sample data to db (airports, states)
            File airportsFile
                = getResourceFile(Inclusion.class, POINTS_SAMPLE_DATA);

            insertDataFromFile(connection, airports, airportsFile);

            log.info("All airports that are located at less than {} m from the reservoir in NYC Central Park", SEARCH_RADIUS_IN_METERS);

            GPoint center = new GPoint(40.782865, -73.965355);

            String queryForCircle = helper.getQueryForCircle("cellId", center, SEARCH_RADIUS_IN_METERS);

            List<Point> points = new ArrayList<>();

            DocumentStream stream = airports.findQuery(
                connection.newQuery()
                    .where(queryForCircle)
                    .build()
            );

            for (Document document : stream) {
                Point pointDto = mapper.readValue(document.asJsonString(), Point.class);
                points.add(pointDto);
            }

            log.info("Number of airports: {}", points.size());

            points.stream()
                .map(Point::getValue)
                .forEach(x -> log.info(x.toString()));

        } finally {
            purgeTable(airports);
            airports.close();
            connection.close();
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
