package com.mapr.geospatial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.geospatial.geo.GeoJson;
import com.mapr.geospatial.geo.Geometry;
import com.mapr.geospatial.geo.Properties;

import java.util.ArrayList;

public class InsertData {
    public static void main(String[] args) throws JsonProcessingException {

        final ObjectMapper mapper = new ObjectMapper();

        GeoJson geo = new GeoJson();
        Geometry geometry = new Geometry();
        Properties properties = new Properties();

        geo.setType("Feature");
        geometry.setType("Point");
        geometry.setLatitude( -105.01621);
        geometry.setLongitude(39.57422);
        properties.setName("Dinagat Islands");

        ArrayList<Geometry> geometries = new ArrayList<>();
        geometries.add(geometry);

        geo.setGeometries(geometries);
        geo.setProperties(properties);

        String value = mapper.writeValueAsString(geo);

        System.out.println(value);

//        final Connection connection = DriverManager.getConnection("ojai:mapr:");
//        final DocumentStore store = connection.getStore("/demo_table");

//        store.close();
//        connection.close();
    }
}
