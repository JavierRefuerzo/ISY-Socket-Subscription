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



package com.voiceforiot.isycustomsocket.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.voiceforiot.isycustomsocket.R;
import com.voiceforiot.isycustomsocket.constants.UriValues;
import com.voiceforiot.isycustomsocket.data.DatabaseContract;


/**
 * Created by Refuerzo on 11/12/2016.
 */

public class TwoItemCursorAdapter extends CursorAdapter {

    //variable which holds the uri sent to the constructor
    Uri mTableUri;


    private static final String LOG_TAG = TwoItemCursorAdapter.class.getName();


    //Constructor..
    //Added Uri tableUri to use the same adapter with many database tables
    public TwoItemCursorAdapter(Context context, Cursor c, Uri tableUri) {
        super(context, c, 0 /* flags */);
        mTableUri = tableUri;
        Log.v(LOG_TAG, "tableUri is: " + mTableUri);
    }

    //Makes a new blank list item view. No data is set (or bound) to the views yet
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //inflate a view from a layout
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView nameTextView = (TextView) view.findViewById(R.id.location_name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.device_name);
        String listItemName = getListItemName(context,cursor);
        String listSubItemDescription = getListItemDescription(context,cursor);
        // Populate fields with extracted properties
        nameTextView.setText((listItemName));
        summaryTextView.setText(listSubItemDescription);
    }


    //helper method to get the List Item Name
    private String getListItemName(Context context, Cursor cursor) {
        String listItemName = "List item name not set see TwoItemCursorAdapter.java";;
        final int uriMatch = UriValues.sUriMatcher.match(mTableUri);
        switch (uriMatch) {
            case UriValues.HUBS:
                listItemName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HubEntry.COLUMN_HUB_NAME));
                break;
            case UriValues.LOGS:
                listItemName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.LogEntry.COLUMN_BODY));
                break;
            default:
                break;
        }
        return listItemName;
    }

    //helper method to get the List item description
    private String getListItemDescription(Context context, Cursor cursor) {
        String listItemDescription = null;
        final int uriMatch = UriValues.sUriMatcher.match(mTableUri);
        switch (uriMatch) {
            case UriValues.HUBS:
                listItemDescription = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_IP_ADDRESS));
                break;
            case UriValues.LOGS:
                listItemDescription = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.LogEntry.COLUMN_TIME));
                break;
            default:
                listItemDescription = "List item description not set see CursorAdapter.java";
                break;
        }

        return listItemDescription;
    }





}
