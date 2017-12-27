package com.mapr.geospatial;

import com.google.common.base.Preconditions;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import org.ojai.Document;
import org.ojai.store.Connection;
import org.ojai.store.DocumentStore;
import org.ojai.store.DriverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Scanner;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

public class InsertData {

    private static final Logger log = LoggerFactory.getLogger(InsertData.class);

    private static final String POINTS_TABLE_NAME = "/apps/points";

    private static final String DRIVER_NAME = "ojai:mapr:";

    private static final String POINTS_SAMPLE_DATA = "points";

    public static void main(String[] args) throws Exception {

        // Create an OJAI connection to MapR cluster
        final Connection connection = DriverManager.getConnection(DRIVER_NAME);

        // Get an instance of airports storage
        final DocumentStore points = connection.getStore(POINTS_TABLE_NAME);

        try {

            File pointsFile
                    = getResourceFile(InsertData.class, POINTS_SAMPLE_DATA);

            insertDataFromFile(connection, points, pointsFile);

            printAllTables(points);

        } finally {
            // remove all data from db
            purgeTable(points);
            points.close();
            connection.close();
        }
    }

    private static Long getCellIdFromDegrees(double lat, double lon) {
        S2LatLng point = S2LatLng.fromDegrees(lat, lon);
        return S2CellId.fromLatLng(point).id();
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
     * Returns file from resources folder
     */
    private static File getResourceFile(Class clazz, String fileName) {
        ClassLoader classLoader = clazz.getClassLoader();
        URL coordinatesUrl = classLoader.getResource(fileName);
        Preconditions.checkNotNull(coordinatesUrl, "Cannot find file " + fileName);
        return new File(coordinatesUrl.getFile());
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
}
