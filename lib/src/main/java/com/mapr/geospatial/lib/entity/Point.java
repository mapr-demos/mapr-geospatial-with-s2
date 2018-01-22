package com.mapr.geospatial.lib.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Point {

    private String _id;
    private Long cellId;
    private Object value;
}
