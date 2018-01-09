package com.mapr.geospatial;

import com.google.common.geometry.S2LatLngRect;
import com.mapr.geospatial.entity.Point;
import lombok.extern.slf4j.Slf4j;
import org.ojai.store.Connection;
import org.ojai.store.DriverManager;

import java.util.List;

import static com.google.common.geometry.S2LatLng.fromDegrees;

@Slf4j
public class InsertData {

    private static final String AIRPORTS_TABLE_NAME = "/apps/airports";

    private static final String DRIVER_NAME = "ojai:mapr:";
    private static final Connection connection = DriverManager.getConnection(DRIVER_NAME);


    public static void main(String[] args) throws Exception {

        S2Helper s2Helper = new S2Helper(AIRPORTS_TABLE_NAME, connection);
        S2LatLngRect rect = S2LatLngRect.fromPointPair(
                fromDegrees(-118.766598, 45.67105),
                fromDegrees(-95.672179, 32.828369)
        );
        List<Point> allPointsInRegion = s2Helper.findAllPointsInRegion(rect, ZoomLevel.HIGH);

        log.info(String.valueOf(allPointsInRegion.size()));
    }
}
