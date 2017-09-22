package org.qumodo.miscaclient.dataProviders;

import android.content.Context;
import android.util.Log;

import org.qumodo.data.DataManager;
import org.qumodo.data.models.GroupListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsContentProvider {

    public static final List<GroupListItem> ITEMS = new ArrayList<>();
    public static final Map<String, GroupListItem> ITEM_MAP = new HashMap<>();

    public static void setup(Context context){
        DataManager dm = new DataManager(context);
        List<GroupListItem> vals = dm.getAllGroupDataForListView();
        for (GroupListItem val: vals) {
            addItem(val);
        }
    }

    public static void reloadData(Context context) {
        ITEMS.clear();
        ITEM_MAP.clear();
        setup(context);
    }

    private static void addItem(GroupListItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }
}
