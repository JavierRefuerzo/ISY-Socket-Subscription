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


package com.voiceforiot.isycustomsocket.activities;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.Toast;

import com.voiceforiot.isycustomsocket.R;
import com.voiceforiot.isycustomsocket.adapters.TwoItemCursorAdapter;
import com.voiceforiot.isycustomsocket.data.DatabaseContract;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private String LOG_TAG = MainActivity.class.getSimpleName();

    private Button mButtonHubId;
    private ListPopupWindow mHubListPopupWindow;
    private FloatingActionButton mFabStart;

    private long mHubId = 0;

    //loader to setup spinner for hub
    private static final int HUB_TABLE_LOADER = 1001;
    private Cursor HUB_TABLE_CURSOR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonHubId = (Button) findViewById(R.id.button_for_spinner_hub);
        mButtonHubId.setOnClickListener(hubButtonClickListener);
        mFabStart = (FloatingActionButton) findViewById(R.id.fab_start);

        setOnClickListeners();

        //request cursors for other data needed
        getLoaderManager().initLoader(HUB_TABLE_LOADER, null , this);

    }


    private void setOnClickListeners(){
        mFabStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mHubId != 0){
                    Intent intent = new Intent(MainActivity.this, LogsActivity.class);
                    intent.putExtra("HubId", mHubId);
                    startActivity(intent);
                }else {
                    Toast.makeText(MainActivity.this, "Please select a Hub", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }


    //Hub Button Click Listener
    View.OnClickListener hubButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.v(LOG_TAG, "Hub Button Clicked");
            createHubListPopupWindow(HUB_TABLE_CURSOR);
        }
    };

    private void createHubListPopupWindow(final Cursor cursor){
        mHubListPopupWindow = new ListPopupWindow(MainActivity.this);
        CursorAdapter cursorAdapter = new TwoItemCursorAdapter(this, cursor, DatabaseContract.HubEntry.CONTENT_URI);
        mHubListPopupWindow.setAdapter(cursorAdapter);
        mHubListPopupWindow.setAnchorView(mButtonHubId);
        mHubListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //on list item click
                mHubListPopupWindow.dismiss();
                //set the hub id
                mHubId = id;
                Log.v(LOG_TAG, "mHubId is: " + mHubId);
                cursor.moveToPosition(position);
                String buttonString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HubEntry.COLUMN_HUB_NAME));
                mButtonHubId.setText(buttonString);
                //set the list window to null so it will not stop back button from being registered
                mHubListPopupWindow = null;
            }
        });
        mHubListPopupWindow.show();
        //Add a footer to the listView
        View view = View.inflate(getApplicationContext(), R.layout.button_item, null);
        Button footer = (Button) view.findViewById(R.id.button_cancel);
        footer.setText("Add New Hub");
        //get the listView from the ListViewPopupWindow, this is only valid when isShowing() == true
        ListView popupWindowListView = mHubListPopupWindow.getListView();
        //set a divider on the listView
        ColorDrawable dividerColor = new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.grayColor));
        popupWindowListView.setDivider(dividerColor);
        popupWindowListView.setDividerHeight(1);
        //set a long click listener to edit an existing property
        popupWindowListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(LOG_TAG, "Property was LONG CLICKED");
                //make an intent to start the networks entry
                Intent intent = new Intent(getApplicationContext(), HubsActivity.class);
                Uri uriWithId = ContentUris.withAppendedId(DatabaseContract.HubEntry.CONTENT_URI, id);
                intent.setData(uriWithId);
                startActivity(intent);
                mHubListPopupWindow.dismiss();
                return false;
            }
        });
        popupWindowListView.addFooterView(view);
        //show the mPropertyListPopupWindow again so the button will show up without scrolling
        mHubListPopupWindow.show();

        //Set an onclick listener for the footer button
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //make an intent to start the networks entry
                Intent intent = new Intent(getApplicationContext(), HubsActivity.class);
                intent.setData(DatabaseContract.HubEntry.CONTENT_URI);
                startActivity(intent);
                mHubListPopupWindow.dismiss();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId){
            case HUB_TABLE_LOADER:
                String[] hubProjection = {
                        DatabaseContract.HubEntry._ID,
                        DatabaseContract.HubEntry.COLUMN_HUB_NAME,
                        DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_IP_ADDRESS
                };
                return new CursorLoader(
                        this,
                        DatabaseContract.HubEntry.CONTENT_URI,
                        hubProjection,
                        null,
                        null,
                        null
                );
            default:
                //An invalid loaderId was passed
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int loaderId = loader.getId();
        switch (loaderId){
            case HUB_TABLE_LOADER:
                HUB_TABLE_CURSOR = cursor;
                allDataLoaded();
                break;

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    //Method created to sync all data from the LoaderManager
    //so we can set the data on the Activity without errors
    //This is not fully implemented as only one loader is called at this time
    private void allDataLoaded() {
        mButtonHubId.setText(R.string.select_hub);
    }




    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause(){
        Log.v(LOG_TAG, "--On pause called--");
        //Close the list popup to prevent a leak
        if (mHubListPopupWindow != null){
            Log.v(LOG_TAG, "Attempting to close mNetworksListPopupWindow");
            mHubListPopupWindow.dismiss();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "- ON DESTROY -");
    }
}
