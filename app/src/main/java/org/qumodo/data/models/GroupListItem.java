package org.qumodo.data.models;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.qumodo.data.contracts.Groups;
import org.qumodo.data.contracts.Messages;
import org.qumodo.network.QMessageType;

public class GroupListItem {

    public String id, name, lastMessageText, lastMessageTime, lastMessageFromID;
    public QMessageType lastMessageType;
    public int unreadMessages;
    public boolean userOnline = false;

    public GroupListItem(Cursor cursor, String refForMessageCount, Context context) {
        this.id = cursor.getString(cursor.getColumnIndex(Groups.GroupsEntry._ID));
        this.name = cursor.getString(cursor.getColumnIndex(Groups.GroupsEntry.COLUMN_NAME_GROUP_NAME));
        String data = cursor.getString(cursor.getColumnIndex(Messages.MessagesEntry.COLUMN_NAME_DATA));
        lastMessageType = QMessageType.conform(cursor.getInt(cursor.getColumnIndex(Messages.MessagesEntry.COLUMN_NAME_TYPE)));

        try {
            JSONObject parsed = new JSONObject(data);
            lastMessageText = lastMessageType == QMessageType.PICTURE ? parsed.getString("caption") : parsed.getString("text");
        } catch (JSONException e) {
            lastMessageText = "";
            Log.e("GROUP", "JSON Parse Error");
            e.printStackTrace();
        }
        long timestamp = cursor.getLong(cursor.getColumnIndex(Messages.MessagesEntry.COLUMN_NAME_TS));
        lastMessageTime = DateUtils.getRelativeTimeSpanString(timestamp, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        lastMessageFromID = cursor.getString(cursor.getColumnIndex(Messages.MessagesEntry.COLUMN_NAME_FROM_ID));
        unreadMessages = cursor.getInt(cursor.getColumnIndex(refForMessageCount));
    }

}
