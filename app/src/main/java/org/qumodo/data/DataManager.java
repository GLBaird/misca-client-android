package org.qumodo.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.qumodo.data.contracts.Groups;
import org.qumodo.data.contracts.Messages;
import org.qumodo.data.contracts.Users;
import org.qumodo.data.models.Group;
import org.qumodo.data.models.Message;
import org.qumodo.data.models.User;

import java.util.ArrayList;
import java.util.List;

public class DataManager {

    DatabaseHelper helper;
    Context appContext;

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
        return message;
    }

}
