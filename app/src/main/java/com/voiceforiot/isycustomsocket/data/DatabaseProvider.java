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


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.voiceforiot.isycustomsocket.R;
import com.voiceforiot.isycustomsocket.constants.UriValues;


public class DatabaseProvider extends ContentProvider {

    //Log Tag
    private static final String LOG_TAG = DatabaseProvider.class.getSimpleName();

    //DatabaseHelper
    private DatabaseHelper mDatabaseHelper;


    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // This cursor will hold the result of the query
        Cursor cursor;
        //NOTE: Joined Tables (not implemented at this time) may require SQLiteQueryBuilder
        //SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();

        //URI matcher can match the URI to a specific code
        //SQLite database object used for raw queries
        final int match = UriValues.sUriMatcher.match(uri);
        switch (match){
            case UriValues.HUBS:
                cursor = queryTable(uri,projection,selection,selectionArgs,sortOrder, DatabaseContract.HubEntry.TABLE_NAME);
                break;
            case UriValues.HUBS_ID:
                selection = DatabaseContract.HubEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = queryTable(uri,projection,selection,selectionArgs,sortOrder, DatabaseContract.HubEntry.TABLE_NAME);
                break;
            case UriValues.LOGS:
                cursor = queryTable(uri,projection,selection,selectionArgs,sortOrder, DatabaseContract.LogEntry.TABLE_NAME);
                break;
            case UriValues.LOGS_ID:
                selection = DatabaseContract.LogEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = queryTable(uri,projection,selection,selectionArgs,sortOrder, DatabaseContract.LogEntry.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(R.string.unknown_uri) + " : " + uri);
        }


        //Set notification URI on the Cursor
        //so whe know what content URI the Cursor was created for.
        //if the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        //Return the cursor
        return cursor;

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        //URI matcher can match the URI to a specific code
        //SQLite database object used for raw queries
        final int match = UriValues.sUriMatcher.match(uri);

