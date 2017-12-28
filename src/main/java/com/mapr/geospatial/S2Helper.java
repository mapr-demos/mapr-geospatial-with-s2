package com.mapr.geospatial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.geometry.S2CellId;
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
    private String tableName;
    private Connection connection;
    private DocumentStore table;

    public S2Helper(String tableName, Connection connection) {
        this.tableName = tableName;
        this.connection = connection;
        this.table = connection.getStore(tableName);
    }

    @SneakyThrows
    public List<Point> findAllPointsInCell(S2CellId cellId) {
        Long bmin = cellId.rangeMin().id();
        Long bmax = cellId.rangeMax().id();

        List<Point> points = new ArrayList<>();

        DocumentStream stream = table.findQuery(findCell(bmin, bmax));
        for (Document document : stream) {
            Point point = objectMapper.readValue(document.asJsonString(), Point.class);
            points.add(point);
        }
        return points;
    }

    private Query findCell(Long bmin, Long bmax) {
        return connection.newQuery()
                .where(connection.newCondition()
                        .is("id", QueryCondition.Op.GREATER_OR_EQUAL, bmin)
                        .and()
                        .is("id", QueryCondition.Op.LESS_OR_EQUAL, bmax))
                .build();
    }

    private void storePoint(Connection connection, Point point) {
        Document newPoint = connection
                .newDocument()
                .setId(UUID.randomUUID().toString())
                .set("id", point.getCellId())
                .set("value", point.getName());
        table.insertOrReplace(newPoint);
    }

    private S2CellId getCellId(Double latitude, Double longitude) {
        return S2CellId.fromLatLng(fromDegrees(latitude, longitude));
    }
}
