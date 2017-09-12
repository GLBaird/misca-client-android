package org.qumodo.data;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DataLoader {

    private DataLoader() {}

    private static final String TAG = "DataLoader";

    @Nullable
    public static String loadText(Context applicationContext, int resourceID) {
        InputStream is = applicationContext.getResources().openRawResource(resourceID);
        String loadedData = loadText(is);
        if (loadedData == null) {
            Log.e(TAG, "Failed to load Raw Resource " + resourceID);
        }
        return loadedData;
    }

    @Nullable
    public static String loadText(InputStream is) {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String currentLine = reader.readLine();
            while (currentLine != null) {
                sb.append(currentLine);
                currentLine = reader.readLine();
            }
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "IO Error loadText");
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public static String loadText(String path) {
        File file = new File(path);
        return loadText(file);
    }

    @Nullable
    static String loadText(File file) {
        try {
            InputStream is = new FileInputStream(file);
            return loadText(is);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to load from file " + file.getAbsolutePath());
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public static JSONObject loadJSONObject(String data) {
        try {
            return new JSONObject(data);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSONObject "+data);
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public static JSONArray loadJSONArray(String data) {
        try {
            return new JSONArray(data);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSONArray " + data);
            e.printStackTrace();
        }

        return null;
    }
}
