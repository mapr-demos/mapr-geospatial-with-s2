package com.mapr.geospatial.sample.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class State {
    private String _id;
    private String name;
    private String code;
    private Location loc;
}
