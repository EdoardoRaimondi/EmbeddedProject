package com.example.app.interfaces;

import com.google.android.libraries.places.api.model.Place;

import org.json.JSONException;

import java.util.List;

/**
 * To implement data parser for json google maps requests
 */
public interface MapJSonDataParser {

    /**
     * Method to parse the Json string data
     * @param JSonData String formed by obtained json file
     * @return List<Place>
     * @throws JSONException If Json computation goes wrong
     */
    List<Place> parse(String JSonData) throws JSONException;


}
