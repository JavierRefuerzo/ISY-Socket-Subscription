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

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.voiceforiot.isycustomsocket.R;
import com.voiceforiot.isycustomsocket.adapters.TwoItemCursorAdapter;
import com.voiceforiot.isycustomsocket.constants.SecurityConstants;
import com.voiceforiot.isycustomsocket.data.DatabaseContract;
import com.voiceforiot.isycustomsocket.dataRequestUtils.Crypto;
import com.voiceforiot.isycustomsocket.interfaces.SocketListener;
import com.voiceforiot.isycustomsocket.objects.SocketObject;

import java.io.IOException;
import java.util.Calendar;

public class LogsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private String LOG_TAG = LogsActivity.class.getSimpleName();

    private long mHubId = 0;

    //loaders
    private static final int LOGS_TABLE_LOADER = 1001;
    private static final int HUB_TABLE_LOADER = 1002;

    //Adapter for listView
    private TwoItemCursorAdapter mCursorAdapter;

    private ListView mListView;
    private FloatingActionButton mFab;
    private ProgressBar mProgressBar;
    //Empty View
    private View mEmptyView;
    private TextView mEmptyTitleText;
    private TextView mEmptyDescriptionText;

    private boolean mIsConnected = false;

    private SocketObject mSocketObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mListView = (ListView) findViewById(R.id.sub_settings_list_view);
        mFab = (FloatingActionButton) findViewById(R.id.fab_start);
        //get to progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_loader);
        //get the empty view
        mEmptyView = findViewById(R.id.empty_list_view);
        mEmptyTitleText = findViewById(R.id.empty_title_text);
        mEmptyDescriptionText = findViewById(R.id.empty_subtitle_text);
        //set the empty view on the list View
        mListView.setEmptyView(mEmptyView);


        //get the intent
        Intent intent = getIntent();
        if (intent != null){
            mHubId = intent.getLongExtra("HubId", 0);
        }

        setOnClickListeners();

        //Setup an adapter to create a list item for each row of  data in the Cursor.
        //There is no data yet (until the loader finishes) so pass in null for the Cursor.
        setAdapter();

    }


    //Creates the Menu button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuItem3 = menu.findItem(R.id.action_save);
        menuItem3.setVisible(false);

        return true;
    }

    //defines what happens when a menu item is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //This should not be visible
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_help:
                Log.v(LOG_TAG, "Help button clicked");
                goToUrl("https://voiceforiot.wordpress.com/setup/#hub");
                return true;
            case android.R.id.home:
                //NOTE THIS HAS BEEN REMOVED FROM MANIFEST
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteLogs();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Perform the deletion of the hub in the database.
     */
    private void deleteLogs() {
        int numOfRowsDeleted = 0;

        String selectionClause =
                DatabaseContract.LogEntry.COLUMN_HUB_ID + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(mHubId)};

            numOfRowsDeleted = getContentResolver().delete(DatabaseContract.LogEntry.CONTENT_URI, selectionClause, selectionArgs);
            //Show a toast message with informatics regarding deletion
            if (numOfRowsDeleted == 0) {
                // If the new content URI is 0, then no rows where deleted.
                Toast.makeText(this, getString(R.string.editor_delete_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the deletion was successful and we can display the number of rows deleted as a toast.
                String stringNumber = String.valueOf(numOfRowsDeleted);
                Toast.makeText(this, getString(R.string.editor_delete_successful) + "\nNumber of rows deleted :" + stringNumber ,
                        Toast.LENGTH_SHORT).show();
            }
    }

    //send user to url
    private void goToUrl(String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        Log.v(LOG_TAG, "Intent started");
        startActivity(intent);
    }

    private void setOnClickListeners(){
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsConnected){
                    Log.v(LOG_TAG, "mFab Clicked with true");
                    try {
                        if (mSocketObject != null){
                            mProgressBar.setVisibility(View.VISIBLE);
                            mSocketObject.closeSocket();
                        }
                    } catch (IOException e) {
                        Log.v(LOG_TAG, "IOException: " + e);
                    }
                }else {
                    Log.v(LOG_TAG, "mFab Clicked with false");
                    getLoaderManager().initLoader(HUB_TABLE_LOADER, null , LogsActivity.this);
                }
            }
        });
    }

    private void setAdapter(){
        mCursorAdapter = new TwoItemCursorAdapter(this, null, DatabaseContract.LogEntry.CONTENT_URI);
        mListView.setAdapter(mCursorAdapter);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

        String[] projection;
        String selectionClause;
        String[] selectionArgs;
        String sortOrder;

        switch (loaderId){
            case LOGS_TABLE_LOADER:
                mProgressBar.setVisibility(View.VISIBLE);
                projection = new String[]{
                        DatabaseContract.LogEntry._ID,
                        DatabaseContract.LogEntry.COLUMN_HUB_ID,
                        DatabaseContract.LogEntry.COLUMN_TIME,
                        DatabaseContract.LogEntry.COLUMN_BODY
                };
                selectionClause =
                        DatabaseContract.LogEntry.COLUMN_HUB_ID + " = ? ";
                selectionArgs = new String[] {String.valueOf(mHubId)};
                sortOrder =
                        DatabaseContract.LogEntry.COLUMN_TIME + " " + "DESC";
                return new CursorLoader(
                        this,
                        DatabaseContract.LogEntry.CONTENT_URI,
                        projection,
                        selectionClause,
                        selectionArgs,
                        sortOrder
                );
            case HUB_TABLE_LOADER:
                mProgressBar.setVisibility(View.VISIBLE);
                projection = new String[] {
                        DatabaseContract.HubEntry._ID,
                        DatabaseContract.HubEntry.COLUMN_HUB_NAME,
                        DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_IP_ADDRESS,
                        DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_USERNAME,
                        DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_PASSWORD,
                };
                selectionClause =
                        DatabaseContract.HubEntry._ID + " = ? ";
                selectionArgs = new String[]{String.valueOf(mHubId)};
                return new CursorLoader(
                        this,
                        DatabaseContract.HubEntry.CONTENT_URI,
                        projection,
                        selectionClause,
                        selectionArgs,
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
            case LOGS_TABLE_LOADER:
                mCursorAdapter.swapCursor(cursor);
                mProgressBar.setVisibility(View.GONE);
                break;
            case HUB_TABLE_LOADER:
                startSocket(cursor);
                mProgressBar.setVisibility(View.GONE);
                break;
            default:
                Log.v(LOG_TAG, "an invalid loader Id was passed");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    private void startSocket(Cursor cursor){
        cursor.moveToFirst();
        String passwordEncrypted = cursor.getString(cursor.getColumnIndex(DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_PASSWORD));
        String decrypted = "";
        try {
            decrypted = Crypto.decryptString(LogsActivity.this, SecurityConstants.KEYSTORE_ALIAS, passwordEncrypted);
        } catch (Exception e) {
            Log.v(LOG_TAG, "Decryption Error: " + e);
            Toast.makeText(this, "Error decrypting local password please reset password\n" + e,
                    Toast.LENGTH_LONG).show();
        }
        mSocketObject = new SocketObject();
        mSocketObject.addSocketListener(socketListener);
        mSocketObject.addUrl(cursor.getString(cursor.getColumnIndex(DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_IP_ADDRESS)));
        mSocketObject.addUserName(cursor.getString(cursor.getColumnIndex(DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_USERNAME)));
        mSocketObject.addPassword(decrypted);
        mSocketObject.startSocket();
    }

    private void setIsConnected(boolean isConnected) {
        if (isConnected) {
            Log.v(LOG_TAG, "Is Connected True Called");
            mIsConnected = true;
            mFab.setImageDrawable(ContextCompat.getDrawable(LogsActivity.this, R.drawable.baseline_stop_white_24));
        } else {
            mIsConnected = false;
            mProgressBar.setVisibility(View.INVISIBLE);
            mFab.setImageDrawable(ContextCompat.getDrawable(LogsActivity.this, R.drawable.baseline_play_arrow_white_24));
            Log.v(LOG_TAG, "Is Connected False Called");
        }
    }


    SocketListener socketListener = new SocketListener() {
        @Override
        public void onOpen(String line, String header) {
//            Log.v(LOG_TAG, "onOpen header:\n" + header + "\nEND onOpen header");
//            Log.v(LOG_TAG, "onOpen xml:\n" + line + "\nEND onOpen xml");
            insertLog(header, line);
            setIsConnected(true);
        }

        @Override
        public void onClose(String lines) {
            insertLog("onClose", lines);
            Log.v(LOG_TAG, "onClose");
            setIsConnected(false);
        }

        @Override
        public void onError(String lines) {
            insertLog("onError", lines);
            Log.v(LOG_TAG, "onError");
            setIsConnected(false);
        }

        @Override
        public void onMessage(String line, String header) {
//            Log.v(LOG_TAG, "onMessage header:\n" + header + "\nEND header");
//            Log.v(LOG_TAG, "onMessage xml:\n" + line + "\nEND onMessage xml");
            insertLog(header, line);
        }
    };


    @Override
    public void onResume(){
        super.onResume();
        if (mHubId != 0){
            Log.v(LOG_TAG, "restarting loader onResume");
            //request cursors for other data needed
            getLoaderManager().initLoader(LOGS_TABLE_LOADER, null , this);

        }
    }

    @Override
    protected void onPause(){
        Log.v(LOG_TAG, "--On pause called--");
        try {
            if (mSocketObject != null){
                mSocketObject.closeSocket();
                setIsConnected(false);
            }
        } catch (IOException e) {
            Log.v(LOG_TAG, "IOException : " + e);
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

    private void insertLog(String header, String body){
        Calendar calendar = Calendar.getInstance();
        if (body == null || body.isEmpty()){
            body = "empty";
        }
        long time = calendar.getTimeInMillis();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.LogEntry.COLUMN_HUB_ID, mHubId);
        values.put(DatabaseContract.LogEntry.COLUMN_TIME, time);
        values.put(DatabaseContract.LogEntry.COLUMN_HEADER, header);
        values.put(DatabaseContract.LogEntry.COLUMN_BODY, body);
        //Use the content resolver to insert a new hub into the database
        Uri newUri = getContentResolver().insert(DatabaseContract.LogEntry.CONTENT_URI, values);
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_insert_failed),
                    Toast.LENGTH_SHORT).show();
        }

    }
}
