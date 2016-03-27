package io.telepat.sdk.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrei on 07.03.2016.
 *
 */
public class SubscriptionSorter {
    private static final String FIELD = "field";
    private static final String ORDER = "order";
    private static final String TYPE = "type";
    private static final String POI = "poi";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";

    public enum SortDirection {
        ascending {
            public String toString() { return "asc"; }
        },
        descending {
            public String toString() { return "desc"; }
        }
    }

    public enum SortType {
        defaultSorting {
            public String toString() { return "default"; }
        },
        geoSpacialSorting {
            public String toString() { return "geo"; }
        }
    }

    public SubscriptionSorter(String sortingField, SortDirection sortingDirection) {
        this.sortingField = sortingField;
        this.sortingDirection = sortingDirection;
        this.sortingType = SortType.defaultSorting;
    }

    public SubscriptionSorter(String sortingField, SortDirection sortingDirection, float latitude, float longitude) {
        this.sortingField = sortingField;
        this.sortingDirection = sortingDirection;
        this.poiLatitude = latitude;
        this.poiLongitude = longitude;
        this.sortingType = SortType.geoSpacialSorting;
    }

    private String sortingField;
    private SortDirection sortingDirection;
    private SortType sortingType;

    private float poiLatitude;
    private float poiLongitude;

    public SortDirection getSortingDirection() {
        return sortingDirection;
    }

    @SuppressWarnings("unused")
    public void setSortingDirection(SortDirection sortingDirection) {
        this.sortingDirection = sortingDirection;
    }


    @SuppressWarnings("unused")
    public void setSortingField(String sortingField) {
        this.sortingField = sortingField;
    }

    public String getSortingField() {
        return sortingField;
    }

    public SortType getSortingType() {
        return sortingType;
    }


    public float getPoiLatitude() {
        return poiLatitude;
    }

    public float getPoiLongitude() {
        return poiLongitude;
    }

    public Map<String, Object> getSortingMap() {
        HashMap<String, Object> sortingHashMap = new HashMap<>();
        sortingHashMap.put(FIELD, getSortingField());
        sortingHashMap.put(ORDER, getSortingDirection());
        sortingHashMap.put(TYPE, getSortingType());
        HashMap<String, Float> poiMap = new HashMap<>();
        poiMap.put(LATITUDE, getPoiLatitude());
        poiMap.put(LONGITUDE, getPoiLongitude());
        sortingHashMap.put(POI, poiMap);
        return sortingHashMap;
    }
}
