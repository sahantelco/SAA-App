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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import org.json.JSONException;
import org.wso2telco.authenticator.client.R;
import org.wso2telco.authenticator.client.fragment.FingerprintFragment;
import org.wso2telco.authenticator.client.fragment.PinFragment;
import org.wso2telco.authenticator.client.server.ServerAPI;
import org.wso2telco.authenticator.client.util.MyDevice;
import org.wso2telco.authenticator.client.util.MySettings;
import org.wso2telco.authenticator.client.util.OnSwipeTouchListener;

import static org.wso2telco.authenticator.client.util.MyFirebaseMessagingService.*;

public class ActivityAuthorize extends FragmentActivity {

    ImageView imgSP;
    TextView tvMessage;
    TextView tvSP;
    String messageId;
    int levelOfAssurance = LOA.Level2;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_authorize);
        MyDevice.setTaskBarColored(this);
        init();
        //showAuthenticator(LOA.Level0);
    }

    private void init() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
        } else {
            try {
                tvMessage = (TextView) findViewById(R.id.txtMessage);
                tvSP = (TextView) findViewById(R.id.txtSP);
                tvMessage.setText(extras.getString(INTENT_MSG));
                tvSP.setText(extras.getString(INTENT_APP_NAME));
                String strSP_URL = extras.getString(INTENT_SP_URL);
                messageId = extras.getString(INTENT_MSG_ID);
                levelOfAssurance = Integer.parseInt(extras.getString(INTENT_LOA));
                if (URLUtil.isValidUrl(strSP_URL)) {
                    showImage(strSP_URL);
                }
                showAuthenticator(levelOfAssurance,messageId);
            } catch (Exception e) {
                finish();
            }
        }
    }

    private void showImage(String imgURL) {
        imgSP = (ImageView) findViewById(R.id.imgSP);
        ImageRequest imgRequest = new ImageRequest(imgURL,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        imgSP.setImageBitmap(response);
                    }
                }, 0, 0, ImageView.ScaleType.FIT_XY, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                imgSP.setBackgroundColor(Color.parseColor("#000000"));
                error.printStackTrace();
            }
        });
        ServerAPI.getInstance(this).addToRequestQueue(imgRequest);
    }

    public void onClickCancelTransaction(View v) {
        setAuthenticationStatus(ServerAPI.Athentication.FAILED);
        ServerAPI.getInstance(context).updateAdapter("REJECTED", messageId, new ServerAPI.ResponseListener() {
            @Override
            public void onSuccess() throws JSONException {
                Log.e("onSuccess", "Fingerprint Authentication");
            }

            @Override
            public void onFailure(String reason) {
                Log.e("onFAilure", "Fingerprint Authentication");

            }
        });
    }

    public void showAuthenticator(int acr, String sessionDataKey) {
        switch (acr) {
            case LOA.Level2:
                showSwipeAuthenticator(sessionDataKey);
                break;
            case LOA.Level3:
                String AUTH_MODE = MySettings.getTransAuthMode(this);
                if (AUTH_MODE.equals(MySettings.Authentication.FINGER_PRINT)) {
                    if (MyDevice.fingerprintStatus(this) == MyDevice.FingerprintStatus.HARDWARE_SUPPORTED_AND_SET)
                        showFingerprintAuthenticator(sessionDataKey);
                    else
                        showPinAuthenticator(sessionDataKey);
                } else
                    showPinAuthenticator(sessionDataKey);
                break;
        }
    }

    public void hideAllAuthenticators() {
        View vwAuthSwipe = findViewById(R.id.authSwipe);
        vwAuthSwipe.setVisibility(View.GONE);
        View vwAuthFingerprint = findViewById(R.id.authFingerprint);
        vwAuthFingerprint.setVisibility(View.GONE);
        View vwAuthPin = findViewById(R.id.authPin);
        vwAuthPin.setVisibility(View.GONE);
    }

    public void showSwipeAuthenticator(final String sessionDataKey) {
        hideAllAuthenticators();

        View vwAuthSwipe = findViewById(R.id.authSwipe);
        vwAuthSwipe.setVisibility(View.VISIBLE);

        Button btnSwipe = (Button) findViewById(R.id.btnSwipe);
        btnSwipe.setOnTouchListener(new OnSwipeTouchListener(ActivityAuthorize.this) {
            public void onSwipeRight() {
                setAuthenticationStatus(ServerAPI.Athentication.SUCCESS);
                ServerAPI.getInstance(context).updateAdapter("APPROVED", sessionDataKey, new ServerAPI.ResponseListener() {
                    @Override
                    public void onSuccess() throws JSONException {
                        Log.e("onSuccess", "oSwipeRight");
                    }

                    @Override
                    public void onFailure(String reason) {
                        Log.e("onFAilure", "onSwipeRight");

                    }
                });
                finish();
            }

            public void onSwipeLeft() {
                setAuthenticationStatus(ServerAPI.Athentication.SUCCESS);
                ServerAPI.getInstance(context).updateAdapter("APPROVED", sessionDataKey, new ServerAPI.ResponseListener() {
                    @Override
                    public void onSuccess() throws JSONException {
                        Log.e("onSuccess", "oSwipeLeft");
                    }

                    @Override
                    public void onFailure(String reason) {
                        Log.e("onFAilure", "onSwipeLeft");

                    }
                });
                finish();
            }
        });
    }

    public void showPinAuthenticator(String sessionDataKey) {
        final String sessionDataKeyValue = sessionDataKey;
        hideAllAuthenticators();

        View vwAuthPin = findViewById(R.id.authPin);
        vwAuthPin.setVisibility(View.VISIBLE);

        PinFragment pinFragment = (PinFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_pin);
        pinFragment.setPinListener(new PinFragment.PinListener() {
            @Override
            public void onSuccess(int mode) {
                setAuthenticationStatus(ServerAPI.Athentication.SUCCESS);
                setResult(Activity.RESULT_OK);
                ServerAPI.getInstance(context).updateAdapter("APPROVED", sessionDataKeyValue, new ServerAPI.ResponseListener() {
                    @Override
                    public void onSuccess() throws JSONException {
                        Log.e("onSuccess", "Pin Authentication");
                    }

                    @Override
                    public void onFailure(String reason) {
                        Log.e("onFailure", "Pin Authentication");

                    }
                });
                finish();
            }

            @Override
            public void onAttemptExceed(int mode) {
                setAuthenticationStatus(ServerAPI.Athentication.FAILED);
                ServerAPI.getInstance(context).updateAdapter("REJECTED", sessionDataKeyValue, new ServerAPI.ResponseListener() {
                    @Override
                    public void onSuccess() throws JSONException {
                        Log.e("onSuccess", "Pin Authentication");
                    }

                    @Override
                    public void onFailure(String reason) {
                        Log.e("onFailure", "Pin Authentication");

                    }
                });
                setResult(Activity.RESULT_CANCELED);
                finish();
            }

            @Override
            public void onDataNotFound() {
                setAuthenticationStatus(ServerAPI.Athentication.FAILED);
                ServerAPI.getInstance(context).updateAdapter("REJECTED", sessionDataKeyValue, new ServerAPI.ResponseListener() {
                    @Override
                    public void onSuccess() throws JSONException {
                        Log.e("onSuccess", "Pin Authentication");
                    }

                    @Override
                    public void onFailure(String reason) {
                        Log.e("onFailure", "Pin Authentication");

                    }
                });
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }

    public void showFingerprintAuthenticator(final String sessionDataKey) {
        final String sessionDataKeyValue = sessionDataKey;
        hideAllAuthenticators();
        View vwAuthFingerprint = findViewById(R.id.authFingerprint);
        vwAuthFingerprint.setVisibility(View.VISIBLE);

        FingerprintFragment fingerprintFragment = (FingerprintFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_fingerprint);
        fingerprintFragment.setFingerListener(new FingerprintFragment.FingerprintListener() {
            @Override
            public void onChangeAuthenticationPin() {
                showPinAuthenticator(sessionDataKey);
            }

            @Override
            public void onSuccess() {
                setAuthenticationStatus(ServerAPI.Athentication.SUCCESS);
                ServerAPI.getInstance(context).updateAdapter("APPROVED", sessionDataKeyValue, new ServerAPI.ResponseListener() {
                    @Override
                    public void onSuccess() throws JSONException {
                        Log.e("onSuccess", "Fingerprint Authentication");
                    }

                    @Override
                    public void onFailure(String reason) {
                        Log.e("onFAilure", "Fingerprint Authentication");

                    }
                });
                setResult(Activity.RESULT_OK);
                finish();
            }

            @Override
            public void onAttemptExceed() {
                setAuthenticationStatus(ServerAPI.Athentication.FAILED);
                ServerAPI.getInstance(context).updateAdapter("REJECTED", sessionDataKeyValue, new ServerAPI.ResponseListener() {
                    @Override
                    public void onSuccess() throws JSONException {
                        Log.e("onSuccess", "Fingerprint Authentication");
                    }

                    @Override
                    public void onFailure(String reason) {
                        Log.e("onFAilure", "Fingerprint Authentication");

                    }
                });
                setResult(Activity.RESULT_CANCELED);
                finish();
            }

        });
    }

    public void setAuthenticationStatus(int status) {
        String strMSISDN = MySettings.getMSISDN(this);
        ServerAPI.getInstance(this).setAuthenticationStatus(strMSISDN, status, messageId, new ServerAPI.ResponseListener() {
            @Override
            public void onSuccess() throws JSONException {
                Log.e("onSuccess", "Activitymain");
            }

            @Override
            public void onFailure(String reason) {
                Log.e("onFAilure", "Activitymain");

            }
        });
        finish();
    }
}