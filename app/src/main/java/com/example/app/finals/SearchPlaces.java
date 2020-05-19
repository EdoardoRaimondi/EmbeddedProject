package com.example.app.finals;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * There is a difference between the string that user see on the UI and
 * the same place string used by google. In order to automate the searching process,
 * it is needed to delete that difference with an adapter
 */
public class SearchPlaces {

    private static final String RESTAURANT = "RESTAURANT";
    private static final String MOVIE_THEATER = "MOVIE THEATER";
    private static final String ZOO = "ZOO";
    private static final String MUSEUM = "MUSEUM";
    private static final String ART_GALLERY = "ART GALLERY";
    private static final String PARK = "PARK";
    private static final String TOURIST_ATTRACTION = "LOCAL ATTRACTIONS";
    private static final String SUPERMARKET = "SUPERMARKET";
    private static final String PARKING = "PARKING";
    private static final String GAS_STATION = "GAS STATION";
    private static final String PHARMACY   = "PHARMACY";

    /**
     * @return a collection of all the searchable places
     */
    public static Collection<String> getPlaceSearchList(){
        Collection<String> mPlaceList = new LinkedList<>();
        mPlaceList.add(RESTAURANT);
        mPlaceList.add(PHARMACY);
        mPlaceList.add(PARKING);
        mPlaceList.add(GAS_STATION);
        mPlaceList.add(TOURIST_ATTRACTION);
        mPlaceList.add(MUSEUM);
        mPlaceList.add(MOVIE_THEATER);
        mPlaceList.add(PARK);
        mPlaceList.add(SUPERMARKET);
        mPlaceList.add(ZOO);
        mPlaceList.add(ART_GALLERY);
        return mPlaceList;
    }

    /**
     * A map with
     * key   = place user see on UI
     * value = place used by google search
     * @return that map
     */
    public static Map<String, NearbyRequestType> getPlacesAdapter(){
        Map<String, NearbyRequestType> map = new HashMap<>();
        map.put(RESTAURANT, NearbyRequestType.restaurant);
        map.put(PHARMACY, NearbyRequestType.pharmacy);
        map.put(PARKING, NearbyRequestType.parking);
        map.put(GAS_STATION, NearbyRequestType.gas_station);
        map.put(TOURIST_ATTRACTION, NearbyRequestType.tourist_attraction);
        map.put(MUSEUM, NearbyRequestType.museum);
        map.put(MOVIE_THEATER, NearbyRequestType.movie_theater);
        map.put(PARK, NearbyRequestType.park);
        map.put(SUPERMARKET, NearbyRequestType.supermarket);
        map.put(ZOO, NearbyRequestType.zoo);
        map.put(ART_GALLERY, NearbyRequestType.art_gallery);
        return map;
    }
}
