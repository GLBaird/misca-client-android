package org.qumodo.miscaclient.dataProviders;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qumodo.data.models.MiscaImage;
import org.qumodo.network.QMessage;
import org.qumodo.network.QMessageType;
import org.qumodo.services.QTCPSocketService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageListProvider {

    public interface ImageListProviderListener {
        void imageListProviderHasUpdatedWithData();
    }

    public static List<MiscaImage> ITEMS = new ArrayList<>();
    public static Map<String, MiscaImage> ITEMS_MAP = new HashMap<>();
    private static ArrayList<ImageListProviderListener> listeners = new ArrayList<>();

    public static void addListener(ImageListProviderListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(ImageListProviderListener listener) {
        listeners.remove(listener);
    }

    private void notifyListenersOfUpdate() {
        for (ImageListProviderListener listener : listeners) {
            listener.imageListProviderHasUpdatedWithData();
        }
    }

    public static void addImage(MiscaImage image) {
        ITEMS.add(image);
        ITEMS_MAP.put(image.getId(), image);
    }

    public static void setITEMS(List<MiscaImage> items) {
        ITEMS = items;
        ITEMS_MAP.clear();
        for (MiscaImage item : ITEMS) {
            ITEMS_MAP.put(item.getId(), item);
        }
    }

}
