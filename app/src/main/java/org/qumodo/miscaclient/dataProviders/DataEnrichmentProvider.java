package org.qumodo.miscaclient.dataProviders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qumodo.data.DatabaseHelper;
import org.qumodo.data.contracts.Enrichments;
import org.qumodo.data.models.EnrichmentData;
import org.qumodo.miscaclient.QMiscaClientApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataEnrichmentProvider {

    public interface DataEnrichmentListener {
        void enrichmentDataReady(EnrichmentData data);
    }

    private static DataEnrichmentProvider mDataEnrichmentProvider;

    public static DataEnrichmentProvider getProvider() {
        if (mDataEnrichmentProvider == null) {
            mDataEnrichmentProvider = new DataEnrichmentProvider();
            mDataEnrichmentProvider.loadData();
        }

        return mDataEnrichmentProvider;
    }

    private HashMap<String, EnrichmentData> data = new HashMap<>();
    private HashMap<String, DataEnrichmentListener> listeners = new HashMap<>();

    private DataEnrichmentProvider() {}

    public void addEnrichment(EnrichmentData enrichmentData) {
        if (enrichmentData != null) {
            data.put(enrichmentData.getId(), enrichmentData);
        }
        Log.d("EnrichmentProvider", enrichmentData != null ? enrichmentData.toString() : "Null Reference");
        if (enrichmentData != null && listeners.containsKey(enrichmentData.getId())) {
            listeners.get(enrichmentData.getId()).enrichmentDataReady(enrichmentData);
            listeners.remove(enrichmentData.getId());
        }

        storeData(enrichmentData);
    }

    @Nullable
    public EnrichmentData getDataWithID(String id) {
        return data.get(id);
    }

    public void removeEnrichment(String id) {
        data.remove(id);
    }

    public void removeEnrichment(EnrichmentData item) {
        data.remove(item.getId());
    }

    public boolean enrichmentDataExists(String id) {
        return data.containsKey(id);
    }

    public void addListener(String id, DataEnrichmentListener listener) {
        listeners.put(id, listener);
    }

    public void removeListener(DataEnrichmentListener listener) {
        for (String id : listeners.keySet()) {
            if (listeners.get(id).equals(listener)) {
                listeners.remove(id);
            }
        }
    }

    private void storeData(EnrichmentData item) {
        Context context = QMiscaClientApplication.getContext();
        if (context != null) {
            DatabaseHelper dh = new DatabaseHelper(context);
            SQLiteDatabase db = dh.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(Enrichments.EnrichmentsEntry.MESSAGE_ID, item.getId());
            cv.put(Enrichments.EnrichmentsEntry.COLUMN_NAME_CLASSIFICATION, item.getClassification());
            cv.put(Enrichments.EnrichmentsEntry.COLUMN_NAME_CAPTIONS, item.getCaptions());
            JSONArray anpr = new JSONArray(Arrays.asList(item.getANPR()));
            JSONObject exif = new JSONObject(item.getExif());
            JSONArray faceData = new JSONArray();
            for (Rect face : item.getFaces()) {
                JSONObject f = new JSONObject();
                try {
                    f.put("top", face.top);
                    f.put("bottom", face.bottom);
                    f.put("left", face.left);
                    f.put("right", face.right);

                    faceData.put(f);
                } catch (JSONException e) { e.printStackTrace(); }
            }
            cv.put(Enrichments.EnrichmentsEntry.COLUMN_NAME_ANPR, anpr.toString());
            cv.put(Enrichments.EnrichmentsEntry.COLUMN_NAME_EXIF, exif.toString());
            cv.put(Enrichments.EnrichmentsEntry.COLUMN_NAME_LAT, String.valueOf(item.getLocation().latitude));
            cv.put(Enrichments.EnrichmentsEntry.COLUMN_NAME_LON, String.valueOf(item.getLocation().longitude));
            cv.put(Enrichments.EnrichmentsEntry.COLUMN_NAME_FACES, faceData.toString());

            db.insert(Enrichments.EnrichmentsEntry.TABLE_NAME, null,cv);
            db.close();
        }
    }

    private void loadData() {
        Context context = QMiscaClientApplication.getContext();
        if (context != null) {
            DatabaseHelper dh = new DatabaseHelper(context);
            SQLiteDatabase db = dh.getWritableDatabase();

            db.execSQL(Enrichments.SQL_CREATE_ENTRIES);

            Cursor cursor = db.query(Enrichments.EnrichmentsEntry.TABLE_NAME, Enrichments.projection, null, null, null, null, null);
            while (cursor.moveToNext()) {
                String messageID = cursor.getString(cursor.getColumnIndex(Enrichments.EnrichmentsEntry.MESSAGE_ID));
                String classification = cursor.getString(cursor.getColumnIndex(Enrichments.EnrichmentsEntry.COLUMN_NAME_CLASSIFICATION));
                String captions = cursor.getString(cursor.getColumnIndex(Enrichments.EnrichmentsEntry.COLUMN_NAME_CAPTIONS));
                String lat = cursor.getString(cursor.getColumnIndex(Enrichments.EnrichmentsEntry.COLUMN_NAME_LAT));
                String lon = cursor.getString(cursor.getColumnIndex(Enrichments.EnrichmentsEntry.COLUMN_NAME_LON));
                String anpr = cursor.getString(cursor.getColumnIndex(Enrichments.EnrichmentsEntry.COLUMN_NAME_ANPR));
                String faces = cursor.getString(cursor.getColumnIndex(Enrichments.EnrichmentsEntry.COLUMN_NAME_FACES));
                String exif = cursor.getString(cursor.getColumnIndex(Enrichments.EnrichmentsEntry.COLUMN_NAME_EXIF));
                try {
                    JSONArray anprArray = new JSONArray(anpr);
                    List<String> anprList = new ArrayList<>(anprArray.length());
                    for (int i = 0; i < anprArray.length(); i++) {
                        anprList.add(anprArray.getString(i));
                    }

                    JSONArray faceArray = new JSONArray(faces);
                    List<Rect> faceList = new ArrayList<>(faceArray.length());
                    for (int i = 0; i < faceArray.length(); i++) {
                        JSONObject face = faceArray.getJSONObject(i);
                        faceList.add(new Rect(
                            Integer.parseInt(face.getString("left")),
                            Integer.parseInt(face.getString("top")),
                            Integer.parseInt(face.getString("right")),
                            Integer.parseInt(face.getString("bottom"))
                        ));
                    }

                    JSONObject exifOB = new JSONObject(exif);
                    Map<String, String> exifList = new HashMap<>();
                    Iterator<String> iter = exifOB.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        exifList.put(key, exifOB.getString(key));
                    }

                    LatLng location = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));

                    Rect[] facesParsed = faceList.toArray(new Rect[faceList.size()]);
                    String[] anprParsed  = anprList.toArray(new String[anprList.size()]);

                    EnrichmentData loaded = new EnrichmentData(
                            messageID,
                            exifList,
                            classification,
                            captions,
                            location,
                            facesParsed,
                            anprParsed
                    );

                    data.put(messageID, loaded);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("DATAENRICH", "Failed");
                }
            }
            Log.d("DATAENRICH", "loaded records " + data.size());

            cursor.close();
            db.close();
        }
    }

}
