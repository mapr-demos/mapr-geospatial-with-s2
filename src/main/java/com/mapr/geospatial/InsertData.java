package com.mapr.geospatial;

import com.google.common.base.Preconditions;
import com.google.common.geometry.S2LatLngRect;
import com.mapr.geospatial.entity.Point;
import lombok.extern.slf4j.Slf4j;
import org.ojai.store.Connection;
import org.ojai.store.DocumentStore;
import org.ojai.store.DriverManager;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import static com.google.common.geometry.S2LatLng.fromDegrees;
import static org.apache.commons.codec.CharEncoding.UTF_8;

@Slf4j
public class InsertData {

    private static final String AIRPORTS_TABLE_NAME = "/apps/airports";

    private static final String POINTS_SAMPLE_DATA = "points";

    private static final String DRIVER_NAME = "ojai:mapr:";
    private static final Connection connection = DriverManager.getConnection(DRIVER_NAME);


    public static void main(String[] args) throws Exception {

        S2Helper s2Helper = new S2Helper(AIRPORTS_TABLE_NAME, connection);

        try (DocumentStore table = connection.getStore(AIRPORTS_TABLE_NAME)) {

            File pointsFile
                    = getResourceFile(InsertData.class, POINTS_SAMPLE_DATA);

            // Insert sample data to db from /resources/points
            Scanner scanner = new Scanner(pointsFile, UTF_8);
            while (scanner.hasNext()) {
                table.insert(connection.newDocument(scanner.nextLine()));
            }

            S2LatLngRect rect = S2LatLngRect.fromPointPair(
                    fromDegrees(44.984924, -111.044691),
                    fromDegrees(41.003994, -104.057992)
            );

            List<Point> allPointsInRegion
                    = s2Helper.findAllPointsInRegion(rect, ZoomLevel.HIGH);

            log.info(String.valueOf(allPointsInRegion.size()));

            allPointsInRegion.stream()
                    .map(Point::getValue)
                    .forEach(x -> log.info(x.toString()));

        } finally {
            s2Helper.purgeTable();
            s2Helper.close();
            connection.close();
        }
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
