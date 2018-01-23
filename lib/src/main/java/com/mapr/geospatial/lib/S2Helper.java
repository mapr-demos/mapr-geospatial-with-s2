package com.mapr.geospatial.lib;

import com.google.common.geometry.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class S2Helper {

    private static S2CellUnion getS2CellIds(S2Region region, int level) {
        S2RegionCoverer coverer = new S2RegionCoverer();
        coverer.setLevelMod(level);
        return coverer.getCovering(region);
    }

    /**
     * Generate query for searching points in the region
     *
     * @param columnName name of the column which contains cell Id
     * @param region     place in which to look for points
     * @param level      the level of search accuracy
     * @return List of queries for searching the points in the region
     */
    public List<String> generateQueries(String columnName, S2Region region, ZoomLevel level) {
        S2CellUnion s2CellIds = getS2CellIds(region, level.getLevel());

        List<String> queries = new ArrayList<>();
        for (S2CellId cellId : s2CellIds.cellIds()) {
            Long bmin = cellId.rangeMin().id();
            Long bmax = cellId.rangeMax().id();

            String query =
                    "{\"$and\": " +
                            "[" +
                            "{\"$ge\":{\"" + columnName + "\":" + bmin + "}}," +
                            "{\"$le\":{\"" + columnName + "\":" + bmax + "}}" +
                            "]}";
            queries.add(query);
        }
        return queries;
    }

    /**
     * Generate cell Id based on the latitude/longitude degrees
     *
     * @param lat latitude in degrees
     * @param lng longitude in degrees
     * @return cellId long representation
     */
    public Long generateCellIdFromDegrees(double lat, double lng) {
        return S2CellId.fromLatLng(S2LatLng.fromDegrees(lat, lng)).id();
    }
}
