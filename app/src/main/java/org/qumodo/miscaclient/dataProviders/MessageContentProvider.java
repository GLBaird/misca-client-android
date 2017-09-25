package org.qumodo.miscaclient.dataProviders;

import android.content.Context;

import org.qumodo.data.DataManager;
import org.qumodo.data.models.Group;
import org.qumodo.data.models.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MessageContentProvider {

    private static String groupID;
    public static final List<Message> ITEMS = new ArrayList<>();
    public static final Map<String, Message> ITEM_MAP = new HashMap<>();

    public static void setup(Context context, String groupID){
        ITEMS.clear();
        ITEM_MAP.clear();
        MessageContentProvider.groupID = groupID;
        DataManager dm = new DataManager(context);
        List<Message> vals = dm.getMessages(groupID);
        for (Message val: vals) {
            addItem(val);
        }
    }

    public static String getGroupID() {
        return groupID;
    }

    public static int unreadMessagesInGroup(Context context, String groupID) {
        DataManager dm = new DataManager(context);
        return dm.unreadMessagesInGroup(groupID);
    }

    public static void addItem(Message item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getId(), item);
    }

    public static void updateMessageError(String messageID, boolean error) {
        Message message = ITEM_MAP.get(messageID);
        if (message != null) {
            message.setSendError(error);
        }
    }

}
