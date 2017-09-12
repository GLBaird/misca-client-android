package org.qumodo.data.models;

import android.content.Context;
import android.database.Cursor;

import org.qumodo.data.DataManager;
import org.qumodo.data.contracts.Groups;

import java.text.DateFormat;
import java.util.Date;

public class Group {

    private String id;
    private String name;
    private String ownerID;
    private Date created;
    private Context appContext;

    public Group(Cursor cursor, Context context) {
        this.id = cursor.getString(cursor.getColumnIndex(Groups.GroupsEntry._ID));
        this.name = cursor.getString(cursor.getColumnIndex(Groups.GroupsEntry.COLUMN_NAME_GROUP_NAME));
        this.ownerID = cursor.getString(cursor.getColumnIndex(Groups.GroupsEntry.COLUMN_NAME_OWNER_ID));
        long created = cursor.getLong(cursor.getColumnIndex(Groups.GroupsEntry.COLUMN_NAME_CREATED_TS));
        this.created = new Date(created);
        this.appContext = context.getApplicationContext();
    }

    public Group(String id, String name, String ownerID, int created, Context context) {
        this.id = id;
        this.name = name;
        this.ownerID = ownerID;
        this.created = new Date(created);
        this.appContext = context.getApplicationContext();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getOwner() {
        return null;
    }

    public String getDateCreatedShort() {
        return getDateCreated(DateFormat.SHORT);
    }

    public String getDateCreatedLong() {
        return getDateCreated(DateFormat.LONG);
    }

    public String getDateCreated(int style) {
        return DateFormat.getDateInstance(style).format(created);
    }

    private Message lastMessage;

    public Message getLastMessageInGroup() {
        if (lastMessage == null) {
            DataManager dm = new DataManager(appContext);
            lastMessage = dm.getLastMessageInGroup(id);
        }

        return lastMessage;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", ownerID='" + ownerID + '\'' +
                ", created=" + created +
                '}';
    }
}


