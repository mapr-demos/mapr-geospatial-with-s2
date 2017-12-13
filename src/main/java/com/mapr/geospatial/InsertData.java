package com.mapr.geospatial;

import com.google.common.base.Preconditions;
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

    private static final String AIRPORTS_TABLE_NAME = "/apps/airports";
    private static final String STATES_TABLE_NAME = "/apps/states";

    private static final String DRIVER_NAME = "ojai:mapr:";

    private static final String AIRPORTS_SAMPLE_DATA = "airports";
    private static final String STATES_SAMPLE_DATA = "states";

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

            printAllTables(airports);
            printAllTables(states);

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
     * Prints into log all the documents from storage
     */
    private static void printAllTables(DocumentStore store) {
        log.info("All records from db:");
        for (Document entries : store.find()) {
            log.info(entries.toString());
        }
    }

    /**
     * Read a file and insert data to db. One line in file is one insert to db.
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
     * Return file from resources folder
     */
    private static File getResourceFile(Class clazz, String fileName) {
        ClassLoader classLoader = clazz.getClassLoader();
        URL coordinatesUrl = classLoader.getResource(fileName);
        Preconditions.checkNotNull(coordinatesUrl, "Cannot find file " + fileName);
        return new File(coordinatesUrl.getFile());
    }
}
