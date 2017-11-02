package org.qumodo.miscaclient.dataProviders;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

public class UserSettingsManager {

    private static final String USER_PREFERENCES = "org.qumodo.misca.preferences";
    private static final String TAG = "UserSettingsManager";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_MISCA_ID = "misca_id";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_CLIENT_CERTIFICATE = "client_certificate";
    private static final String KEY_USER_AUTHORISED = "user_authorised";

    public static final String USER_ID_A = "QSDU_1122334455";
    public static final String USER_ID_B = "QSDU_3944238293";

    public static String userPassPhrase;

    private static SharedPreferences mSharedPreferences;

    @NonNull
    public static SharedPreferences getSharedPreferences(Context context) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(USER_PREFERENCES, 0);
        }

        return mSharedPreferences;
    }

    public static void loadSharedPreferences(Context context) {
        mSharedPreferences = context.getSharedPreferences(USER_PREFERENCES, 0);
    }

    @Nullable
    public static String getUserID() {
        return getValue(KEY_USER_ID, null);
    }

    public static void setUserID(String userID) {
        setValue(KEY_USER_ID, userID);
    }

    public static void setValue(String key, String value) {
        if (mSharedPreferences != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(key, value);
            editor.apply();
        } else
            Log.e(TAG, "Error, shared preferences not loaded Setting Value: " + key + ": " + value);
    }

    public static void setValue(String key, int value) {
        if (mSharedPreferences != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt(key, value);
            editor.apply();
        } else
            Log.e(TAG, "Error, shared preferences not loaded Setting Value: " + key + ": " + value);
    }

    @Nullable
    public static String getValue(String key, String defaultValue) {
        if (mSharedPreferences != null)
            return mSharedPreferences.getString(key, defaultValue);
        Log.e(TAG, "Error, shared preferences not loaded Getting Value: " + key);
        return null;
    }

    public static int getValue(String key, int defaultValue) {
        if (mSharedPreferences != null)
            return mSharedPreferences.getInt(key, defaultValue);
        Log.e(TAG, "Error, shared preferences not loaded Getting Value: " + key);
        return -1;
    }

    @Nullable
    public static String getMiscaID() {
        return getValue(KEY_MISCA_ID, null);
    }

    public static void setMiscaID(String id) {
        setValue(KEY_MISCA_ID, id);
    }

    public static String getDeviceID() {

        String deviceID = getValue(KEY_DEVICE_ID, null);

        if (deviceID == null) {
            deviceID = UUID.randomUUID().toString();
            setValue(KEY_DEVICE_ID, deviceID);
        }

        return deviceID;
    }

    public void setDeviceID(String id) {
        setValue(KEY_DEVICE_ID, id);
    }

    @Nullable
    public static Certificate getClientCertificate() {
        try {
            KeyStore ks = getKeyStore();
            KeyStore.Entry entry = ks.getEntry(KEY_CLIENT_CERTIFICATE, null);
            if (entry == null) {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_EC,
                        ANDROID_KEY_STORE
                );

                kpg.initialize(
                        new KeyGenParameterSpec.Builder(
                                KEY_CLIENT_CERTIFICATE,
                                KeyProperties.PURPOSE_SIGN
                                  | KeyProperties.PURPOSE_VERIFY
                                  | KeyProperties.PURPOSE_ENCRYPT
                                  | KeyProperties.PURPOSE_DECRYPT
                        ).build()
                );

                kpg.generateKeyPair();
                entry = ks.getEntry(KEY_CLIENT_CERTIFICATE, null);
            }

            if (entry != null && entry instanceof KeyStore.PrivateKeyEntry) {
                KeyStore.PrivateKeyEntry privateKey = (KeyStore.PrivateKeyEntry) entry;
                return privateKey.getCertificate();
            }
        } catch (KeyStoreException | InvalidAlgorithmParameterException | NoSuchProviderException
                | UnrecoverableEntryException | IOException | NoSuchAlgorithmException
                | CertificateException e) {
            e.printStackTrace();
        }

        return null;
    }

    @NonNull
    public static KeyStore getKeyStore()
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
        ks.load(null);
        return ks;
    }

    @Nullable
    public static PublicKey getClientPublicKey() {
        Certificate cert =  getClientCertificate();
        if (cert != null)
            return cert.getPublicKey();
        return null;
    }

    @Nullable
    public static String getClientPublicKeyAsString() {
        PublicKey pk = getClientPublicKey();
        if (pk != null)
            return new String( Base64.encode(pk.getEncoded(), 0) );

        return null;
    }

    public static String getHashedClientPublicKeyString() {
        try {
            String pk = getClientPublicKeyAsString();
            if (pk != null) {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(pk.getBytes(StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();

                for (byte aHash : hash) {
                    String hex = Integer.toHexString(0xff & aHash);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }

                return hexString.toString();
            }
        } catch (Exception error) {
            error.printStackTrace();
        }

        return null;
    }



    public static void validate(PublicKey pk)
            throws NoSuchProviderException, CertificateException, NoSuchAlgorithmException,
                   InvalidKeyException, SignatureException {
        Certificate cert = getClientCertificate();
        if (cert != null)
            cert.verify(pk);
    }

    @Nullable
    public static String encryptWithClientCertificate(String value) {
        try {
            Certificate cert = getClientCertificate();
            if (cert != null) {
                RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();
                Cipher input = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
                input.init(Cipher.ENCRYPT_MODE, publicKey);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, input);
                cipherOutputStream.write(value.getBytes("UTF-8"));
                cipherOutputStream.close();

                byte [] vals = outputStream.toByteArray();
                return Base64.encodeToString(vals, Base64.DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public static String decryptWithClientCertificate(String encrypted) {

        try {
            KeyStore ks = getKeyStore();
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)ks.getEntry(KEY_CLIENT_CERTIFICATE, null);
            if (privateKeyEntry != null) {
                RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();

                Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
                output.init(Cipher.DECRYPT_MODE, privateKey);
                CipherInputStream cipherInputStream = new CipherInputStream(
                        new ByteArrayInputStream(Base64.decode(encrypted, Base64.DEFAULT)),
                        output
                );
                ArrayList<Byte> values = new ArrayList<>();
                int nextByte;
                while ((nextByte = cipherInputStream.read()) != -1) {
                    values.add((byte)nextByte);
                }
                byte[] bytes = new byte[values.size()];
                for(int i = 0; i < bytes.length; i++) {
                    bytes[i] = values.get(i);
                }
                return new String(bytes, 0, bytes.length, "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isUserAuthorised() {
        return mSharedPreferences.getBoolean(KEY_USER_AUTHORISED, false);
    }

    public static void setUserAuthorised(boolean state) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_USER_AUTHORISED, state);
        editor.apply();
    }

}
