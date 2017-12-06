package com.mapr.geospatial;

import com.google.common.geometry.R2Vector;
import com.google.common.geometry.S1Angle;
import com.google.common.geometry.S2Cap;
import com.google.common.geometry.S2Cell;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2RegionCoverer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

/**
 * Simple geometric processing using S2.
 * from https://github.com/tdunning/parksim
 */
public class Geo {

    private S2Point origin;

    private S2Point north;
    private S2Point east;

    public Geo(double latitude, double longitude) {
        this.origin = S2LatLng.fromDegrees(latitude, longitude).toPoint();

        this.north = S2LatLng.fromDegrees(90, 0).toPoint();
        this.east = S2Point.normalize(S2Point.crossProd(north, origin));
        this.north = S2Point.normalize(S2Point.crossProd(origin, east));
    }

    public <T> void scan(SortedMap<Long, T> spots, double x, double y, double limit,
                         Function2<T, Integer, Boolean> action) {
        scan(spots, x, y, limit, action, null);
    }

    public <T> void scan(SortedMap<Long, T> spots, double x, double y, double limit,
                         Function2<T, Integer, Boolean> action,
                         Function2<S2Cell, Integer, Void> regionLogger) {
        S2LatLng base = getS2LatLng(x, y);
        List<S2CellId> searches = regionSearch(base, limit);
        int i = 0;
        for (S2CellId search : searches) {
            long a = search.childBegin(S2CellId.MAX_LEVEL).id();
            long b = search.childEnd(S2CellId.MAX_LEVEL).id();
            S2Cell s = new S2Cell(search);
            if (regionLogger != null) {
                regionLogger.apply(s, i);
            }
            // you will probably want to search a database rather than an ordered map.
            Set<Long> keys = spots.tailMap(a).keySet();
            for (Long k : keys) {
                if (k > b) {
                    break;
                }
                T p = spots.get(k);
                if (!action.apply(p, i)) {
                    break;
                }
            }
            i++;
        }
    }

    public interface Function2<T1, T2, R> {
        R apply(T1 t1, T2 t2);
    }

    @SuppressWarnings("WeakerAccess")

    static public List<S2CellId> regionSearch(S2LatLng point, double radius) {
        ArrayList<S2CellId> covering = new ArrayList<>();
        S2Cap circle = S2Cap.fromAxisAngle(point.toPoint(), S1Angle.radians(radius / S2LatLng.EARTH_RADIUS_METERS));
        new S2RegionCoverer().getCovering(circle, covering);
        return covering;
    }

    @SuppressWarnings("WeakerAccess")
    public S2LatLng getS2LatLng(double x, double y) {
        return new S2LatLng(S2Point.add(origin,
                S2Point.add(
                        S2Point.mul(east, x / S2LatLng.EARTH_RADIUS_METERS),
                        S2Point.mul(north, y / S2LatLng.EARTH_RADIUS_METERS))));
    }

    @SuppressWarnings("WeakerAccess")
    public R2Vector getXY(S2LatLng p) {
        S2Point px = p.toPoint();
        return new R2Vector(
                px.dotProd(east) * S2LatLng.EARTH_RADIUS_METERS,
                px.dotProd(north) * S2LatLng.EARTH_RADIUS_METERS);
    }

    static S2CellId point(double latitude, double longitude) {
        return S2CellId.fromLatLng(S2LatLng.fromDegrees(latitude, longitude));
    }

    static double latitude(S2CellId point) {
        return point.toLatLng().lat().degrees();
    }

    static double longitude(S2CellId point) {
        return point.toLatLng().lng().degrees();
    }
}
