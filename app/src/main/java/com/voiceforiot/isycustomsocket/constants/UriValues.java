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



package com.voiceforiot.isycustomsocket.constants;

import android.content.UriMatcher;

import com.voiceforiot.isycustomsocket.data.DatabaseContract;

public class UriValues {

    // To prevent someone from accidentally instantiating the UriValues class,
    // make the constructor private. 
    private UriValues(){}

    //Create Human Readable names for ID (variables)  used int the URI matcher
    //public so variables can be used by other classes
    /** uri matcher coder for the entire hubs table*/
    public static final int HUBS = 101;
    /** uri matcher code for a single row in the hubs table */
    public static final int HUBS_ID = 102;
    /** uri matcher code for the entire logs table*/
    public static final int LOGS = 103;
    /** uri matcher code for a single row in the logs table */
    public static final int LOGS_ID = 104;



    //Initialize the URI Matcher
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        //Note "#" is a wild card for integer. so this must match / + an int (int for row Id).
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY, DatabaseContract.PATH_HUBS, HUBS);
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY, DatabaseContract.PATH_HUBS + "/#", HUBS_ID);
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY, DatabaseContract.PATH_LOGS, LOGS);
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY, DatabaseContract.PATH_LOGS + "/#", LOGS_ID);
        //NOTE: JOIN TABLES should be added here also (not implemented yet)
    }
}
