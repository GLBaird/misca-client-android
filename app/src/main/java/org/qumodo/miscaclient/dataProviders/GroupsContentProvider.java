package org.qumodo.miscaclient.dataProviders;

import android.content.Context;
import android.util.Log;

import org.qumodo.data.DataManager;
import org.qumodo.data.models.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper Class to provide fake data from DB for mocking UI
 * To be replaced with real data provider.
 * <p>
 * TODO: Replace all uses of this class before publishing app.
 */
public class GroupsContentProvider {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Group> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Group> ITEM_MAP = new HashMap<>();

    public static void setup(Context context){
        DataManager dm = new DataManager(context);
        List<Group> vals = dm.getGroups();
        Log.d("????", ">>> WHAT "+vals);
        for (Group val: vals) {
            addItem(val);
        }
    }

    private static void addItem(Group item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getId(), item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final String id;
        public final String content;
        public final String details;

        public DummyItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
