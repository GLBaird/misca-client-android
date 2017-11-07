package org.qumodo.miscaclient.dataProviders;

import android.support.annotation.Nullable;
import android.util.Log;

import org.qumodo.data.models.EnrichmentData;
import java.util.HashMap;

public class DataEnrichmentProvider {

    public interface DataEnrichmentListener {
        void enrichmentDataReady(EnrichmentData data);
    }

    private static DataEnrichmentProvider mDataEnrichmentProvider;

    public static DataEnrichmentProvider getProvider() {
        if (mDataEnrichmentProvider == null) {
            mDataEnrichmentProvider = new DataEnrichmentProvider();
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

}
