package com.mapr.geospatial;

import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2CellUnion;
import com.google.common.geometry.S2LatLngRect;
import com.google.common.geometry.S2RegionCoverer;
import org.ojai.store.Connection;
import org.ojai.store.DriverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.geometry.S2LatLng.fromDegrees;

public class InsertData {

    private static final Logger log = LoggerFactory.getLogger(InsertData.class);

    private static final String AIRPORTS_TABLE_NAME = "/apps/airports";

    private static final String DRIVER_NAME = "ojai:mapr:";
    private static final Connection connection = DriverManager.getConnection(DRIVER_NAME);


    public static void main(String[] args) throws Exception {

        S2Helper s2Helper = new S2Helper(AIRPORTS_TABLE_NAME, connection);
        S2LatLngRect rect = S2LatLngRect.fromPointPair(
                fromDegrees(48.99, 1.852),
                fromDegrees(48.68, 2.75)
        );
        S2RegionCoverer coverer = new S2RegionCoverer();
        coverer.setLevelMod(20);
        coverer.setMaxCells(8);
        S2CellUnion covering = coverer.getCovering(rect);

        for (S2CellId cellId : covering.cellIds()) {
            s2Helper.findAllPointsInCell(cellId);
        }


    }


}
