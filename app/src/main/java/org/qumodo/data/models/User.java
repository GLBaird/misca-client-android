package org.qumodo.data.models;

import android.database.Cursor;

import org.qumodo.data.contracts.Users;

public class User {

    private String id;
    private String name;

    public User(Cursor cursor) {
        this.id = cursor.getString(cursor.getColumnIndex(Users.UsersEntry._ID));
        this.name = cursor.getString(cursor.getColumnIndex(Users.UsersEntry.COLUMN_NAME));
    }

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
