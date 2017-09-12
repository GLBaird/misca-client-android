package org.qumodo.data.contracts;

import android.content.Context;
import android.provider.BaseColumns;

import org.qumodo.data.DataLoader;
import org.qumodo.miscaclient.R;

public class Messages {

    private Messages() {}

    public static class MessagesEntry implements BaseColumns {

        public static final String TABLE_NAME = "messages";
        public static final String COLUMN_NAME_FROM_ID = "from_id";
        public static final String COLUMN_NAME_GROUP_ID = "group_id";
        public static final String COLUMN_NAME_TS = "ts";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_DATA = "data";
        public static final String COLUMN_NAME_VIEWED = "viewed";

    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MessagesEntry.TABLE_NAME + " (" +
                    MessagesEntry._ID + " INTEGER PRIMARY KEY," +
                    MessagesEntry.COLUMN_NAME_FROM_ID + " TEXT," +
                    MessagesEntry.COLUMN_NAME_GROUP_ID + " TEXT," +
                    MessagesEntry.COLUMN_NAME_TS + " INT," +
                    MessagesEntry.COLUMN_NAME_TYPE + " TEXT," +
                    MessagesEntry.COLUMN_NAME_DATA + " TEXT," +
                    MessagesEntry.COLUMN_NAME_VIEWED + " TEXT," +
                    "FOREIGN KEY (" +
                            MessagesEntry.COLUMN_NAME_FROM_ID +
                        ") REFERENCES " +
                            Users.UsersEntry.TABLE_NAME +
                        "(" +
                            Users.UsersEntry._ID +
                        ")," +
                    "FOREIGN KEY (" +
                            MessagesEntry.COLUMN_NAME_GROUP_ID +
                        ") REFERENCES " +
                            MessagesEntry.TABLE_NAME +
                        "(" +
                            MessagesEntry._ID +
                        ")" +
                    ")";


    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MessagesEntry.TABLE_NAME;

    public static String SQL_SAMPLE_DATA(Context appContext) {
        return DataLoader.loadText(appContext, R.raw.sample_data_messages);
    }

    public static final String[] projection = {
            MessagesEntry._ID,
            MessagesEntry.COLUMN_NAME_FROM_ID,
            MessagesEntry.COLUMN_NAME_GROUP_ID,
            MessagesEntry.COLUMN_NAME_TS,
            MessagesEntry.COLUMN_NAME_TYPE,
            MessagesEntry.COLUMN_NAME_DATA,
            MessagesEntry.COLUMN_NAME_VIEWED
    };

}
