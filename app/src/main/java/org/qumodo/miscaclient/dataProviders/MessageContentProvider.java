package org.qumodo.miscaclient.dataProviders;

import android.content.Context;

import org.qumodo.data.DataManager;
import org.qumodo.data.models.Group;
import org.qumodo.data.models.Message;

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
public class MessageContentProvider {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Message> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Message> ITEM_MAP = new HashMap<>();

    public static void setup(Context context, String groupID){
        DataManager dm = new DataManager(context);
        List<Message> vals = dm.getMessages(groupID);
        for (Message val: vals) {
            addItem(val);
        }
    }

    public static int unreadMessagesInGroup(Context context, String groupID) {
        DataManager dm = new DataManager(context);
        return dm.unreadMessagesInGroup(groupID);
    }

    private static void addItem(Message item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getId(), item);
    }

}
