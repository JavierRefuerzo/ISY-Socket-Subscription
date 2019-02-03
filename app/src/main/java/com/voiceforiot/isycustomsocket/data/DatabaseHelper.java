/*
 * Copyright 2016 - 2019 Javier Refuerzo. Swansea Software LLC. Denver, CO. USA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.voiceforiot.isycustomsocket.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    //And implement the onUpgrade() method
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "custom_socket.db";
    private Context mContext;
    
    //Constructor
    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        
        //Creates the database with the Variable "SQL_CREATE_***" which is defined in this class
        //Note: "execSQL" only executes an action on the db, it does not return a value, so is good
        //in this situation (create a table) but not when data needs to be returned
        
        //allow tables to reference other tables' _ID
        db.execSQL("PRAGMA foreign_keys=ON");
        
        //create the hubs table
        db.execSQL(SQL_CREATE_HUBS_TABLE);
        //create the logs table
        db.execSQL(SQL_CREATE_LOGS_TABLE);
    }
    
    

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //allow tables to reference other tables' _ID
        sqLiteDatabase.execSQL("PRAGMA foreign_keys=ON");
    }


    //this is called when the database is opened
    //needed for foreign_keys to work
    @Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db);
        //allow tables to reference other tables' _ID
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    //Variables for sqLite types
    private static final String CREATE_TABLE = "CREATE TABLE ";
    private static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT = " INTEGER PRIMARY KEY AUTOINCREMENT ";
    private static final String TEXT_TYPE = " TEXT ";
    private static final String INTEGER_TYPE = " INTEGER ";
    private static final String NOT_NULL_TYPE = " NOT NULL ";
    private static final String UNIQUE_TYPE = " UNIQUE ";
    private static final String DEFAULT_TYPE = " DEFAULT ";
    private static final String FOREIGN_KEY = " FOREIGN KEY ";
    private static final String REFERENCES = " REFERENCES ";
    private static final String COMMA_SEP = ",";
    private static final String ON_CONFLICT_REPLACE = " ON CONFLICT REPLACE ";
    private static final String ON_DELETE_CASCADE = " ON DELETE CASCADE ";


    //Setup the hubs table
    private static final String SQL_CREATE_HUBS_TABLE =
            CREATE_TABLE + DatabaseContract.HubEntry.TABLE_NAME + " ( " +
                    DatabaseContract.HubEntry._ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT + COMMA_SEP +
                    DatabaseContract.HubEntry.COLUMN_HUB_NAME + TEXT_TYPE + NOT_NULL_TYPE + COMMA_SEP +
                    DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_IP_ADDRESS + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_USERNAME + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_PASSWORD + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.HubEntry.COLUMN_ALWAYS_LOCAL + INTEGER_TYPE + COMMA_SEP +
                    DatabaseContract.HubEntry.COLUMN_HUB_REMOTE_URL + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.HubEntry.COLUMN_HUB_REMOTE_USERNAME + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.HubEntry.COLUMN_HUB_REMOTE_PASSWORD + TEXT_TYPE +
                    COMMA_SEP +
                    UNIQUE_TYPE + " ( " + DatabaseContract.HubEntry.COLUMN_HUB_NAME + " ) " +
                    " )";


    //Setup the logs table
    private static final String SQL_CREATE_LOGS_TABLE =
            CREATE_TABLE + DatabaseContract.LogEntry.TABLE_NAME + " ( " +
                    DatabaseContract.LogEntry._ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT + COMMA_SEP +
                    DatabaseContract.LogEntry.COLUMN_HUB_ID + INTEGER_TYPE + NOT_NULL_TYPE + COMMA_SEP +
                    DatabaseContract.LogEntry.COLUMN_TIME + INTEGER_TYPE + NOT_NULL_TYPE + COMMA_SEP +
                    DatabaseContract.LogEntry.COLUMN_HEADER + TEXT_TYPE + NOT_NULL_TYPE + COMMA_SEP +
                    DatabaseContract.LogEntry.COLUMN_BODY + TEXT_TYPE + NOT_NULL_TYPE +
                    COMMA_SEP +
                    FOREIGN_KEY + " ( " + DatabaseContract.LogEntry.COLUMN_HUB_ID + " ) " +
                    REFERENCES + DatabaseContract.HubEntry.TABLE_NAME + " ( " +
                    DatabaseContract.HubEntry._ID + " ) " +
                    ON_DELETE_CASCADE +
                    " )";
    
}
