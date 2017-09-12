package org.qumodo.data.contracts;

import android.content.Context;
import android.provider.BaseColumns;

import org.qumodo.data.DataLoader;
import org.qumodo.miscaclient.R;

public class Users {

    private Users() {}

    public static class UsersEntry implements BaseColumns {

        public static final String TABLE_NAME = "users";
        public static final String COLUMN_NAME = "name";

    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + UsersEntry.TABLE_NAME + " (" +
                    UsersEntry._ID + " TEXT PRIMARY KEY," +
                    UsersEntry.COLUMN_NAME + " TEXT)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + UsersEntry.TABLE_NAME;

    public static String SQL_SAMPLE_DATA(Context appContext) {
        return DataLoader.loadText(appContext, R.raw.sample_data_users);
    }

    public static final String SQL_GET_ALL =
            "SELECT * FROM " + UsersEntry.TABLE_NAME +
                    " ORDER BY " +
                        UsersEntry.COLUMN_NAME + " ASC";

    public static final String SQL_GET_RECORD =
            "SELECT * FROM " + UsersEntry.TABLE_NAME +
                    " WHERE " +
                        UsersEntry._ID + " = ? " +
                    " ORDER BY " +
                        UsersEntry.COLUMN_NAME + " ASC";

    public static final String[] projection = {
            UsersEntry._ID,
            UsersEntry.COLUMN_NAME
    };

}
