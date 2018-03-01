package com.mapr.geospatial.lib;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GPoint {
    private Double latitude;
    private Double longitude;
}
