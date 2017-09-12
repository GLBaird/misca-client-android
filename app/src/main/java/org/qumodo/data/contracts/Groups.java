package org.qumodo.data.contracts;

import android.content.Context;
import android.provider.BaseColumns;

import org.qumodo.data.DataLoader;
import org.qumodo.miscaclient.R;

public class Groups {

    private Groups(){}

    public static class GroupsEntry implements BaseColumns {

        public static final String TABLE_NAME = "groups";
        public static final String COLUMN_NAME_GROUP_NAME = "group_name";
        public static final String COLUMN_NAME_OWNER_ID = "owner_id";
        public static final String COLUMN_NAME_CREATED_TS = "created_ts";

    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + GroupsEntry.TABLE_NAME + " (" +
                GroupsEntry._ID + " TEXT PRIMARY KEY," +
                GroupsEntry.COLUMN_NAME_GROUP_NAME + " TEXT, " +
                GroupsEntry.COLUMN_NAME_OWNER_ID + " TEXT, " +
                GroupsEntry.COLUMN_NAME_CREATED_TS + " INTEGER, " +
                "FOREIGN KEY(" +
                        GroupsEntry.COLUMN_NAME_OWNER_ID +
                    ") REFERENCES " +
                        Users.UsersEntry.TABLE_NAME +
                    "(" +
                        Users.UsersEntry._ID +
                    ")" +
            ")";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + GroupsEntry.TABLE_NAME;

    public static String SQL_SAMPLE_DATA(Context appContext) {
        return DataLoader.loadText(appContext, R.raw.sample_data_groups);
    }

    public static final String[] projection = {
            GroupsEntry._ID,
            GroupsEntry.COLUMN_NAME_GROUP_NAME,
            GroupsEntry.COLUMN_NAME_CREATED_TS,
            GroupsEntry.COLUMN_NAME_OWNER_ID
    };

}
