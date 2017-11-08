package org.qumodo.data.contracts;

import android.content.Context;
import android.provider.BaseColumns;

import org.qumodo.data.DataLoader;
import org.qumodo.miscaclient.R;

public class Enrichments {

    private Enrichments() {}

    public static class EnrichmentsEntry implements BaseColumns {

        public static final String TABLE_NAME = "enrichments";
        public static final String MESSAGE_ID = "message_id";
        public static final String COLUMN_NAME_ANPR = "anpr";
        public static final String COLUMN_NAME_FACES = "faces";
        public static final String COLUMN_NAME_CLASSIFICATION = "classification";
        public static final String COLUMN_NAME_CAPTIONS = "captions";
        public static final String COLUMN_NAME_EXIF = "exif";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LON = "lon";

    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + EnrichmentsEntry.TABLE_NAME + " (" +
                    EnrichmentsEntry._ID + " TEXT PRIMARY KEY," +
                    EnrichmentsEntry.MESSAGE_ID + " TEXT," +
                    EnrichmentsEntry.COLUMN_NAME_ANPR + " TEXT," +
                    EnrichmentsEntry.COLUMN_NAME_FACES + " TEXT," +
                    EnrichmentsEntry.COLUMN_NAME_CLASSIFICATION + " TEXT," +
                    EnrichmentsEntry.COLUMN_NAME_CAPTIONS + " TEXT," +
                    EnrichmentsEntry.COLUMN_NAME_EXIF + " TEXT," +
                    EnrichmentsEntry.COLUMN_NAME_LAT + " TEXT," +
                    EnrichmentsEntry.COLUMN_NAME_LON + " TEXT" +
            ")";


    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + EnrichmentsEntry.TABLE_NAME;

    public static String SQL_SAMPLE_DATA(Context appContext) {
        return null;
    }

    public static final String[] projection = {
            EnrichmentsEntry._ID,
            EnrichmentsEntry.MESSAGE_ID,
            EnrichmentsEntry.COLUMN_NAME_ANPR,
            EnrichmentsEntry.COLUMN_NAME_FACES,
            EnrichmentsEntry.COLUMN_NAME_CLASSIFICATION,
            EnrichmentsEntry.COLUMN_NAME_CAPTIONS,
            EnrichmentsEntry.COLUMN_NAME_EXIF,
            EnrichmentsEntry.COLUMN_NAME_LAT,
            EnrichmentsEntry.COLUMN_NAME_LON
    };

}
