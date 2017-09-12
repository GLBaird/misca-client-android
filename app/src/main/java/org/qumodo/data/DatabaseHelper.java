package org.qumodo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.qumodo.data.contracts.Groups;
import org.qumodo.data.contracts.Messages;
import org.qumodo.data.contracts.Users;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "org.qumodo.misca_client";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DatabaseHelper";
    private Context applicationContext;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        applicationContext = context.getApplicationContext();
    }

    private void setupTables(SQLiteDatabase db) {
        db.execSQL(Users.SQL_CREATE_ENTRIES);
        db.execSQL(Groups.SQL_CREATE_ENTRIES);
        db.execSQL(Messages.SQL_CREATE_ENTRIES);
    }

    private void loadSampleData(SQLiteDatabase db) {
        db.execSQL(Users.SQL_SAMPLE_DATA(applicationContext));
        db.execSQL(Groups.SQL_SAMPLE_DATA(applicationContext));
        db.execSQL(Messages.SQL_SAMPLE_DATA(applicationContext));
    }

    private void migrateData(SQLiteDatabase db) {
        db.execSQL(Messages.SQL_DELETE_ENTRIES);
        db.execSQL(Groups.SQL_DELETE_ENTRIES);
        db.execSQL(Users.SQL_DELETE_ENTRIES);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        setupTables(db);
        loadSampleData(db);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        migrateData(db);
        setupTables(db);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
