package org.qumodo.data.models;


import android.graphics.Rect;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qumodo.miscaclient.dataProviders.LocationProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EnrichmentData {

    private String id;
    private String classification;
    private String captions;
    private LatLng location;
    private Rect[] faces;
    private String[] registrationPlates;
    private Map<String, String> exif;

    public EnrichmentData(String id, Map<String, String> exif, String classification,
                          String captions, LatLng location, Rect[] faces,
                          String[] registrationPlates) {
        this.id = id;
        this.exif = exif;
        this.classification = classification;
        this.captions = captions;
        this.location = location;
        this.faces = faces;
        this.registrationPlates = registrationPlates;
    }

    @Nullable
    public static EnrichmentData parseJSON(String json) {
        try {
            JSONObject converted = new JSONObject(json);
            return parseJSON(converted);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Map<String, String> parseExifData(JSONObject data) {
        HashMap<String, String> parsedExif = new HashMap<>();
        JSONObject exifData = null;
        try {
            exifData = data.getJSONObject("enriched_data").getJSONObject("exif");
            Iterator<String> keys = exifData.keys();

            while (keys.hasNext()) {
                try {
                    String key = keys.next();
                    String value = exifData.getString(key);
                    parsedExif.put(key, value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return parsedExif;
        }

        return parsedExif;
    }

    @Nullable
    public static EnrichmentData parseJSON(JSONObject json) {
        try {
            String id = json.getString("id");
            JSONObject body = json.getJSONObject("body");
            String classification = body.has("classifiers") ? body.getString("classifiers") : null;
            LatLng location = LocationProvider
                                .getSharedLocationProvider()
                                .getCurrentLocation();
            ArrayList<String> parsedPlates = new ArrayList<>();
            if (body.has("anpr")) {
                JSONArray plates = body.getJSONArray("anpr");
                for (int i = 0; i < plates.length(); i++) {
                    parsedPlates.add(plates.getString(i));
                }
            }

            ArrayList<Rect> faces = new ArrayList<>();
            if (body.has("faces")) {
                JSONArray faceData = body.getJSONArray("faces");
                for (int i = 0; i< faceData.length(); i++) {
                    JSONObject f = (JSONObject) faceData.get(i);
                    int x = f.getInt("x"), y = f.getInt("y"), w = f.getInt("w"), h = f.getInt("h");
                    faces.add(new Rect(x, y, x + w, y + h));
                }
            }

            Map<String, String> exif = parseExifData(body);
            return new EnrichmentData(id, exif, classification, null, location,
                    faces.toArray(new Rect[faces.size()]),
                    parsedPlates.toArray(new String[parsedPlates.size()]));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getId() {
        return id;
    }

    public String getClassification() {
        return classification;
    }

    public String getCaptions() {
        return captions;
    }

    public LatLng getLocation() {
        return location;
    }

    public Rect[] getFaces() {
        return faces;
    }

    public Map<String, String> getExif() {
        return exif;
    }

    public String[] getANPR() {
        return registrationPlates;
    }

    public boolean matchesID(String id) {
        return this.id.equals(id);
    }

    @Override
    public String toString() {
        return  "Enrichment Data for " + id + "\n" +
                "Classification: " + classification + "\n" +
                "Captions: " + (captions != null ? captions : "NULL") + "\n" +
                "Location: " + location.toString() + "\n" +
                "Anpr: " + Arrays.toString(registrationPlates) + "\n"+
                "faces: " + Arrays.toString(faces) + "\n" +
                "Exif: " + exif.toString() + "\n";

    }
}