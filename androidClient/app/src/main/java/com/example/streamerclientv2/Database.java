package com.example.streamerclientv2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {




    public static class DatabaseRecord{

        private String username;
        private String pass;
        private String machine_id;
        private String machine_name;
        private String current_IP;
        private String is_online;
        public byte type; // 1 if record from table "users", 2 if from table machines


//        public DatabaseRecord(String username, String pass){
//
//            this.username = username;
//            this.pass = pass;
//            type = 1;
//        }

        public DatabaseRecord(String machine_id, String machine_name, String current_IP, String is_online){

            this.machine_id = machine_id;
            this.machine_name = machine_name;
            this.current_IP = current_IP;
            this.is_online = is_online;
            type = 2;

        }

        public String getMachine_id(){if (type == 2) return machine_id; else return null;}
        public String getCurrent_IP(){ if (type == 2) return current_IP; else return null; }
        public String getMachine_name(){
            if (type == 2) return machine_name; else return null;
        }
        public String isOnline(){if (type == 2) return is_online; else return null;}

        @Override
        public String toString(){

            if(type == 2)
                //return "user_id: " + user_id + " , machine_name: " + machine_name + " , current_IP:" + current_IP;
                return "machine_id: " + machine_id + " , machine_name: " + machine_name + " , current_IP: " + current_IP + ", is_online: " + is_online;
            return "weird record";
        }

    }









    public static final String DATABASE_NAME = "users.db";
    //public String CONTACTS_TABLE_NAME = "user1";
    public static final String COLUMN_MACHINE_ID = "machine_id";
    public static final String COLUMN_MACHINE_NAME = "machine_name";
    public static final String COLUMN_CURRENT_IP = "current_IP";
    public static final String COLUMN_IS_ONLINE = "is_online";


    public Database(Context context) {
        super(context, DATABASE_NAME , null, 1);

    }


    @Override
    public void onCreate(SQLiteDatabase db) {


        db.execSQL(
                "create table machines" +
                        "(machine_id INTEGER primary key, machine_name VARCHAR2(100), current_IP VARCHAR2(25), is_online INTEGER)"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS machines");
        onCreate(db);
    }

    public void remakeMachinesTable(){

        // sa il fac rezistent la SQL injection
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS machines" );
//        db.execSQL(
//                "create table machines" +
//                        "(id integer primary key, user_id INTEGER, machine_name VARCHAR2(100), current_IP VARCHAR2(25))"
//        );

        db.execSQL(
                "create table machines" +
                        "(machine_id INTEGER primary key, machine_name VARCHAR2(100), current_IP VARCHAR2(25), is_online INTEGER)"
        );

    }

    @Deprecated
    public boolean insertRecord(String IP, String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("IP", IP);
        contentValues.put("path", path);
        db.insert("user", null, contentValues);
        return true;
    }

    public boolean insertRecord(DatabaseRecord record){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // record from table machines
        if (record.type == 2){

            contentValues.put(COLUMN_MACHINE_ID, record.getMachine_id());
            contentValues.put(COLUMN_MACHINE_NAME, record.getMachine_name());
            contentValues.put(COLUMN_CURRENT_IP, record.getCurrent_IP());
            contentValues.put(COLUMN_IS_ONLINE, record.isOnline());
            db.insert("machines", null, contentValues);
        }


        return true;

    }





    public ArrayList<DatabaseRecord> getAllRows() {

        ArrayList<DatabaseRecord> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "select * from machines", null );
        cursor.moveToFirst();

        while(cursor.isAfterLast() == false){
//                    array_list.add(cursor.getString(cursor.getColumnIndex(COLUMN_ID)) +
//                            cursor.getString(cursor.getColumnIndex(COLUMN_IP)) +
//                            cursor.getString(cursor.getColumnIndex(COLUMN_PATH)));

//            array_list.add(new DatabaseRecord(cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID)),
//                    cursor.getString(cursor.getColumnIndex(COLUMN_MACHINE_NAME)), cursor.getString(cursor.getColumnIndex(COLUMN_CURRENT_IP))));

            array_list.add(new DatabaseRecord(cursor.getString(cursor.getColumnIndex(COLUMN_MACHINE_ID)), cursor.getString(cursor.getColumnIndex(COLUMN_MACHINE_NAME)), cursor.getString(cursor.getColumnIndex(COLUMN_CURRENT_IP)), cursor.getString(cursor.getColumnIndex(COLUMN_IS_ONLINE))));
            cursor.moveToNext();
        }
        return array_list;
    }


}