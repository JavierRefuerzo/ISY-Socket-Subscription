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




package com.voiceforiot.isycustomsocket.dataRequestUtils;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import android.util.Log;

import com.voiceforiot.isycustomsocket.constants.SecurityConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

public class Crypto {

    private static final String LOG_TAG = Crypto.class.getSimpleName();

    private Crypto() {
    }

    public static boolean isInitiated(String alias) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        boolean isInitiated = false;
        KeyStore keyStore;

        keyStore = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        keyStore.load(null);
        Enumeration<String> aliases = keyStore.aliases();
        //Log.v(LOG_TAG, "aliases are: " + alias);
        while (aliases.hasMoreElements()) {
            String currentAlias = aliases.nextElement();
            if (currentAlias.matches(alias)) {
                //Log.v(LOG_TAG, "match made");
                isInitiated = true;
            }
        }
        Log.v(LOG_TAG, "isInitiated is: " + isInitiated);
        return isInitiated;
    }

    public static void createNewAlias(Context context, String alias) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyStore keyStore;
        keyStore = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        keyStore.load(null);

        // Create new key if needed
        if (!keyStore.containsAlias(alias)) {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 1);
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(alias)
                    .setSubject(new X500Principal("CN=Sample Name, O=Android Authority"))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            generator.initialize(spec);

            KeyPair keyPair = generator.generateKeyPair();
        }
    }

    public void deleteAlias(final String alias) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        KeyStore keyStore;
        keyStore = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        keyStore.load(null);
        keyStore.deleteEntry(alias);
    }


    public static String encryptString(Context context, String alias, String stringToEncrypt) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, UnrecoverableEntryException, InvalidAlgorithmParameterException {

        boolean isInit = isInitiated(alias);
        if (isInit == false){
            Log.v(LOG_TAG, "encrypt test");
            createNewAlias(context, alias);
            isInit = isInitiated(alias);
        }
        if (isInit == false){
            throw new IllegalArgumentException("could not encrypt String");
        }
        KeyStore keyStore;
        keyStore = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        keyStore.load(null);
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
        PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey(); // Don't TypeCast to RSAPublicKey


        Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(
                outputStream, inCipher);
        cipherOutputStream.write(stringToEncrypt.getBytes("UTF-8"));
        cipherOutputStream.close();

        byte[] vals = outputStream.toByteArray();
        String encryptedText = Base64.encodeToString(vals, Base64.DEFAULT);

        return encryptedText;
    }



    public static String decryptString(Context context, String alias, String stringToDecrypt) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, NoSuchPaddingException, InvalidKeyException, UnrecoverableEntryException, NoSuchProviderException, InvalidAlgorithmParameterException {

        boolean isInit = isInitiated(alias);
        if (isInit == false){
            //this should only be started if decryptSting isInit is false
            createNewAlias(context, alias);
            isInit = isInitiated(alias);
        }
        if (isInit == false){
            Log.v(LOG_TAG, "throwing exception decryptString isInit is: " + isInit);
            throw new IllegalArgumentException("could not decrypt String");
        }

        KeyStore keyStore;
        keyStore = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        keyStore.load(null);


        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
        PrivateKey privateKey = privateKeyEntry.getPrivateKey(); // Don't TypeCast to RSAPrivateKey
        Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        output.init(Cipher.DECRYPT_MODE, privateKey);

        String cipherText = stringToDecrypt;
        CipherInputStream cipherInputStream = new CipherInputStream(
                new ByteArrayInputStream(Base64.decode(cipherText, Base64.DEFAULT)), output);
        ArrayList<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte) nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i).byteValue();
        }

        String finalText = new String(bytes, 0, bytes.length, "UTF-8");

        return finalText;
    }

}