        //NOTE DIFFERENCE CONTENT_LIST_TYPE vs CONTENT_ITEM_TYPE
        switch (match){
            case UriValues.HUBS:
                return DatabaseContract.HubEntry.CONTENT_LIST_TYPE;
            case UriValues.HUBS_ID:
                return DatabaseContract.HubEntry.CONTENT_ITEM_TYPE;
            case UriValues.LOGS:
                return DatabaseContract.LogEntry.CONTENT_LIST_TYPE;
            case UriValues.LOGS_ID:
                return DatabaseContract.LogEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException(String.valueOf(R.string.unknown_uri) + " : " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        //URI matcher can match the URI to a specific code
        //SQLite database object used for raw queries
        final int match = UriValues.sUriMatcher.match(uri);

        //ONLY ONE CASE STATEMENT per Uri (no uri/#) AS WE CANNOT INSERT A NEW ROW INTO AN EXISTING ROW
        switch (match){
            case UriValues.HUBS:
                return insertHub(uri, contentValues);
            case UriValues.HUBS_ID:
                throw new IllegalArgumentException(String.valueOf(R.string.unsupported_uri) + " : " + uri);
            case UriValues.LOGS:
                return insertLog(uri, contentValues);
            case UriValues.LOGS_ID:
                throw new IllegalArgumentException(String.valueOf(R.string.unsupported_uri) + " : " + uri);
            default:
                throw new IllegalArgumentException(String.valueOf(R.string.unknown_uri) + " : " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        //URI matcher can match the URI to a specific code
        //SQLite database object used for raw queries
        final int match = UriValues.sUriMatcher.match(uri);

        switch (match){
            case UriValues.HUBS:
                return deleteFromTable(uri, selection, selectionArgs, DatabaseContract.HubEntry.TABLE_NAME);
            case UriValues.HUBS_ID:
                // For the userPin _ID key, extract out the ID from the URI,
                //Note that ? is a wild card for selectionArgs (arguments)
                selection = DatabaseContract.HubEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return deleteFromTable(uri, selection, selectionArgs, DatabaseContract.HubEntry.TABLE_NAME);
            case UriValues.LOGS:
                return deleteFromTable(uri, selection, selectionArgs, DatabaseContract.LogEntry.TABLE_NAME);
            case UriValues.LOGS_ID:
                // For the userPin _ID key, extract out the ID from the URI,
                //Note that ? is a wild card for selectionArgs (arguments)
                selection = DatabaseContract.HubEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return deleteFromTable(uri, selection, selectionArgs, DatabaseContract.LogEntry.TABLE_NAME);
            default:
                throw new IllegalArgumentException(String.valueOf(R.string.unknown_uri) + " : " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        //URI matcher can match the URI to a specific code
        //SQLite database object used for raw queries
        final int match = UriValues.sUriMatcher.match(uri);

        switch (match){
            case UriValues.HUBS:
                return updateHubData(uri, contentValues, selection, selectionArgs);
            case UriValues.HUBS_ID:
                // For the userPin _ID key, extract out the ID from the URI,
                ////Note that ? is a wild card for selectionArgs (arguments)
                selection = DatabaseContract.HubEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateHubData(uri, contentValues, selection, selectionArgs);
            case UriValues.LOGS:
                return updateLogData(uri, contentValues, selection, selectionArgs);
            case UriValues.LOGS_ID:
                // For the userPin _ID key, extract out the ID from the URI,
                ////Note that ? is a wild card for selectionArgs (arguments)
                selection = DatabaseContract.LogEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateLogData(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(String.valueOf(R.string.unknown_uri) + " : " + uri);
        }
    }



    /////////////////////////Query Helper Methods///////////////////////////////
    //Query Table
    private Cursor queryTable(Uri uri, String[] projection, String selection, String[] selectionArgs,
                              String sortOrder, String tableName){
        //Get readable database
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        //Query database
        return database.query(tableName, projection, selection, selectionArgs,null,null, sortOrder);
    }

    /////////////////////DELETE HELPER METHODS/////////////////////////////////

    //delete items from table
    private int deleteFromTable(Uri uri, String selection, String[] selectionArgs, String tableName){
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        int rowsDeleted = database.delete(tableName, selection,selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }



    ///////////////////////Insert Helper Methods///////////////////////////////////

    //helper method to insert data into tableName
    private Uri insertIntoTable(Uri uri, ContentValues contentValues, String tableName) {
        // Get writable database
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        long id = database.insert(tableName, null, contentValues);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        //Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }


    //insert into hubs table
    private Uri insertHub(Uri uri, ContentValues contentValues) {

        //check that Hub name is not null
        String hubName = contentValues.getAsString(DatabaseContract.HubEntry.COLUMN_HUB_NAME);
        if (hubName == null || hubName.isEmpty()) {
            throw new IllegalArgumentException("Hub requires a name");
        }
        return insertIntoTable(uri, contentValues, DatabaseContract.HubEntry.TABLE_NAME);
    }


    //insert into logs table
    private Uri insertLog(Uri uri, ContentValues contentValues) {

        //check that a hub id exists
        Integer hubId = contentValues.getAsInteger(DatabaseContract.LogEntry.COLUMN_HUB_ID);
        if (hubId == null) {
            throw new IllegalArgumentException("Log requires a hub ID");
        }
        //check that the time exists
        Long time = contentValues.getAsLong(DatabaseContract.LogEntry.COLUMN_TIME);
        if (time == null){
            throw new IllegalArgumentException("Log requires a time");
        }
        //check that the header exists
        String header = contentValues.getAsString(DatabaseContract.LogEntry.COLUMN_HEADER);
        if (header == null || header.isEmpty()){
            throw new IllegalArgumentException("Log requires a header");
        }
        //check that the body exists
        String body = contentValues.getAsString(DatabaseContract.LogEntry.COLUMN_BODY);
        if (body == null || body.isEmpty()){
            throw new IllegalArgumentException("Log requires a body");
        }
        return insertIntoTable(uri, contentValues, DatabaseContract.LogEntry.TABLE_NAME);
    }


    ///////////////////////UPDATE HELPER METHODS/////////////////////////////////

    ///update table, helper method to update any table with "tableName"
    private int updateTable (Uri uri, ContentValues contentValues, String selection, String[] selectionArgs, String tableName){
        // If there are no values to update, then don't try to update the database
        if (contentValues.size() == 0) {
            return 0;
        }
        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();

        //Preform the update(s) on the database and get the number of rows affected
        int rowsUpdated = database.update(tableName, contentValues, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            //Notify all listeners that the data has changed for the pet content URI
            getContext().getContentResolver().notifyChange(uri, null);
        }
        //return the number of rows updated
        return rowsUpdated;
    }


    //Hubs Table update
    private int updateHubData(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        //Data Validation
        //check that name has a value
        if (contentValues.containsKey(DatabaseContract.HubEntry.COLUMN_HUB_NAME)){
            String name  = contentValues.getAsString(DatabaseContract.HubEntry.COLUMN_HUB_NAME);
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Hub requires a name");
            }
        }
        return updateTable(uri, contentValues,selection,selectionArgs, DatabaseContract.HubEntry.TABLE_NAME);
    }

    //Logs Table update. NOTE this should not happen but here for consistency
    private int updateLogData(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        //check that a hub id exists
        if (contentValues.containsKey(DatabaseContract.LogEntry.COLUMN_HUB_ID)){
            Integer hubId = contentValues.getAsInteger(DatabaseContract.LogEntry.COLUMN_HUB_ID);
            if (hubId == null) {
                throw new IllegalArgumentException("Log requires a hub ID");
            }
        }

        //check that the time exists
        if (contentValues.containsKey(DatabaseContract.LogEntry.COLUMN_TIME)){
            Long time = contentValues.getAsLong(DatabaseContract.LogEntry.COLUMN_TIME);
            if (time == null){
                throw new IllegalArgumentException("Log requires a time");
            }
        }

        //check that the header exists
        if (contentValues.containsKey(DatabaseContract.LogEntry.COLUMN_HEADER)){
            String header = contentValues.getAsString(DatabaseContract.LogEntry.COLUMN_HEADER);
            if (header == null || header.isEmpty()){
                throw new IllegalArgumentException("Log requires a header");
            }
        }

        //check that the body exists
        if (contentValues.containsKey(DatabaseContract.LogEntry.COLUMN_BODY)){
            String body = contentValues.getAsString(DatabaseContract.LogEntry.COLUMN_BODY);
            if (body == null || body.isEmpty()){
                throw new IllegalArgumentException("Log requires a body");
            }
        }

        return updateTable(uri, contentValues,selection,selectionArgs, DatabaseContract.LogEntry.TABLE_NAME);
    }


}
