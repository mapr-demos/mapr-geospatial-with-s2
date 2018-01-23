package com.mapr.geospatial.sample.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Point {

    private String _id;
    private Long cellId;
    private Object value;
}
