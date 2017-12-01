package com.mapr.geospatial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.geometry.S2LatLng;
import com.mapr.geospatial.geo.GeoJson;
import org.ojai.Document;
import org.ojai.store.Connection;
import org.ojai.store.DocumentStore;
import org.ojai.store.DriverManager;
import org.ojai.store.Query;
import org.ojai.store.QueryCondition;

import java.util.UUID;

public class InsertData {
    public static void main(String[] args) throws JsonProcessingException {

        // Create an OJAI connection to MapR cluster
        final Connection connection = DriverManager.getConnection("ojai:mapr:");

        // Get an instance of OJAI
        final DocumentStore store = connection.getStore("/apps/geo_data");

        GeoJson geo = createGeoJson();
//        store.insert(connection.newDocument(convertToJson(geo)));

        for (Document entries : store.find()) {
            System.out.println(entries.asJsonString());
        }

        Query query = connection.newQuery()
                .where(connection.newCondition().is("latitude", QueryCondition.Op.GREATER_OR_EQUAL, -108.01621).build())
                .build();

        GeoJson geoJson = null;

        for (Document entries : store.findQuery(query)) {
//            System.out.println(entries.asJsonString());
            geoJson = entries.toJavaBean(GeoJson.class);
            System.out.println(geoJson.toString());
        }

        if (geoJson != null) {
            S2LatLng s2LatLng =
                    S2LatLng.fromRadians(geoJson.getLatitude(), geoJson.getLongitude());
            System.out.printf(s2LatLng.toString());
        }

        store.close();
        connection.close();
    }

    private static String convertToJson(Object o) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(o);
    }

    private static GeoJson createGeoJson() {
        GeoJson geo = new GeoJson();
        String newDocUUID = UUID.randomUUID().toString();
        geo.set_id(newDocUUID);
        geo.setType("Point");
        geo.setLatitude(37.4185099);
        geo.setLongitude(-121.9450038);
        geo.setName("MapR headquarter");
        return geo;
    }
}
