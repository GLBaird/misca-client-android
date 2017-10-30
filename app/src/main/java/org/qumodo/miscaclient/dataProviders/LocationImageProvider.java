package org.qumodo.miscaclient.dataProviders;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.nearby.messages.PublishCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qumodo.data.models.MiscaImage;
import org.qumodo.network.QMessage;
import org.qumodo.network.QMessageType;
import org.qumodo.network.QTCPSocket;
import org.qumodo.services.QTCPSocketService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LocationImageProvider {

    public interface LocationImageProviderListener {
        void locationImageProviderHasUpdatedWithData();
    }

    public static List<MiscaImage> ITEMS = new ArrayList<>();
    public static Map<String, MiscaImage> ITEMS_MAP = new HashMap<>();
    private static ArrayList<LocationImageProviderListener> listeners = new ArrayList<>();

    public static void addListener(LocationImageProviderListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(LocationImageProviderListener listener) {
        listeners.remove(listener);
    }

    public static void getLocationImages(Location location, Context context) {
       getLocationObjectImages(location, null, context);
    }

    public static void getLocationObjectImages(Location location, String description, Context context) {
        Log.d("LocationProvider", "Search with object: " + description);
        JSONObject data = new JSONObject();
        try {
            data.put("command", "core_image_search");
            data.put("lat", location.getLatitude());
            data.put("lon", location.getLongitude());
            data.put("classifier", description);

            QMessage message = new QMessage(
                    UserSettingsManager.getMiscaID(),
                    UserSettingsManager.getUserID(),
                    QMessageType.COMMAND,
                    data
            );

            Intent sendMessage = new Intent();
            sendMessage.setAction(QTCPSocketService.ACTION_SEND_MESSAGE);
            sendMessage.putExtra(QTCPSocketService.INTENT_KEY_MESSAGE, message.serialize());
            if (context != null) {
                context.sendBroadcast(sendMessage);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void parseImageData(JSONArray imageData) {
        ITEMS_MAP.clear();
        ITEMS.clear();
        for (int i = 0; i < imageData.length(); i++) {
            try {
                JSONObject image = (JSONObject) imageData.get(i);

                Log.d("LocationProvider", "Found: " + image.getString("classifiers") + image.getString("captions"));

                MiscaImage parsed = new MiscaImage(
                        image.getString("id"),
                        image.getString("path"),
                        image.getString("classifiers"),
                        image.getString("captions"),
                        new LatLng(image.getDouble("lat"), image.getDouble("lon")),
                        null
                );

                addImage(parsed);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (LocationImageProviderListener listener : listeners) {
            listener.locationImageProviderHasUpdatedWithData();
        }
    }

    public static void addImage(MiscaImage image) {
        ITEMS.add(image);
        ITEMS_MAP.put(image.getId(), image);
    }

}
