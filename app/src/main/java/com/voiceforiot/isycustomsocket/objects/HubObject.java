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



package com.voiceforiot.isycustomsocket.objects;


/**
 * Created by Refuerzo on 12/13/2016.
 */

public class HubObject {
    private  long m_ID;
    private String mHubName;
    private String mLocalIpAddress;
    private String mLocalUsername;
    private String mLocalPassword;
    private String mRemoteUrl;
    private String mRemoteUsername;
    private String mRemotePassword;

    /*Constructs a new Hub object*/
    public HubObject(long id, String hubName,
                     String localIpAddress, String localUsername, String localPassword,
                     String remoteUrl, String remoteUserName, String remotePassword){
        m_ID = id;
        mHubName = hubName;
        mLocalIpAddress = localIpAddress;
        mLocalUsername = localUsername;
        mLocalPassword = localPassword;
        mRemoteUrl = remoteUrl;
        mRemoteUsername = remoteUserName;
        mRemotePassword = remotePassword;
    }

    public long get_ID() { return m_ID; }
    public String getHubName() { return  mHubName; }

    public String getLocalIpAddress() { return  mLocalIpAddress; }
    public String getLocalUsername() { return  mLocalUsername; }
    public String getLocalPassword() { return mLocalPassword; }

    public String getRemoteUrl() { return mRemoteUrl; }
    public String getRemoteUsername() { return mRemoteUsername; }
    public String getRemotePassword() { return mRemotePassword; }


    public void setHubName(String hubName){
        mHubName = hubName;
    }
}
