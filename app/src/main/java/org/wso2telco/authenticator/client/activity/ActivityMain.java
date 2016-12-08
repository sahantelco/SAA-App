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


package org.wso2telco.authenticator.client.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.wso2telco.authenticator.client.R;
import org.wso2telco.authenticator.client.fragment.FingerprintFragment;
import org.wso2telco.authenticator.client.fragment.PinFragment;
import org.wso2telco.authenticator.client.server.ServerAPI;
import org.wso2telco.authenticator.client.util.MyDevice;
import org.wso2telco.authenticator.client.util.MySettings;

public class ActivityMain extends Activity {

    private static final String TAG = "ActivityMain";
    static final int REQUEST_READ_PHONE_STATE = 100;

    TextView tvRegStatus;
    TextView tvRegRetry;

    public interface Request {
        public static int SETTINGS = 0;
        public static int RECOVERY = 1;
        public static int INITIAL_PIN_SETTING = 2;
        public static int FINGERPRINT = 3;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MultiDex.install(this);
        setContentView(R.layout.activity_main);
        try {
            init();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("onCreate error", e.toString());
        }
        MyDevice.setTaskBarColored(this);
        Log.e("PushTokenMain", MySettings.getDevicePushToken(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Request.SETTINGS) {
                Intent i = new Intent(this, ActivitySettings.class);
                startActivity(i);
            } else if (requestCode == Request.RECOVERY) {
                Intent i = new Intent(this, ActivitySettings.class);
                startActivity(i);
            } else if (requestCode == Request.INITIAL_PIN_SETTING) {
                try {
                    init();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("onActivityResult error", e.toString());
                }
            } else if (requestCode == Request.FINGERPRINT) {
                if (data.getIntExtra(FingerprintFragment.STATUS, 0) == FingerprintFragment.Return.SHOW_PIN)
                    showPinActivity(PinFragment.SECURITY_TYPE_ASK_FOR_PIN, Request.SETTINGS);
                else if (data.getIntExtra(FingerprintFragment.STATUS, 0) == FingerprintFragment.Return.SUCCESS)
                    showSettingActivity();
            }
        }
    }

    private void init() throws JSONException {
        if (!MySettings.isPinCodeSet(this))
            showPinActivity(PinFragment.SECURITY_TYPE_NEW_PIN, Request.INITIAL_PIN_SETTING);
        else if (!MyDevice.hasSIM(this))
            showNoSim(View.VISIBLE);
        else if (MySettings.getDeviceRegistrationStatus(this) == MySettings.Registration.NOT_REGISTERED) {
            if (!MyDevice.isInternetConnectedAndByDataNetwork(this))
                showNoMobileData(View.VISIBLE);
            else{
                showDeviceRegistration(View.VISIBLE);
                //MySettings.setDeviceRegistrationStatus();
            }

        } else if (!MyDevice.isInternetConnected(this))
            showNoInternet(View.VISIBLE);
        else {
            Log.e("PushTokenMain", MySettings.getDevicePushToken(this));
            waitForAuthorization();
        }

    }

