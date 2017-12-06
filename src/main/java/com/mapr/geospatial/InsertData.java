package com.mapr.geospatial;

import com.google.common.base.Preconditions;
import com.mapr.geospatial.entity.Coordinate;
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
import java.util.Scanner;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

public class InsertData {

    private static final Logger log = LoggerFactory.getLogger(InsertData.class);

    private static final String TABLE_NAME = "/apps/geo_data";
    private static final String DRIVER_NAME = "ojai:mapr:";

    private static final String SAMPLE_DATA_FILE_NAME = "coordinates";

    public static void main(String[] args) throws Exception {

        // Create an OJAI connection to MapR cluster
        final Connection connection = DriverManager.getConnection(DRIVER_NAME);

        // Get an instance of OJAI
        final DocumentStore store = connection.getStore(TABLE_NAME);

        insertDataFromRes(connection, store);

        for (Document entries : store.find()) {
            log.info(entries.asJsonString());
        }

        Query query = connection.newQuery()
                .where(
                        connection.newCondition()
                                .is("latitude", QueryCondition.Op.EQUAL, 37.4185099)
                                .build())
                .build();

        Coordinate coordinate = null;

        for (Document entries : store.findQuery(query)) {
            coordinate = entries.toJavaBean(Coordinate.class);
            log.info(coordinate.toString());
        }

        assert coordinate != null;

        Geo geo =
                new Geo(coordinate.getLatitude(), coordinate.getLongitude());

        purgeTable(store);

        store.close();
        connection.close();
    }

    private static void insertDataFromRes(Connection connection,
                                          DocumentStore store) throws FileNotFoundException {
        File file = getResourceFile(InsertData.class);
        Scanner scanner = new Scanner(file, UTF_8);

        while (scanner.hasNext()) {
            store.insert(connection.newDocument(scanner.nextLine()));
        }
    }

    private static void purgeTable(DocumentStore store) {
        DocumentStream streamMasterDocs = store.find();
        for (Document userDocument : streamMasterDocs) {
            store.delete(userDocument.getId());
        }
        store.flush();
    }

    private static File getResourceFile(Class clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        URL coordinatesUrl = classLoader.getResource(SAMPLE_DATA_FILE_NAME);
        Preconditions.checkNotNull(coordinatesUrl, "Cannot find file " + SAMPLE_DATA_FILE_NAME);

        return new File(coordinatesUrl.getFile());
    }
}
