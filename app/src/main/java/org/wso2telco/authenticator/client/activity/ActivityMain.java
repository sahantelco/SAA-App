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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2telco.authenticator.client.R;
import org.wso2telco.authenticator.client.fragment.FingerprintFragment;
import org.wso2telco.authenticator.client.fragment.PinFragment;
import org.wso2telco.authenticator.client.oauthconnection.EnvironmentDTO;
import org.wso2telco.authenticator.client.server.ServerAPI;
import org.wso2telco.authenticator.client.util.MyDevice;
import org.wso2telco.authenticator.client.util.MySettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ActivityMain extends Activity {

    private static final String TAG = "ActivityMain";
    static final int REQUEST_READ_PHONE_STATE = 100;
    private WebView webView;
    String endpoint;

    TextView tvRegStatus;
    Button tvRegRetry;
    ImageView tvLoading;
    String aouthCodeValue = null;
    String tokenCodeValue = null;
    String userInfo;
    String deviceId = null;
    String pushToken = null;
    String platform = null;

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

        //////////////////////////////////
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        Log.d("DpHeight",Float.toString(dpHeight));
        Log.d("DpWidth",Float.toString(dpWidth));
        //////////////////////////////////
        setContentView(R.layout.activity_main);
        try {
            init();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MyDevice.setTaskBarColored(this);
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
        if (!MySettings.isAppInstalled(this)) {
            Log.d("app", "App installed");
            showAppInstallActivity(VISIBLE);
            MySettings.setAppInstalled(this, "installed");
        }
//        else if (!MyDevice.hasSIM(this))
//            showNoSim(View.VISIBLE);
//        else if (!MyDevice.isSIMSupportMobileConnect(this))
//            showSimNotSupportMC(View.VISIBLE);
        else if (MySettings.getDeviceRegistrationStatus(this) == MySettings.Registration.NOT_REGISTERED) {
            if (!MyDevice.isInternetConnectedAndByDataNetwork(this))
                showNoMobileData(VISIBLE);
            else {
                showDeviceRegistration(VISIBLE);
            }
        } else if (!MyDevice.isInternetConnected(this))
            showNoInternet(VISIBLE);
        else {
            Log.e("PushTokenMain", MySettings.getDevicePushToken(this));
            waitForAuthorization();
        }
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

    public void onClickRetrySIM(View v) throws JSONException {
        if (MyDevice.hasSIM(this)) {
            showNoSim(View.GONE);
            init();
        } else
            showToast(R.string.no_sim);
    }

    public void onClickRetrySIMmobileConnect(View v) throws JSONException {
        if (MyDevice.isSIMSupportMobileConnect(this)) {
            showSimNotSupportMC(View.GONE);
            init();
        } else
            showToast(R.string.no_sim_supportMC);
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

    private void showNoSim(int visibility) {
        findViewById(R.id.overlay_no_sim).setVisibility(visibility);
    }

    private void showSimNotSupportMC(int visibility) {
        findViewById(R.id.overlay_no_sim_support_mc).setVisibility(visibility);
        showToast(R.string.no_sim_supportMC);
    }

    private void showNoMobileData(int visibility) {
        findViewById(R.id.overlay_no_mobiledata).setVisibility(visibility);
    }

    private void showNoInternet(int visibility) {
        findViewById(R.id.overlay_no_internet).setVisibility(visibility);
    }

    private void waitForAuthorization() {
        findViewById(R.id.overlay_initial_app_settings).setVisibility(GONE);
        Log.d("WaitforAuthorization", "Waitfor authorization");
        TextView txtAwaiting = (TextView) findViewById(R.id.txtAwaitingAutherization);
//        showImageSP(EnvironmentDTO.serviceproviderURL);
        blink(txtAwaiting);
    }


//    private void showImageSP(String imgURL) {
//        final ImageView imgSP = (ImageView) findViewById(R.id.spAuthorizationPage);
//        ImageRequest imgRequest = new ImageRequest(imgURL,
//                new Response.Listener<Bitmap>() {
//                    @Override
//                    public void onResponse(Bitmap response) {
//                        imgSP.setImageBitmap(response);
//                    }
//                }, 0, 0, ImageView.ScaleType.FIT_XY, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                imgSP.setBackgroundColor(Color.parseColor("#000000"));
//                error.printStackTrace();
//            }
//        });
//        ServerAPI.getInstance(this).addToRequestQueue(imgRequest);
//    }

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

    private void showMainActivity() {
        Intent i = new Intent(this, ActivityMain.class);
        startActivity(i);
    }

    private void showAppInstallActivity(int visibility) {
        findViewById(R.id.overlay_initial_app_settings).setVisibility(visibility);
//        TextView overlay_initial_app_settings_textview = (TextView) findViewById(R.id.overlay_initial_app_settings_textview);
//        overlay_initial_app_settings_textview.setTextSize(20 * getResources().getDisplayMetrics()
//                .density);
////        TextView overlay_initial_app_settings_textview = (TextView) findViewById(R.id.overlay_initial_app_settings_textview);
//        overlay_initial_app_settings_textview.setTextSize(20 * getResources().getDisplayMetrics()
//                .density);
//
//        Button btnIntialAppPin = (Button) findViewById(R.id
//                .btnIntialAppPin);
//        btnIntialAppPin.setTextSize(20 * getResources().getDisplayMetrics()
//                .density);
//
//        Button btnIntialAppFingerprint = (Button) findViewById(R.id
//                .btnIntialAppFingerprint);
//        btnIntialAppFingerprint.setTextSize(20 * getResources().getDisplayMetrics()
//                .density);

//        Button btnIntialAppPin = (Button) findViewById(R.id
//                .btnIntialAppPin);
//        btnIntialAppPin.setTextSize(20 * getResources().getDisplayMetrics()
//                .density);
//
//        Button btnIntialAppFingerprint = (Button) findViewById(R.id
//                .btnIntialAppFingerprint);
//        btnIntialAppFingerprint.setTextSize(20 * getResources().getDisplayMetrics()
//                .density);

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
        if (VISIBLE == visibility)
            registerDeviceWithCheck();
    }

    public void onClickRetryRegistration(View v) throws JSONException {
        if (tvRegRetry.getVisibility() == VISIBLE) {
            tvRegStatus.setText(R.string.registering);
            blink(tvRegStatus);
            registerDeviceWithCheck();
        }
    }

    public void onClickRegisterWithPin(View v) throws JSONException {
        if (!MySettings.isPinCodeSet(this)) {
            showPinActivity(PinFragment.SECURITY_TYPE_NEW_PIN, ActivityMain.Request.INITIAL_PIN_SETTING);
        }
    }

    public void onClickRegisterWithFingerprint(View v) throws JSONException {
        int deviceFingerprintStatus = MyDevice.fingerprintStatus(this);

        if (deviceFingerprintStatus == MyDevice.FingerprintStatus.HARDWARE_SUPPORTED_AND_SET)
            MySettings.setAppAuthMode(this, MySettings.Authentication.FINGER_PRINT);
        else if (deviceFingerprintStatus == MyDevice.FingerprintStatus.HARDWARE_SUPPORTED_AND_NOT_SET)
            Toast.makeText(this,
                    getResources().getString(R.string.error_not_set_finger_print),
                    Toast.LENGTH_LONG).show();
        else if (deviceFingerprintStatus == MyDevice.FingerprintStatus.HARDWARE_NOT_SUPPORTED)
            Toast.makeText(this,
                    getResources().getString(R.string.error_not_supported_finger_print),
                    Toast.LENGTH_LONG).show();

        showMainActivity();
    }


    public void onCancelInstallation() {
        //showMainActivity();
    }

    private void registerDeviceWithCheck() throws JSONException {

        endpoint = getMyUrl();
        Log.d("EndPoint", endpoint);
        webView = (WebView) findViewById(R.id.webview01);
        tvRegStatus = (TextView) findViewById(R.id.txtRegistrationStatus);
        tvRegRetry = (Button) findViewById(R.id.btnRetryRegistration);
        tvRegRetry.setVisibility(View.GONE);
        tvRegStatus.setText(R.string.registering);
        blink(tvRegStatus);
        deviceId = MySettings.getClientDeviceId(this);
        pushToken = MySettings.getDevicePushToken(this);
        platform = MySettings.getDevicePlatform(this);

        if (MyDevice.isTelephonyPermissionGranted(this)) {
            if (!pushToken.isEmpty()) {

                endpoint = getMyUrl();
                Log.d("EndPoint", endpoint);
                webView = (WebView) findViewById(R.id.webview01);
                tvRegStatus = (TextView) findViewById(R.id.txtRegistrationStatus);
                tvRegRetry = (Button) findViewById(R.id.btnRetryRegistration);
                tvLoading = (ImageView) findViewById(R.id.imageLoading);
                tvRegRetry.setVisibility(View.GONE);
                tvRegStatus.setText(R.string.registering);
                blink(tvRegStatus);

                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);
                webView.setWebViewClient(new WebViewClient() {

                    @SuppressWarnings("deprecation")
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.setVisibility(View.GONE);
                        try {
                            view.setVisibility(View.GONE);
                            Log.d("WebView URL", url);
                            view.loadUrl(url);

                            //if (view.getUrl().contains("preprod/authenticationendpoint/login" +
                            // ".do") && view.getUrl().contains
                            // ("authenticators=MSISDNAuthenticator:LOCAL"))

                            if (view.getUrl().contains("sandbox.mconnect.wso2telco.com/dashboard/register.jag") && view.getUrl().contains("sp=admin_gsmaapp")) {
                                view.setVisibility(View.VISIBLE);
                                tvRegStatus.setVisibility(View.GONE);
                                tvRegRetry.setVisibility(View.GONE);
                                tvLoading.setVisibility(View.GONE);
                            } else {
                                tvRegStatus.setVisibility(View.VISIBLE);
                                tvLoading.setVisibility(View.VISIBLE);
                                blink(tvRegStatus);
                            }

                            if (view.getUrl().contains("playground2") && view.getUrl().contains("&code=")) {

                                aouthCodeValue = getAouthCodeFromUrl(view.getUrl());
                                Log.d("Aouth Code value", aouthCodeValue);

                                if (aouthCodeValue != null) {
                                    //Get the token code
                                    TokenRequest tokenTask = new TokenRequest();
                                    if (Build.VERSION.SDK_INT >= 11)
                                        tokenTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    else
                                        tokenTask.execute();
                                }
                            } else
                                return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                    }

                    @Override
                    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                        handler.proceed();
                    }

                });

                webView.clearCache(true);
                webView.clearHistory();
                webView.loadUrl(endpoint);

            } else {
                tvRegStatus.clearAnimation();
                tvRegStatus.setText(R.string.pushtoken_not_found);
                tvRegRetry.setVisibility(VISIBLE);
            }
        } else {
            tvRegStatus.clearAnimation();
            tvRegStatus.setText(R.string.no_permission);
            // tvLoading.setImageResource(R.drawable.no_telephony_services);
            tvRegRetry.setVisibility(VISIBLE);
        }
    }

    private void registerDevice(String deviceId, String pushToken, String platform)
            throws
            JSONException {
        Log.e("Register device", "Registration");
        ServerAPI.getInstance(this).register(deviceId, pushToken, platform, new ServerAPI.ResponseListener() {
            @Override
            public void onSuccess() throws JSONException {
                Log.e("onSuccess", "Activitymain");
                showDeviceRegistration(View.GONE);
                MySettings.setDeviceRegistrationStatus(getBaseContext(), MySettings.Registration.REGISTERED);
                notifyUserAndGoBackground("Your smart phone authenticator has been sucesfully enrolled. It will run in" +
                        " the background");
            }

            @Override
            public void onFailure(String reason) {
                Log.e("onFailure", "Activitymain");
                tvRegStatus.clearAnimation();
                if (reason.equalsIgnoreCase("Device Already registered")) {
                    tvRegStatus.setText(R.string.alredy_registered);
                } else if (reason.equalsIgnoreCase("Error in Registration")) {
                    tvRegStatus.setText(R.string.registration_error);
                }
                tvRegRetry.setVisibility(VISIBLE);
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

    //Open alert dialog box
    public void notifyUserAndGoBackground(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        moveTaskToBack(true);
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private String getMyUrl() {
        String scope = EnvironmentDTO.scope;
        if (scope.contains("openid")) {
            String url = EnvironmentDTO.getOpenidEndpoint();
            if (EnvironmentDTO.isHeaderEnrichViaNetwork() == false)
                url = url.concat("&msisdn=" + EnvironmentDTO.getMsisdn());
            return url;
        }
        return null;
    }

    private String getAouthCodeFromUrl(String url) {
        String[] params = url.split("&code=");
        return params[1];
    }

    private class TokenRequest extends AsyncTask<Void, Void, String> {

        HttpResponse response;

        protected String doInBackground(Void... params) {
            String tokenUrl = EnvironmentDTO.getTokenEndpoint() + aouthCodeValue;

            DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
            SSLContext ctx = null;
            try {
                ctx = SSLContext.getInstance("TLS");
                ctx.init(null, new TrustManager[]{
                        new X509TrustManager() {
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                            }

                            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                            }

                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[]{};
                            }
                        }
                }, null);

                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }

            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
            HttpPost httpPost = new HttpPost(tokenUrl);
            httpPost.addHeader("Authorization", EnvironmentDTO.getOautherizationHeaderValue());
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");

            try {
                response = defaultHttpClient.execute(httpPost);
                getJsonObject(response);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            Log.d("token code", tokenCodeValue);
            ServerAPI.setTOKEN(tokenCodeValue);

            if (tokenCodeValue != null) {
                UserInfoRequest userInfo = new UserInfoRequest();
                if (Build.VERSION.SDK_INT >= 11)
                    userInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    userInfo.execute();
            }
        }

        private void getJsonObject(HttpResponse response) {
            BufferedReader rd = null;
            try {
                rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            } catch (UnsupportedOperationException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            StringBuffer result = new StringBuffer();
            String line = "";
            try {
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                JSONObject o = new JSONObject(result.toString());
                if (o.get("access_token") != null) {
                    tokenCodeValue = (String) o.get("access_token");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class UserInfoRequest extends AsyncTask<Void, Void, String> {
        HttpResponse response;

        protected String doInBackground(Void... urls) {

            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(EnvironmentDTO.getUserInfoEndpoint());
            httpGet.addHeader("Authorization", "Bearer " + tokenCodeValue);
            httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");

            try {
                response = httpclient.execute(httpGet);
                getJsonObject(response);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            Log.d("MSISDN", userInfo);
            ServerAPI.setMSISDN(userInfo);
            Log.d("Inside MSISDN Token", ServerAPI.getTOKEN());

            if (userInfo != null)
                try {
                    registerDevice(deviceId, pushToken, platform);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }

        private void getJsonObject(HttpResponse response) {
            BufferedReader rd = null;
            try {
                rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            } catch (UnsupportedOperationException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            StringBuffer result = new StringBuffer();
            String line = "";
            try {
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                JSONObject o = new JSONObject(result.toString());
                if (o.get("msisdn") != null) {
                    userInfo = (String) o.get("msisdn");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
