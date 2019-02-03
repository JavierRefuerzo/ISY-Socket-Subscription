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




package com.voiceforiot.isycustomsocket.cursorQueries;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.voiceforiot.isycustomsocket.data.DatabaseContract;
import com.voiceforiot.isycustomsocket.data.DatabaseHelper;
import com.voiceforiot.isycustomsocket.objects.HubObject;


/**
 * Created by Refuerzo on 1/31/2017.
 */

public class QueryHub {

    private static String LOG_TAG = QueryHub.class.getSimpleName();


    /**
     * Create a PRIVATE CONSTRUCTOR because no one should ever create a {@link QueryHub} object.
     * This class is only meant to hold ****static**** variables and methods, which can be accessed
     * directly from the class name and an object instance of {@link QueryHub} is not needed.
     */
    private QueryHub(){}


    /**
     * get relevant data from the Hubs table
     */
    public static HubObject getHubDataFromHubId(Context context, long HubId) {
        HubObject hubObject;
        String mLocalIpAddress;
        String mHubName;
        String mLocalUsername;
        String mLocalPassword;
        String mRemoteUrl;
        String mRemoteUsername;
        String mRemotePassword;
        DatabaseHelper keypadDbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = keypadDbHelper.getReadableDatabase();
        String[] projection = {
                DatabaseContract.HubEntry._ID,
                DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_IP_ADDRESS,
                DatabaseContract.HubEntry.COLUMN_HUB_NAME,
                DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_USERNAME,
                DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_PASSWORD,
                DatabaseContract.HubEntry.COLUMN_HUB_REMOTE_URL,
                DatabaseContract.HubEntry.COLUMN_HUB_REMOTE_USERNAME,
                DatabaseContract.HubEntry.COLUMN_HUB_REMOTE_PASSWORD
        };
        String selectionClause = DatabaseContract.HubEntry._ID + " == " + HubId;
        Cursor hubsCursor = db.query(
                DatabaseContract.HubEntry.TABLE_NAME,
                projection,
                selectionClause,
                null,
                null,
                null,
                null
        );
        if (hubsCursor.getCount() > 0){
            hubsCursor.moveToFirst();
            mLocalIpAddress = hubsCursor.getString(hubsCursor.getColumnIndexOrThrow(DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_IP_ADDRESS));
            mHubName = hubsCursor.getString(hubsCursor.getColumnIndexOrThrow(DatabaseContract.HubEntry.COLUMN_HUB_NAME));
            mLocalUsername = hubsCursor.getString(hubsCursor.getColumnIndexOrThrow(DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_USERNAME));
            mLocalPassword = hubsCursor.getString(hubsCursor.getColumnIndexOrThrow(DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_PASSWORD));
            mRemoteUrl = hubsCursor.getString(hubsCursor.getColumnIndexOrThrow(DatabaseContract.HubEntry.COLUMN_HUB_REMOTE_URL));
            mRemoteUsername = hubsCursor.getString(hubsCursor.getColumnIndexOrThrow(DatabaseContract.HubEntry.COLUMN_HUB_REMOTE_USERNAME));
            mRemotePassword = hubsCursor.getString(hubsCursor.getColumnIndexOrThrow(DatabaseContract.HubEntry.COLUMN_HUB_REMOTE_PASSWORD));
            hubObject = new HubObject(HubId,
                    mHubName,
                    mLocalIpAddress,
                    mLocalUsername,
                    mLocalPassword,
                    mRemoteUrl,
                    mRemoteUsername,
                    mRemotePassword
            );

        }else {
            Log.v(LOG_TAG, "could not find hub by ID, see getHubData");
            hubObject = null;
        }
        hubsCursor.close();
        db.close();



        return hubObject;
    }


}
