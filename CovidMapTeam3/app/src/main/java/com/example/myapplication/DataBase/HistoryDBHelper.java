package com.example.myapplication.DataBase;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HistoryDBHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "HistoryDBHelper";

    //singleton
    private static HistoryDBHelper ts;

    private static final String TAG = "HistoryDBHelper";

    public static HistoryDBHelper getInstance(Context context) {
        if (ts == null) {
            ts = new HistoryDBHelper(context);
        }
        return ts;
    }

    public HistoryDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }


    /*
    Store the travel tracking data in an encrypted form to prevent privacy leak.
     */
    @Override
    public void onConfigure(SQLiteDatabase db){
        db.execSQL("PRAGMA key = 'secretkey'");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table HistoryDBHelper (" +
                "name TEXT," +
                "lat DOUBLE," +
                "long DOUBLE,"+
                "timestamp TIMESTAMP"+
                ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean addHistoryItem(String name, double lat, double lon, Timestamp timestamp){
        try{
            SQLiteDatabase db = ts.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name",name);
            values.put("lat",lat);
            values.put("long",lon);
            values.put("timestamp",timestamp.toString());
            db.insert(DATABASE_NAME,null,values);
            return true;
        }catch(Exception e) {
            Log.v(TAG, String.valueOf(e));
            return false;
        }
    }

    public ArrayList<HistoryItem>  getAllListHistory(){
        Log.v(TAG, "Start method getListTestCenter");
        ArrayList<HistoryItem> HistoryItems = new ArrayList<HistoryItem>();
        SQLiteDatabase db = ts.getReadableDatabase();
        Cursor c = db.query(DATABASE_NAME,null,null,null,null,null,null);
        while (c.moveToNext()) {
            HistoryItem temp = new HistoryItem(c.getString(c.getColumnIndex("name")),
                    c.getDouble(c.getColumnIndex("lat")),
                    c.getDouble(c.getColumnIndex("long")),
                    Timestamp.valueOf( c.getString(c.getColumnIndex("timestamp")))
                   );
            Log.v(TAG, temp.toString());
            HistoryItems.add(temp);
        }
        return HistoryItems;
    }

    public void clear(){
        SQLiteDatabase db = ts.getWritableDatabase();
        //db.execSQL("DROP TABLE IF EXISTS '" + DATABASE_NAME + "'");
        db.execSQL("DELETE FROM " + DATABASE_NAME);
        //db.execSQL("DROP " + DATABASE_NAME);
    }

    public void initTestCenter(){
        Log.v(TAG, "Start method initTestCenter");
        addHistoryItem("LA1", 12.32, 415.23, Timestamp.valueOf("2020-04-23 13:19:27.627"));
        addHistoryItem("LA2", 13.32, 413.23, Timestamp.valueOf("2021-03-23 13:19:27.627"));
        addHistoryItem("LA3", 14.32, 411.23, Timestamp.valueOf("2021-04-11 13:19:27.627"));
        addHistoryItem("LA4", 122.32, 41.23, Timestamp.valueOf("2021-04-12 13:19:27.627"));
        addHistoryItem("LA5", 122.32, 41.23, Timestamp.valueOf("2021-04-12 13:19:27.627"));
        addHistoryItem("LA6", 122.32, 41.23, Timestamp.valueOf("2021-04-22 13:19:27.627"));
        addHistoryItem("LA7", 122.32, 41.23, new Timestamp(System.currentTimeMillis()));
    }

    /*
     retrieve by DATE which belongs to java.util public class Date
     to init custom simply call new Date() to get current date or new Date
     for custom use
     public Date(int year,
            int month,
            int date)
     */

    public ArrayList<HistoryItem>  retrieveByDate(Date day){
        Log.v(TAG, "Start method retrieveByDate");
        ArrayList<HistoryItem> HistoryItems = new ArrayList<HistoryItem>();
        SQLiteDatabase db = ts.getReadableDatabase();
        String dateFormat = new SimpleDateFormat("yyyy-MM-dd").format(day);

        Log.v(TAG, "day: "+ dateFormat);
        Cursor c = db.query(DATABASE_NAME,null,"DATE(timestamp) = "+ "'" + dateFormat + "'", null,null,null,null);
        while (c.moveToNext()) {
            HistoryItem temp = new HistoryItem(c.getString(c.getColumnIndex("name")),
                    c.getDouble(c.getColumnIndex("lat")),
                    c.getDouble(c.getColumnIndex("long")),
                    Timestamp.valueOf( c.getString(c.getColumnIndex("timestamp")))
            );
            Log.v(TAG, temp.toString());
            HistoryItems.add(temp);
        }
        return HistoryItems;
    }


}