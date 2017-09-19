package org.qumodo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.qumodo.data.contracts.Groups;
import org.qumodo.data.contracts.Messages;
import org.qumodo.data.contracts.Users;
import org.qumodo.data.models.Group;
import org.qumodo.data.models.GroupListItem;
import org.qumodo.data.models.Message;
import org.qumodo.data.models.User;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.network.QMessageType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DataManager {

    DatabaseHelper helper;
    Context appContext;
    private static final String TAG = "DataManager";

    public DataManager(Context context) {
        appContext = context.getApplicationContext();
        helper = new DatabaseHelper(appContext);
    }

    public List<Group> getGroups() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor results = db.query(
                Groups.GroupsEntry.TABLE_NAME,
                Groups.projection,
                null, null, null, null,
                Groups.GroupsEntry.COLUMN_NAME_GROUP_NAME + " ASC, " +
                Groups.GroupsEntry.COLUMN_NAME_CREATED_TS + " ASC"
        );

        ArrayList<Group> groups = new ArrayList<>(results.getCount());

        while (results.moveToNext()) {
            groups.add(new Group(results, appContext));
        }

        results.close();
        db.close();
        return groups;
    }

    public List<GroupListItem> getAllGroupDataForListView() {
        String gTN = Groups.GroupsEntry.TABLE_NAME + ".";
        String mTN = Messages.MessagesEntry.TABLE_NAME + ".";
        String unreadColumn = "unread";

        String query =
            "SELECT " +
                    gTN + Groups.GroupsEntry._ID + ", " +
                    gTN + Groups.GroupsEntry.COLUMN_NAME_GROUP_NAME + ", " +
                    mTN + Messages.MessagesEntry.COLUMN_NAME_FROM_ID + ", " +
                    mTN + Messages.MessagesEntry.COLUMN_NAME_DATA + ", " +
                    mTN + Messages.MessagesEntry.COLUMN_NAME_TYPE + ", " +
                    mTN + Messages.MessagesEntry.COLUMN_NAME_VIEWED + ", " +
                    mTN + Messages.MessagesEntry.COLUMN_NAME_TS + ", " +
                "(SELECT count(*) " +
                 "FROM " + Messages.MessagesEntry.TABLE_NAME + " " +
                 "WHERE " +
                    mTN + Messages.MessagesEntry.COLUMN_NAME_GROUP_ID + " = " +
                    gTN + Groups.GroupsEntry._ID + " " +
                 "AND " + mTN + Messages.MessagesEntry.COLUMN_NAME_VIEWED + " = 0" +
                ") AS " + unreadColumn + " " +
                "FROM " + Groups.GroupsEntry.TABLE_NAME + " " +
                "LEFT JOIN " + Messages.MessagesEntry.TABLE_NAME + " " +
                "ON " +
                    gTN + Groups.GroupsEntry._ID + " = " +
                    mTN + Messages.MessagesEntry.COLUMN_NAME_GROUP_ID + " " +
                "AND " +
                    mTN + Messages.MessagesEntry.COLUMN_NAME_TS + " = (" +
                        "SELECT MAX(" + Messages.MessagesEntry.COLUMN_NAME_TS + ") " +
                        "FROM " + Messages.MessagesEntry.TABLE_NAME + " " +
                        "WHERE " +
                            gTN + Groups.GroupsEntry._ID + " = " +
                            mTN + Messages.MessagesEntry.COLUMN_NAME_GROUP_ID +
                    ")" +
                "ORDER BY " + mTN + Messages.MessagesEntry.COLUMN_NAME_TS + " DESC";

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor results = db.rawQuery(query, null);

        ArrayList<GroupListItem> groups = new ArrayList<>(results.getCount());

        while (results.moveToNext()) {
            groups.add(new GroupListItem(results, unreadColumn, appContext));
        }

        results.close();
        db.close();

        return groups;
    }

    public Group getGroup(String id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String selection = Groups.GroupsEntry._ID + " = ?";
        String[] selectionArgs = {id};
        Cursor result = db.query(
                Groups.GroupsEntry.TABLE_NAME,
                Groups.projection,
                selection,
                selectionArgs,
                null, null, null
        );
        Group group = null;
        if (result.getCount() > 0) {
            result.moveToFirst();
            group = new Group(result, appContext);
        }
        result.close();
        db.close();
        return group;
    }

    public List<User> getUsers() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor results = db.query(
                Users.UsersEntry.TABLE_NAME,
                Users.projection,
                null, null, null, null,
                Users.UsersEntry.COLUMN_NAME + " ASC"
        );

        ArrayList<User> users = new ArrayList<>(results.getCount());

        while (results.moveToNext()) {
            users.add(new User(results));
        }

        results.close();
        db.close();
        return users;
    }

    public User getUser(String userID) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String selection = Users.UsersEntry._ID + " = ?";
        String[] selectionArgs = {userID};
        Cursor result = db.query(
                Users.UsersEntry.TABLE_NAME,
                Users.projection,
                selection,
                selectionArgs,
                null, null, null
        );
        User user = null;
        if (result.getCount() > 0) {
            result.moveToFirst();
            user = new User(result);
        }
        result.close();
        db.close();
        return user;
    }

    public List<Message> getMessages(String groupID) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String selection = Messages.MessagesEntry.COLUMN_NAME_GROUP_ID + " = ?";
        String[] selectionArgs = {groupID};
        Cursor results = db.query(
                Messages.MessagesEntry.TABLE_NAME,
                Messages.projection,
                selection,
                selectionArgs,
                null, null,
                Messages.MessagesEntry.COLUMN_NAME_TS + " ASC"
        );
        ArrayList<Message> messages = new ArrayList<>(results.getCount());
        while (results.moveToNext()) {
            messages.add(new Message(results, appContext));
        }
        results.close();
        db.close();
        return messages;
    }

    public Message getMessage(String id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String selection = Messages.MessagesEntry._ID + " = ?";
        String[] selectionArgs = {id};
        Cursor result = db.query(
                Messages.MessagesEntry.TABLE_NAME,
                Messages.projection,
                selection,
                selectionArgs,
                null, null, null
        );
        Message message = null;
        if (result.getCount() > 0) {
            result.moveToFirst();
            message = new Message(result, appContext);
        }
        result.close();
        db.close();
        return message;
    }

    public Message getLastMessageInGroup(String groupID) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String selection = Messages.MessagesEntry.COLUMN_NAME_GROUP_ID + " = ?";
        String[] selectionArgs = {groupID};
        Cursor result = db.query(
                Messages.MessagesEntry.TABLE_NAME,
                Messages.projection,
                selection,
                selectionArgs,
                null, null,
                Messages.MessagesEntry.COLUMN_NAME_TS + " DESC", "1"
        );
        Message message = null;
        if (result.getCount() > 0) {
            result.moveToFirst();
            message = new Message(result, appContext);
        }
        result.close();
        db.close();
        return message;
    }

    public int unreadMessagesInGroup(String groupID) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String query = "SELECT Count(*) FROM " + Messages.MessagesEntry.TABLE_NAME +
                " WHERE " + Messages.MessagesEntry.COLUMN_NAME_GROUP_ID + " = ? " +
                "AND " + Messages.MessagesEntry.COLUMN_NAME_VIEWED + " = 0";
        String[] args = {groupID};
        Cursor result = db.rawQuery(query, args);
        result.moveToFirst();
        int value = result.getInt(0);
        result.close();
        db.close();
        return value;
    }

    public Message addNewMessage(String text, QMessageType type, String groupID, String id, String from, Date ts) {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        try {
            JSONObject data = new JSONObject();
            data.put("text", text);
            String userID = UserSettingsManager.getUserID();
            Message newMessage = new Message(
                id,
                from == null ? userID : from,
                groupID,
                ts == null ? new Date().getTime() : ts.getTime(),
                type.value,
                data.toString(),
                1,
                appContext
            );

            ContentValues cv = new ContentValues();
            cv.put(Messages.MessagesEntry._ID, newMessage.getId());
            cv.put(Messages.MessagesEntry.COLUMN_NAME_TYPE, newMessage.getType().value);
            cv.put(Messages.MessagesEntry.COLUMN_NAME_GROUP_ID, groupID);
            cv.put(Messages.MessagesEntry.COLUMN_NAME_FROM_ID, userID);
            cv.put(Messages.MessagesEntry.COLUMN_NAME_DATA, data.toString());
            cv.put(Messages.MessagesEntry.COLUMN_NAME_TS, newMessage.getTS());
            cv.put(Messages.MessagesEntry.COLUMN_NAME_VIEWED, 1);

            SQLiteDatabase db = helper.getWritableDatabase();
            db.insert(Messages.MessagesEntry.TABLE_NAME, null, cv);

            return newMessage;
        } catch (JSONException err) {
            //TODO: Handle this error??
            Log.d(TAG, "Error creating message with JSON??");
            err.printStackTrace();
            return null;
        }
    }

    public void setMessageError(String messageID, boolean error) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues(1);
        cv.put(Messages.MessagesEntry.COLUMN_NAME_SEND_ERROR, error);
        db.update(Messages.MessagesEntry.TABLE_NAME, cv, Messages.MessagesEntry._ID + " = ?", new String[]{messageID});
    }

}
