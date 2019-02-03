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

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.voiceforiot.isycustomsocket.BuildConfig;

public class DatabaseContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DatabaseContract(){}

    //Content Authority variable name will be prefixed with the base application id, same as android manifest
    //Done this way so build flavors do not have a conflicting content provider
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID + ".custom_socket";


    //Create the base of all URI's which app(s) will use to contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    /**
     * Below are table names
     * Note: these names are appended to BASE_CONTENT_URI for possible URI's
     */

    //Tables in Database
    public static final String PATH_HUBS = "hubs";
    public static final String PATH_LOGS = "logs";
    //Join Tables
    //ADD JOIN TABLE NAMES HERE. NONE AT THIS TIME


    //Inner class that defines the table contents of the hubs table
    public static final class HubEntry implements BaseColumns {
        //The content URI used to access the Hubs table in the DatabaseProvider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_HUBS);
        //Table name. Same as the URI path
        public static final String TABLE_NAME = PATH_HUBS;
        //column names
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_HUB_NAME = "name";
        public static final String COLUMN_HUB_LOCAL_IP_ADDRESS = "local_ip_address";
        public static final String COLUMN_HUB_LOCAL_USERNAME = "local_username";
        public static final String COLUMN_HUB_LOCAL_PASSWORD = "local_password";
        //Future use. Not used at this time but will be needed in the future, so build DB correctly.
        public static final String COLUMN_HUB_REMOTE_URL = "remote_url";
        public static final String COLUMN_HUB_REMOTE_USERNAME = "remote_username";
        public static final String COLUMN_HUB_REMOTE_PASSWORD = "remote_password";
        public static final String COLUMN_ALWAYS_LOCAL = "always_local";
        //The MIME type of CONTENT_URI for all items in DataBase
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HUBS;
        //The MIME type of CONTENT_URI for a single item in the DataBase
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HUBS;
    }

    //Inner class that defines the table contents of the logs table
    public static final class LogEntry implements BaseColumns {
        //The content URI used to access the Logs table in the DatabaseProvider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_LOGS);
        //Table name. Same as the URI path
        public static final String TABLE_NAME = PATH_LOGS;
        //column names
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_HUB_ID = "hub_id";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_HEADER = "header";
        public static final String COLUMN_BODY = "body";
        //The MIME type of CONTENT_URI for all items in DataBase
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOGS;
        //The MIME type of CONTENT_URI for a single item in the DataBase
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOGS;
    }
}
