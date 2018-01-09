package com.mapr.geospatial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2CellUnion;
import com.google.common.geometry.S2Region;
import com.google.common.geometry.S2RegionCoverer;
import com.mapr.geospatial.entity.Point;
import lombok.Data;
import lombok.SneakyThrows;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.Connection;
import org.ojai.store.DocumentStore;
import org.ojai.store.Query;
import org.ojai.store.QueryCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.google.common.geometry.S2LatLng.fromDegrees;

@Data
public class S2Helper {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String tableName;
    private final Connection connection;
    private final DocumentStore table;

    public S2Helper(String tableName, Connection connection) {
        this.tableName = tableName;
        this.connection = connection;
        this.table = connection.getStore(tableName);
    }

    public List<Point> findAllPointsInRegion(S2Region region, ZoomLevel level) {
        S2CellUnion s2CellIds = getS2CellIds(region, level.getLevel());

        List<Point> points = new ArrayList<>();

        for (S2CellId cellId : s2CellIds.cellIds()) {
            List<Point> pointsInCell = findAllPointsInCell(cellId);
            points.addAll(pointsInCell);
        }
        return points;
    }

    /**
     * Looking for all points withing cell range.
     *
     * @param cellId S2CellId
     * @return List of points
     */
    @SneakyThrows
    public List<Point> findAllPointsInCell(S2CellId cellId) {
        // compute min & max limits for cell
        Long bmin = cellId.rangeMin().id();
        Long bmax = cellId.rangeMax().id();

        List<Point> points = new ArrayList<>();
        //find all cells from range
        DocumentStream stream = table.findQuery(findCells(bmin, bmax));
        for (Document document : stream) {
            Point point = objectMapper.readValue(document.asJsonString(), Point.class);
            points.add(point);
        }
        return points;
    }

    /**
     * Build query that will find all cells from range
     *
     * @param bmin min id value
     * @param bmax max id value
     * @return Query
     */
    private Query findCells(Long bmin, Long bmax) {
        return connection.newQuery()
                .where(connection.newCondition()
                        .is("cellId", QueryCondition.Op.LESS_OR_EQUAL, bmax)
                        .build())
                .where(connection.newCondition()
                        .is("cellId", QueryCondition.Op.GREATER_OR_EQUAL, bmin)
                        .build())
                .build();
    }

    public void storePoint(Point point) {
        Document newPoint = connection
                .newDocument()
                .setId(point.get_id())
                .set("cellId", point.getCellId())
                .setArray("value", point.getValue());
        table.insertOrReplace(newPoint);
    }

    public void storePoint(Double latitude, Double longitude, Object value) {
        S2CellId cellId = getCellIdFromLatLong(latitude, longitude);
        Point point = new Point(UUID.randomUUID().toString(), cellId.id(), value);
        storePoint(point);
    }

    /**
     * Compute cellId using latitude and longitude
     *
     * @param latitude  latitude in degrees
     * @param longitude longitude in degrees
     * @return S2CellId
     */
    public S2CellId getCellIdFromLatLong(Double latitude, Double longitude) {
        return S2CellId.fromLatLng(fromDegrees(latitude, longitude));
    }

    private S2CellUnion getS2CellIds(S2Region region, int level) {
        S2RegionCoverer coverer = new S2RegionCoverer();
        coverer.setLevelMod(level);
        return coverer.getCovering(region);
    }
}
