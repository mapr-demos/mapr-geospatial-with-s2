package com.mapr.geospatial.sample;

import com.google.common.base.Preconditions;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2LatLngRect;
import com.mapr.geospatial.lib.S2Helper;
import com.mapr.geospatial.lib.ZoomLevel;
import com.mapr.geospatial.sample.entity.Point;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.Connection;
import org.ojai.store.DocumentStore;
import org.ojai.store.DriverManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.google.common.geometry.S2LatLng.fromDegrees;
import static org.apache.commons.codec.CharEncoding.UTF_8;

@Slf4j
public class InsertData {

    private static final String AIRPORTS_TABLE_NAME = "/apps/airports";

    private static final String POINTS_SAMPLE_DATA = "points";

    private static final String DRIVER_NAME = "ojai:mapr:";

    private static final ObjectMapper mapper = new ObjectMapper();

    // Create an OJAI connection to MapR cluster
    private static final Connection connection = DriverManager.getConnection(DRIVER_NAME);

    // Get an instance of OJAI
    private static final DocumentStore table = connection.getStore(AIRPORTS_TABLE_NAME);


    public static void main(String[] args) throws Exception {

        S2Helper helper = new S2Helper();

        File pointsFile
            = getResourceFile(InsertData.class, POINTS_SAMPLE_DATA);

        Scanner scanner = new Scanner(pointsFile, UTF_8);

        // Insert sample data to db from /resources/points
        while (scanner.hasNext()) {
            table.insert(connection.newDocument(scanner.nextLine()));
        }

        printDelimeter();
        log.info("Find all airports in the Wyoming");

        // ------------------- Sample 1 - Inclusion -------------------

        S2LatLngRect rect = S2LatLngRect.fromPointPair(
            fromDegrees(44.984924, -111.044691),
            fromDegrees(41.003994, -104.057992)
        );

        List<String> queries = helper.getQueriesForRegion("cellId", rect, ZoomLevel.HIGH);
        //find all cells from range
        List<Point> points = getPoints(queries);

//        log.info(String.valueOf());

        log.info("Number of airports: {}", points.size());
        printDelimeter();

        points.stream()
            .map(Point::getValue)
            .forEach(x -> log.info(x.toString()));

        printDelimeter();
        log.info("All airports that are located at less than 20km from the reservoir in NYC Central Park");

        // ------------------- Sample 1 - Proximity -------------------

        S2LatLng centerPoint = fromDegrees(40.782865, -73.965355);

        List<String> queriesForCircles = helper.getQueriesForCircleSearchRegion("cellId", centerPoint, 20000, ZoomLevel.HIGH);

        List<Point> circle = getPoints(queriesForCircles);

        log.info("Number of airports: {}", circle.size());
        printDelimeter();

        circle.stream()
            .map(Point::getValue)
            .forEach(x -> log.info(x.toString()));

        purgeTable(table);
        table.close();
        connection.close();
    }

    /**
     * Return all corresponding points
     */
    private static List<Point> getPoints(List<String> queries) throws IOException {
        List<Point> points = new ArrayList<>();
        for (String query : queries) {

            DocumentStream stream = table.findQuery(
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
}
