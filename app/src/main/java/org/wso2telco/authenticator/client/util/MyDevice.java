/* ******************************************************************************************
 *
 * Copyright (c) 2016-2017, WSO2.Telco Inc. (http://wso2telco.com/) All Rights Reserved.
 *
 * WSO2.Telco Inc. licenses this file under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
********************************************************************************************/

package org.wso2telco.authenticator.client.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import org.wso2telco.authenticator.client.R;

import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.KeyGenerator;


public class MyDevice {

    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    static final String DEFAULT_KEY_NAME = "default_key";

    static final int REQUEST_READ_PHONE_STATE = 100;


    public interface FingerprintStatus {
        public static int HARDWARE_SUPPORTED_AND_SET = 0;
        public static int HARDWARE_SUPPORTED_AND_NOT_SET = 1;
        public static int HARDWARE_NOT_SUPPORTED = 2;
    }

    private MyDevice() {
    }

    public static void setTaskBarColored(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = context.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(context, R.color.black));
        }
    }

    private static TelephonyManager getTelephonyManagerService(Context context) {
        return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    private static ConnectivityManager getConnectivityManagerService(Context context) {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private static FingerprintManager getFingerprintManagerService(Context context) {
        return (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
    }

    private static boolean checkForReadPhoneStatePermission(Context context) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
        }
        return true;
    }

    private static String computeMD5Hash(String value) {
        String MD5HashString = "";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(value.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuffer MD5Hash = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                MD5Hash.append(h);
            }
            MD5HashString = MD5Hash.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return MD5HashString;
    }

    public static boolean hasSIM(Context context) {

        boolean SIM_Present = false;
        TelephonyManager telMgr = getTelephonyManagerService(context);
        int simState = telMgr.getSimState();
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
            case TelephonyManager.SIM_STATE_UNKNOWN:
                SIM_Present = false;
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
            case TelephonyManager.SIM_STATE_READY:
                SIM_Present = true;
                break;
        }
        return SIM_Present;
    }

    public static String getClientDeviceID(Context context) {

        String clientDeviceId = "";
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            String deviceIMSI = getTelephonyManagerService(context).getSubscriberId();
            String deviceIMEI = getTelephonyManagerService(context).getDeviceId();
            clientDeviceId = computeMD5Hash(deviceIMSI + deviceIMEI);
        }
        return clientDeviceId;
    }

    public static String getMsisdn(Context context) {

        String msisdn = "";
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            msisdn = getTelephonyManagerService(context).getLine1Number();
        }
        return msisdn;
    }

    public static boolean isTelephonyPermissionGranted(Activity activity) {
        boolean flag = false;
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
            flag = true;
        else
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        return flag;
    }

    public static boolean isInternetConnectedByDataNetwork(Context context) {
        NetworkInfo activeNetwork = getConnectivityManagerService(context).getActiveNetworkInfo();
        boolean state = activeNetwork != null &&
                activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
        return state;
    }

    public static boolean isInternetConnected(Context context) {
        NetworkInfo activeNetwork = getConnectivityManagerService(context).getActiveNetworkInfo();
        boolean state = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return state;
    }

    public static boolean isInternetConnectedAndByDataNetwork(Context context) {
        NetworkInfo activeNetwork = getConnectivityManagerService(context).getActiveNetworkInfo();
        boolean state = activeNetwork != null && activeNetwork.isConnectedOrConnecting() &&
                activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
        return state;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static int fingerprintStatus(Context context) {
        int status = FingerprintStatus.HARDWARE_NOT_SUPPORTED;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED) {
            FingerprintManager fingerprintManager = getFingerprintManagerService(context);
            if (fingerprintManager.hasEnrolledFingerprints()) {
                status = FingerprintStatus.HARDWARE_SUPPORTED_AND_SET;
            } else {
                status = FingerprintStatus.HARDWARE_SUPPORTED_AND_NOT_SET;
            }
        }
        return status;
    }

    public static void getPackageInfo(Activity activity) {
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(
                    activity.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                Log.d("Signature:", activity.getPackageName());

            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }

    public static final class PinHash {
        private final String salt;
        private final String hash;

        public PinHash(String salt, String hash) {
            this.salt = salt;
            this.hash = hash;
        }

        public String getSalt() {
            return salt;
        }

        public String getHash() {
            return hash;
        }
    }

    public static PinHash pinToHash(String paramString, String salt) {
        if (paramString == null)
            return null;
        String str = null;
        byte[] arrayOfByte1 = null;
        try {
            if (salt.isEmpty()) {
                salt = getSalt();
            }
            byte[] arrayOfByte2 = (paramString + salt).getBytes();
            byte[] arrayOfByte3 = null;
            MessageDigest localMessageDigest = MessageDigest.getInstance("SHA-1");
            long l1 = System.currentTimeMillis();
            for (int i = 0; i < 1024; i++) {
                arrayOfByte1 = null;
                if (arrayOfByte3 != null)
                    localMessageDigest.update(arrayOfByte3);
                localMessageDigest.update(("" + i).getBytes());
                localMessageDigest.update(arrayOfByte2);
                arrayOfByte3 = localMessageDigest.digest();
            }
            arrayOfByte1 = byteArrayToHex(arrayOfByte3).getBytes();
            long l2 = System.currentTimeMillis();
            PinHash pinHash = new PinHash(salt, byteArrayToHex(arrayOfByte1));
            return pinHash;
        } catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
            Log.e("LockPatternUtils", "Failed to encode string because of missing algorithm: " + str);
        }
        return null;
    }

    private static String getSalt() {
        int min = 5000;
        int max = 10000;
        Random r = new Random();
        int i1 = r.nextInt(max - min + 1) + min;

        Log.e("Salt", String.valueOf(i1));

        return String.valueOf(i1);
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }
}