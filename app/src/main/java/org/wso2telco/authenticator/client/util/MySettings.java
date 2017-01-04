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

import android.content.SharedPreferences;
import android.content.Context;
import android.util.Log;

public class MySettings {

    private static final String APP_SETTINGS = "APP_SETTINGS";
    private static final String LOCALE = "LOCALE";
    private static final String DEVICE_CLIENT_ID = "CLIENT_DEVICE_ID";
    private static final String APP_PINCODE = "APP_PINCODE";
    private static final String APP_INSTALLED = "APP_INSTALLED";
    private static final String APP_PINSALT = "APP_PINSALT";
    private static final String TRANS_AUTH_MODE = "TRANS_AUTH_MODE";
    private static final String APP_AUTH_MODE = "APP_AUTH_MODE";
    private static final String DEFAULT_APP_AUTH_MODE = Authentication.PIN;
    private static final String DEFAULT_TRANS_AUTH_MODE = Authentication.PIN;
    private static final String DEVICE_PUSH_TOKEN = "DEVICE_PUSH_TOKEN";
    private static final String DEVICE_REGISTRATION = "DEVICE_REGISTRATION";
    private static final String MSISDN = "MSISDN";
    private static final String PLATFORM = "Android";
    private static final String ACTIVITYOAUTH = "False";
    private static int DEFAULT_DEVICE_REGISTRATION_STATUS = Registration.NOT_REGISTERED;


    public interface Authentication {
        public static String PIN = "PIN";
        public static String FINGER_PRINT = "FP";
    }

    public interface Registration {
        public static int REGISTERED = 0;
        public static int NOT_REGISTERED = 1;
    }

    private MySettings() {
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
    }

    //getters and setters for LOCALE
    public static String getLocale(Context context) {
        return getSharedPreferences(context).getString(LOCALE, null);
    }

    public static void setLocale(Context context, String newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(LOCALE, newValue);
        editor.commit();
    }

    //getters and setters for DEVICE_CLIENT_ID
    public static String getClientDeviceId(Context context) {
        String deviceId = MyDevice.getClientDeviceID(context);
        return deviceId;
    }

    //getters and setters for PLATFORM
    public static String getDevicePlatform(Context context) {
        return PLATFORM;
    }

    //getters and setters for MSISDN
    public static String getMSISDN(Context context) {
        return getSharedPreferences(context).getString(MSISDN, "");
    }

    public static void setMSISDN(Context context, String newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(MSISDN, newValue);
        editor.commit();
    }

    //getters and setters for APP_PINSALT
    public static void setPinSalt(Context context, String newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(APP_PINSALT, newValue);
        editor.commit();
    }

    public static String getPinSalt(Context context) {
        return getSharedPreferences(context).getString(APP_PINSALT, "");
    }

    //getters and setters APP_PINCODE
    public static void setPinCode(Context context, String newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(APP_PINCODE, newValue);
        editor.commit();
    }

    public static String getPinCode(Context context) {
        return getSharedPreferences(context).getString(APP_PINCODE, "");
    }

    public static boolean isPinCodeSet(Context context) {
        String pinCode = getSharedPreferences(context).getString(APP_PINCODE, "");
        if (pinCode.isEmpty())
            return false;
        else
            return true;
    }

    //getters and setters APP_INSTALLED
    public static void setAppInstalled(Context context, String newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(APP_INSTALLED, newValue);
        editor.commit();
        Log.d("set", "app installed");
    }

    public static String getAppInstalled(Context context) {
        return getSharedPreferences(context).getString(APP_INSTALLED, "");
    }

    public static boolean isAppInstalled(Context context) {
        String appInstalled = getSharedPreferences(context).getString(APP_INSTALLED, "");
        Log.d("AppInstalled", appInstalled);
        if (appInstalled.isEmpty())
            return false;
        else
            return true;
    }

    //getters and setters for TRANS_AUTH_MODE
    public static String getTransAuthMode(Context context) {
        return getSharedPreferences(context).getString(TRANS_AUTH_MODE, DEFAULT_TRANS_AUTH_MODE);
    }

    public static void setTransAuthMode(Context context, String newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(TRANS_AUTH_MODE, newValue);
        editor.commit();
    }

    //getters and setters for DEVICE_REGISTRATION
    public static int getDeviceRegistrationStatus(Context context) {
        return getSharedPreferences(context).getInt(DEVICE_REGISTRATION, DEFAULT_DEVICE_REGISTRATION_STATUS);
    }

    public static void setDeviceRegistrationStatus(Context context, int newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(DEVICE_REGISTRATION, newValue);
        editor.commit();
    }

    //getters and setters for APP_AUTH_MODE
    public static String getAppAuthMode(Context context) {
        return getSharedPreferences(context).getString(APP_AUTH_MODE, DEFAULT_APP_AUTH_MODE);
    }

    public static void setAppAuthMode(Context context, String newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(APP_AUTH_MODE, newValue);
        editor.commit();
    }

    //getters and setters for DEVICE_PUSH_TOKEN
    public static String getDevicePushToken(Context context) {
        return getSharedPreferences(context).getString(DEVICE_PUSH_TOKEN, "");
    }

    public static void setDevicePushToken(Context context, String newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(DEVICE_PUSH_TOKEN, newValue);
        editor.commit();
    }

    //getters and setters for ACTIVITYOAUTH
    public static boolean getActivityOAuth(Context context) {
        return getSharedPreferences(context).getBoolean(ACTIVITYOAUTH, false);
    }

}