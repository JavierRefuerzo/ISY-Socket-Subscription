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

import android.util.Base64;
import android.util.Log;
import com.voiceforiot.isycustomsocket.interfaces.SocketListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SocketObject {


    private String LOG_TAG = SocketObject.class.getSimpleName();

    private SocketListener mSocketListener;



    private PrintWriter mPrinterWriterOut;

    private BufferedReader mBufferedReaderIn;

    private String mUserName;

    private String mPassword;

    private String mUrl;

    SSLSocket mSSLSocket;
    Socket mSocket;

    public void addSocketListener(SocketListener socketListener){
        mSocketListener = socketListener;
    }

    public void addUserName(String userName){
        mUserName = userName;
    }

    public void addPassword(String password){
        mPassword = password;
    }

    public void addUrl(String url){
        mUrl = url;
    }

    public void startSocket(){
        this.backgroundThread = new Thread(attemptSocketConnection);
        this.backgroundThread.start();
    }

    public void closeSocket() throws IOException {
        this.closeBackgroundThread = new Thread(closeSocket);
        this.closeBackgroundThread.start();
////        if (mPrinterWriterOut != null){
////            mPrinterWriterOut.print("Unsubscribing from ISY");
////            mPrinterWriterOut.flush();
////        }
//        if (mBufferedReaderIn != null) {
//            mBufferedReaderIn.close();
//        }
//        if (mPrinterWriterOut != null) {
//            mPrinterWriterOut.close();
//        }
//        if (mSSLSocket != null){
//            mSSLSocket.close();
//        }
//        if (mSocket != null){
//            mSocket.close();
//        }
//        mSocketListener.onClose("");
    }

    private Thread closeBackgroundThread;

    private Runnable closeSocket = new Runnable() {
        @Override
        public void run() {
            Log.v(LOG_TAG, "Closing Socket");
            if (mPrinterWriterOut != null){
                mPrinterWriterOut.print("Unsubscribing from ISY");
                mPrinterWriterOut.flush();
            }
            if (mBufferedReaderIn != null){
                try {
                    mBufferedReaderIn.close();
                }catch (IOException e){

                }
            }
            if (mPrinterWriterOut != null){
                mPrinterWriterOut.close();
            }
            if (mSSLSocket != null){
                try {
                    mSSLSocket.close();
                }catch (IOException e){

                }
            }
            if (mSocket != null){
                try {
                    mSocket.close();
                }catch (IOException e){

                }
            }
            mSocketListener.onClose("This Application Closed Socket");
        }

    };


    private Thread backgroundThread;

    private Runnable attemptSocketConnection = new Runnable() {
        @Override
        public void run() {

            String usernamePassword = mUserName + ":" + mPassword;
            String base64UsernamePassword;

            try {
                byte[] usernamePasswordByte = usernamePassword.getBytes("UTF-8");
                //NO_WRAP NEEDED AS DEFAULT APPENDS NEW LINE CHAR
                base64UsernamePassword = Base64.encodeToString(usernamePasswordByte, Base64.NO_WRAP);
                Log.v(LOG_TAG, "base64 username is: " + base64UsernamePassword);
            } catch (UnsupportedEncodingException e) {
                mSocketListener.onError("UnsupportedEncodingException: " + e);
                //Log.v(LOG_TAG, "Error: " + e);
                return;
            }

            URL url = null;
            try {
                //get this from my.isy.io
                url = new URL(mUrl);
            } catch (MalformedURLException e) {
                mSocketListener.onError("MalformedURLException: " + e);
                return;
            }
            String urlString = url.toString();
            String host = url.getHost();

            String body = "<s:Envelope><s:Body>" +
                    "<u:Subscribe xmlns:u='urn:udi-com:service:X_Insteon_Lighting_Service:1'>" +
                    "<reportURL>REUSE_SOCKET</reportURL><duration>infinite</duration>" +
                    "</u:Subscribe></s:Body></s:Envelope>";

            String writeString = "POST /services HTTP/1.1\n" +
                    "Host: " + urlString +"\n" +
                    "Content-Type: text/xml; charset=utf-8\n" +
                    "Authorization: " + "Basic " + base64UsernamePassword + "\n" +
                    "Content-Length: " + (body.length()) + "\n" +
                    "SOAPAction: urn:udi-com:device:X_Insteon_Lighting_Service:1#Subscribe" +
                    "\r\n" +
                    "\r\n" +
                    body +
                    "\r\n";

            Log.v(LOG_TAG,"writeString\n" + writeString);

            try{

                //HTTP
                if (mUrl.startsWith("http://")){
                    SocketFactory factory =
                            (SocketFactory)SocketFactory.getDefault();
                    mSocket =
                            (Socket)factory.createSocket(host, 80);

                    //mSocket.startHandshake();

                    mPrinterWriterOut = new PrintWriter(
                            new BufferedWriter(
                                    new OutputStreamWriter(
                                            mSocket.getOutputStream())));
                    mPrinterWriterOut.print(writeString);
                    mPrinterWriterOut.print("");
                    mPrinterWriterOut.flush();

                    //check if any errors exist
                    if (mPrinterWriterOut.checkError()){
                        //Log.v(LOG_TAG, "SSLSocketClient: java.io.PrintWriter error");
                        mSocketListener.onError("SocketClient: java.io.PrintWriter error");
                        return;
                    }

                    //Read
                    mBufferedReaderIn = new BufferedReader(
                            new InputStreamReader(
                                    mSocket.getInputStream()));
                }
                //HTTPS
                else if (mUrl.startsWith("https://")){
                    SSLSocketFactory factory =
                            (SSLSocketFactory)SSLSocketFactory.getDefault();
                    mSSLSocket =
                            (SSLSocket)factory.createSocket(host, 443);

                    mSSLSocket.startHandshake();

                    mPrinterWriterOut = new PrintWriter(
                            new BufferedWriter(
                                    new OutputStreamWriter(
                                            mSSLSocket.getOutputStream())));
                    mPrinterWriterOut.print(writeString);
                    mPrinterWriterOut.print("");
                    mPrinterWriterOut.flush();

                    //check if any errors exist
                    if (mPrinterWriterOut.checkError()){
                        //Log.v(LOG_TAG, "SSLSocketClient: java.io.PrintWriter error");
                        mSocketListener.onError("SSLSocketClient: java.io.PrintWriter error");
                        return;
                    }

                    //Read
                    mBufferedReaderIn = new BufferedReader(
                            new InputStreamReader(
                                    mSSLSocket.getInputStream()));
                }



                String line = null;
                String allLines = null;

                boolean onOpenHeader = false;
                int length = 0;
                int charInt;
                boolean isMessageLine = false;
                //while mSocket is open
                while ((charInt = mBufferedReaderIn.read()) != -1){
                    //get the char
                    char ch = (char) charInt;

                    //convert char to string
                    String stringValue = String.valueOf(ch);


                    //build a line
                    if (line != null){
                        line = line + stringValue;
                    }else {
                        line = stringValue;
                    }



                    //if this is the message line
                    if (isMessageLine){
                        if (line.length() == length){
                            //this is the end of the message
                            isMessageLine = false;
                            if (onOpenHeader){
                                mSocketListener.onOpen(line, allLines);
                                onOpenHeader = false;
                                //<?xml version="1.0" encoding="UTF-8"?><s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope"><s:Body><SubscriptionResponse><SID>uuid:129</SID><duration>0</duration></SubscriptionResponse></s:Body></s:Envelope>
                            }else {
                                mSocketListener.onMessage(line, allLines);
                                //<?xml version="1.0"?><Event seqnum="0" sid="uuid:129"><control>_4</control><action>5</action><node></node><eventInfo><status>0</status></eventInfo></Event>
                            }
                            allLines = null;
                            line = null;
                        }
                    }



                    //line finished by mSocket with "\n" char
                    if (stringValue.equals("\n")){


                        //Log.v(LOG_TAG, "new line:\n " + line);
                        if (allLines != null){
                            allLines = allLines + line;
                        }else {
                            allLines = line;
                        }

                        //check if this is the content length line
                        if (line.startsWith("CONTENT-LENGTH:")){
                            //get the content length
                            int index = line.indexOf(":") + 1;
                            String len = line.substring(index).trim();
                            length = Integer.parseInt(len);
                            //    POST reuse HTTP/1.1
                            //    HOST:0.0.0.0:8000
                            //    CONTENT-TYPE:text/xml
                            //    CONTENT-LENGTH:155
                            //    Connection: Keep-Alive
                        }
                        //check if this is the content length line
                        else if (line.startsWith("Content-Length: ")){
                            //get the content length
                            int index = line.indexOf(":") + 1;
                            String len = line.substring(index).trim();
                            length = Integer.parseInt(len);
                            onOpenHeader = true;
                            //    HTTP/1.1 200 OK
                            //    Content-Length: 216
                            //    Connection: Keep-Alive
                            //    WWW-Authenticate: Basic realm="/"
                            //    Content-Type: application/soap+xml; charset=UTF-8
                            //    Cache-Control: max-age=3600, must-revalidate
                            //    EXT: UCoS, UPnP/1.0, UDI/1.0
                            //    Last-Modified: Tue, 29 Jan 2019 23:22:9 GMT
                        }
                        //if this is a blank line
                        //line between the header and message
                        else if (line.equals("\r\n")){
                            //Log.v(LOG_TAG, "This is a blank line");
                            isMessageLine = true;

                        }

                        line = null;
                    }
                }

                Log.v(LOG_TAG, "Closing Socket");
                mBufferedReaderIn.close();
                mPrinterWriterOut.close();
                if (mSSLSocket != null){
                    mSSLSocket.close();
                }
                if (mSocket != null){
                    mSocket.close();
                }
                mSocketListener.onClose(allLines);




            }catch (UnknownHostException e){
                //Log.v(LOG_TAG, "UnknownHostException: " + e);
                mSocketListener.onError("UnknownHostException: " + e);
            }catch (IOException e){
                //Log.v(LOG_TAG, "IOException: " + e);
                mSocketListener.onError("IOException SocketObject: " + e);
            }



        }
    };



    /**
     * Typical soap authorized response
     */
//    HTTP/1.1 200 OK
//    Connection: Keep-Alive
//    WWW-Authenticate: Basic realm="/"
//    Content-Type: application/soap+xml; charset=UTF-8
//    Cache-Control: max-age=3600, must-revalidate
//    Content-Length: 214
//
//    <?xml version="1.0" encoding="UTF-8"?><s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope"><s:Body><SubscriptionResponse><SID>uuid:1</SID><duration>0</duration></SubscriptionResponse></s:Body></s:Envelope>POST reuse HTTP/1.1
//
//
//    HOST: 0.0.0.0:443
//    CONTENT-TYPE: text/xml
//    CONTENT-LENGTH: 188
//    Connection: keep-alive
//
//
//    <?xml version="1.0"?><Event seqnum="3660" sid="uuid:1"><control>_11</control><action>100</action><node></node><eventInfo><value>2019/01/27 08:02:00</value><unit></unit></eventInfo></Event>POST reuse HTTP/1.1


    /**
     * Typical soap unauthorized response
     */

//    HTTP/1.1 401 Unauthorized
//    X-Powered-By: Express
//    Vary: Origin
//    Cache-Control: no-cache
//    Content-Type: text/html; charset=utf-8
//    Content-Length: 238
//    ETag: W/"ee-LkT5k0RtLx0m1Kw6/mt4tQ"
//    Date: Sun, 27 Jan 2019 16:00:08 GMT
//    Connection: keep-alive
//
//
//    <?xml version="1.0" encoding="UTF-8"?><s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope"><s:Body><UDIDefaultResponse><status>401</status><info>User authorization not valid (401)</info></UDIDefaultResponse></s:Body></s:Envelope>

}