    public void onClickRetrySIM(View v) {
        if (MyDevice.hasSIM(this))
            showNoSim(View.GONE);
        else
            showToast(R.string.no_sim);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClickRetryMobileData(View v) throws JSONException {
        if (MyDevice.isInternetConnectedAndByDataNetwork(this)) {
            showNoMobileData(View.GONE);
            init();
        } else
            showToast(R.string.no_mobile_data);
    }

    public void onClickRetryInet(View v) throws JSONException {
        if (MyDevice.isInternetConnected(this)) {
            showNoInternet(View.GONE);
            init();
        } else
            showToast(R.string.no_internet);
    }

    public void onClickSettings(View v) {
        String AUTH_MODE = MySettings.getAppAuthMode(this);
        if (AUTH_MODE.equals(MySettings.Authentication.FINGER_PRINT)) {
            if (MyDevice.fingerprintStatus(this) == MyDevice.FingerprintStatus.HARDWARE_SUPPORTED_AND_SET)
                showFingerprintActivity();
            else
                showPinActivity(PinFragment.SECURITY_TYPE_ASK_FOR_PIN, Request.SETTINGS);
        } else
            showPinActivity(PinFragment.SECURITY_TYPE_ASK_FOR_PIN, Request.SETTINGS);
    }

    public void onClickRecovery(View v) {
        showAuthentication();
    }

    public void onClickInfo(View v) {
        showInfoActivity();
    }

    //////////////////Lakini////////////////
    public void onClickAuthorize(View v) throws JSONException {
        authenticateDevice();
    }

    //////////////////////////////////////////////////////////////////////////
    private void showNoSim(int visibility) {
        findViewById(R.id.overlay_no_sim).setVisibility(visibility);
    }

    private void showNoMobileData(int visibility) {
        findViewById(R.id.overlay_no_mobiledata).setVisibility(visibility);
    }

    private void showNoInternet(int visibility) {
        findViewById(R.id.overlay_no_internet).setVisibility(visibility);
    }

    private void waitForAuthorization() {
        TextView txtAwaiting = (TextView) findViewById(R.id.txtAwaiting);
        blink(txtAwaiting);
    }

    private void blink(TextView tv) {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        tv.startAnimation(anim);
    }

    private void showToast(int messageId) {
        Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
    }

    private void showPinActivity(int mode, int returnType) {
        Intent i = new Intent(this, ActivityPin.class);
        i.putExtra(PinFragment.SECURITY_TYPE, mode);
        startActivityForResult(i, returnType);
    }

    private void showSettingActivity() {
        Intent i = new Intent(this, ActivitySettings.class);
        startActivity(i);
    }

    private void showInfoActivity() {
        Intent i = new Intent(this, ActivityPin.class);
        i.putExtra(PinFragment.SECURITY_TYPE, PinFragment.SECURITY_TYPE_ASK_FOR_PIN);
        startActivityForResult(i, Request.SETTINGS);
    }

    private void showAuthentication() {
        Intent i = new Intent(this, ActivityAuthorize.class);
        startActivity(i);
    }

    private void showFingerprintActivity() {
        Intent i = new Intent(this, ActivityFingerprint.class);
        startActivityForResult(i, Request.FINGERPRINT);
    }

    private void showDeviceRegistration(int visibility) throws JSONException {
        findViewById(R.id.overlay_device_registration).setVisibility(visibility);
        if (View.VISIBLE == visibility)
            registerDeviceWithCheck();
    }

    public void onClickRetryRegistration(View v) throws JSONException {
        if (tvRegRetry.getVisibility() == View.VISIBLE)
            registerDeviceWithCheck();
    }

    private void registerDeviceWithCheck() throws JSONException {

        tvRegStatus = (TextView) findViewById(R.id.txtRegistrationStatus);
        tvRegRetry = (TextView) findViewById(R.id.txtRegistrationRetry);
        tvRegRetry.setVisibility(View.GONE);
        tvRegStatus.setText(R.string.registering);
        String deviceId = MySettings.getClientDeviceId(this);
        String pushToken = MySettings.getDevicePushToken(this);
        String platform = MySettings.getDevicePlatform(this);
        String msisdn = MyDevice.getMsisdn(this);

        Log.e("MSISDN new",msisdn);

        Log.e("saa reg with check", pushToken);

        blink(tvRegStatus);

        if (MyDevice.isTelephonyPermissionGranted(this)) {
            if (!pushToken.isEmpty())
                registerDevice(deviceId, pushToken, platform);
            else {
                tvRegStatus.clearAnimation();
                tvRegStatus.setText(R.string.pushtoken_not_found);
                tvRegRetry.setVisibility(View.VISIBLE);
            }
        } else {
            tvRegStatus.clearAnimation();
            tvRegStatus.setText(R.string.no_permission);
            tvRegRetry.setVisibility(View.VISIBLE);
        }
    }

    private void registerDevice(String deviceId, String pushToken, String platform)
            throws
            JSONException {
        //String strMSISDN = MySettings.getMSISDN(this);
        ServerAPI.getInstance(this).register(deviceId, pushToken, platform, new ServerAPI.ResponseListener() {
            @Override
            public void onSuccess() throws JSONException {
                Log.e("onSuccess", "Activitymain");
                showDeviceRegistration(View.GONE);
                MySettings.setDeviceRegistrationStatus(getBaseContext(),MySettings.Registration.REGISTERED);
                //init();
            }

            @Override
            public void onFailure(String reason) {
                Log.e("onFAilure", "Activitymain");
                tvRegStatus.clearAnimation();
                if(reason.equalsIgnoreCase("Device Already registered"))
                    tvRegStatus.setText(R.string.alredy_registered);
                else if(reason.equalsIgnoreCase("Error in Registration"))
                    tvRegStatus.setText(R.string.registration_error);
                tvRegRetry.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    try {
                        registerDeviceWithCheck();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    waitForAuthorization();
                }
                break;

            default:
                break;
        }
    }

    ////////Temporary
    //Authenticatestatus
    private void authenticateDevice() throws JSONException {
        ServerAPI.getInstance(this).setAuthenticationStatus("712295446", 1, "340943904904904343904", new ServerAPI.ResponseListener() {


            @Override
            public void onSuccess() throws JSONException {
                Log.e("onSuccess", "Activitymain");
                showDeviceRegistration(View.GONE);
            }

            @Override
            public void onFailure(String reason) {
                Log.e("onFAilure", "Activitymain");
                tvRegStatus.clearAnimation();
                tvRegStatus.setText("Authentication Failed!");
                tvRegRetry.setVisibility(View.VISIBLE);
            }
        });
    }
}
