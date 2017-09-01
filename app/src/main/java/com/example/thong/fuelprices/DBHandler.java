package com.example.thong.fuelprices;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Thong on 26/08/2017.
 */

public class DBHandler extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDBName.db";
    public DBHandler(Context context) {
        super(context,DATABASE_NAME,null,1);
        SQLiteDatabase db = this.getWritableDatabase();
        onUpgrade(db,1 ,1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Gas_Station (" +
                "Latitude DOUBLE," +
                "Longitude DOUBLE," +
                "Name TEXT," +
                "U91 INT," +
                "U95 INT," +
                "U98 INT," +
                "Diesel INT," +
                "LPG INT," +
                "E10 INT," +
                "E85 INT)");
        db.execSQL("CREATE TABLE Deleted (" +
                "Latitude DOUBLE," +
                "Longitude DOUBLE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Gas_station");
        db.execSQL("DROP TABLE IF EXISTS Deleted");
        onCreate(db);
    }

    public boolean insertStation (String name, double longitude, double latitude) {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.i("curr", "CLICKEDDDin");
        Cursor res =  db.rawQuery( "SELECT * FROM Gas_Station WHERE Longitude = "+longitude + " AND Latitude = " + latitude, null);
        res.moveToFirst();
        Cursor resDelete =  db.rawQuery( "SELECT * FROM Deleted WHERE Longitude = "+longitude + " AND Latitude = " + latitude, null);
        resDelete.moveToFirst();
        Log.i("curr", "CLICKEDDDin");
        if ((res.isAfterLast() == true) && (resDelete.isAfterLast() == true)) {
            Log.i("curr", "CLICKEDDDcreate");
            db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("Name", name);
            contentValues.put("Longitude", longitude);
            contentValues.put("Latitude", latitude);
            contentValues.put("U91", -1);
            contentValues.put("U95", -1);
            contentValues.put("U98", -1);
            contentValues.put("Diesel", -1);
            contentValues.put("LPG", -1);
            contentValues.put("E10", -1);
            contentValues.put("E85", -1);
            db.insert("Gas_Station", null, contentValues);
            return true;
        }
        return false;
    }
    public boolean updateU91 (double longitude, double latitude, int u91) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("U91", u91);
        db.update("Gas_Station", contentValues, "Longitude ="+ longitude + " Latitude ="+ latitude,null);
        return true;
    }
    public boolean updateU95 (double longitude, double latitude, int u95) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("U95", u95);
        db.update("Gas_Station", contentValues, "Longitude ="+ longitude + " Latitude ="+ latitude,null);
        return true;
    }
    public boolean updateU98 (double longitude, double latitude, int u98) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("U98", u98);
        db.update("Gas_Station", contentValues, "Longitude ="+ longitude + " Latitude ="+ latitude,null);
        return true;
    }
    public boolean updateDiesel (double longitude, double latitude, int Diesel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Diesel", Diesel);
        db.update("Gas_Station", contentValues, "Longitude ="+ longitude + " Latitude ="+ latitude,null);
        return true;
    }
    public boolean updateLPG (double longitude, double latitude, int LPG) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("LPG", LPG);
        db.update("Gas_Station", contentValues, "Longitude ="+ longitude + " Latitude ="+ latitude,null);
        return true;
    }
    public boolean updateE10 (double longitude, double latitude, int E10) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("E10", E10);
        db.update("Gas_Station", contentValues, "Longitude ="+ longitude + " Latitude ="+ latitude,null);
        return true;
    }
    public boolean updateE85 (double longitude, double latitude, int E85) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("E85", E85);
        db.update("Gas_Station", contentValues, "Longitude ="+ longitude + " Latitude ="+ latitude,null);
        return true;
    }
    public Cursor getStation (double longitude, double latitude) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM Gas_Station WHERE Longitude = "+longitude + " AND Latitude = " + latitude, null );
        return res;
    }
    public boolean deleteStation(double longitude, double latitude) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL( "DELETE * FROM Gas_Station WHERE Longitude = "+longitude + " AND Latitude = " + latitude);
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Longitude", longitude);
        contentValues.put("Latitude", latitude);
        db.insert("Deleted", null, contentValues);
        return true;
    }
}
