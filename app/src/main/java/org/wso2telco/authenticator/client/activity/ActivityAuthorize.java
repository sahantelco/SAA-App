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
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import org.json.JSONException;
import org.wso2telco.authenticator.client.R;
import org.wso2telco.authenticator.client.fragment.FingerprintFragment;
import org.wso2telco.authenticator.client.fragment.PinFragment;
import org.wso2telco.authenticator.client.oauthconnection.EnvironmentDTO;
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
                //showImageSP(EnvironmentDTO.serviceproviderURL);
                if (URLUtil.isValidUrl(strSP_URL)) {
                    showImageOperator(strSP_URL);
                }
                showAuthenticator(levelOfAssurance);
            } catch (Exception e) {
                finish();
            }
        }
    }

    /**
     * Display the Operator's icon in the Activity
     *
     * @param imgURL
     */
    private void showImageOperator(String imgURL) {
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

    /**
     * Display the Service Provider's icon in the Activity
     *
     * @param imgURL
     */
    private void showImageSP(String imgURL) {
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
        setAuthenticationStatus(ServerAPI.Athentication.FAILED, ServerAPI.RequestStatus.REJECTED);
    }

    public void showAuthenticator(int acr) {
        switch (acr) {
            case LOA.Level2:
                showSwipeAuthenticator();
                break;
            case LOA.Level3:
                String AUTH_MODE = MySettings.getTransAuthMode(this);
                if (AUTH_MODE.equals(MySettings.Authentication.FINGER_PRINT)) {
                    if (MyDevice.fingerprintStatus(this) == MyDevice.FingerprintStatus.HARDWARE_SUPPORTED_AND_SET)
                        showFingerprintAuthenticator();
                    else
                        showPinAuthenticator();
                } else
                    showPinAuthenticator();
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

    public void showSwipeAuthenticator() {
        hideAllAuthenticators();

        View vwAuthSwipe = findViewById(R.id.authSwipe);
        vwAuthSwipe.setVisibility(View.VISIBLE);

        Button btnSwipe = (Button) findViewById(R.id.btnSwipe);
        btnSwipe.setOnTouchListener(new OnSwipeTouchListener(ActivityAuthorize.this) {
            public void onSwipeRight() {
                setAuthenticationStatus(ServerAPI.Athentication.SUCCESS, ServerAPI.RequestStatus.APPROVED);
            }

            public void onSwipeLeft() {
                setAuthenticationStatus(ServerAPI.Athentication.SUCCESS, ServerAPI.RequestStatus.APPROVED);
            }
        });
    }

    public void showPinAuthenticator() {
        hideAllAuthenticators();

        View vwAuthPin = findViewById(R.id.authPin);
        vwAuthPin.setVisibility(View.VISIBLE);

        PinFragment pinFragment = (PinFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_pin);
        pinFragment.setPinListener(new PinFragment.PinListener() {
            @Override
            public void onSuccess(int mode) {
                setAuthenticationStatus(ServerAPI.Athentication.SUCCESS, ServerAPI.RequestStatus.APPROVED);
                setResult(Activity.RESULT_OK);
            }

            @Override
            public void onAttemptExceed(int mode) {
                setResult(Activity.RESULT_CANCELED);
                setAuthenticationStatus(ServerAPI.Athentication.FAILED, ServerAPI.RequestStatus.REJECTED);
            }

            @Override
            public void onDataNotFound() {
                setResult(Activity.RESULT_CANCELED);
                setAuthenticationStatus(ServerAPI.Athentication.FAILED, ServerAPI.RequestStatus.REJECTED);
            }
        });
    }

    public void showFingerprintAuthenticator() {
        hideAllAuthenticators();
        View vwAuthFingerprint = findViewById(R.id.authFingerprint);

        vwAuthFingerprint.setVisibility(View.VISIBLE);

        FingerprintFragment fingerprintFragment = (FingerprintFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_fingerprint);
        fingerprintFragment.setFingerListener(new FingerprintFragment.FingerprintListener() {
            @Override
            public void onChangeAuthenticationPin() {
                showPinAuthenticator();
            }

            @Override
            public void onSuccess() {
                setResult(Activity.RESULT_OK);
                setAuthenticationStatus(ServerAPI.Athentication.SUCCESS, ServerAPI.RequestStatus.APPROVED);
            }

            @Override
            public void onAttemptExceed() {
                setResult(Activity.RESULT_CANCELED);
                setAuthenticationStatus(ServerAPI.Athentication.FAILED, ServerAPI.RequestStatus.REJECTED);
            }
        });
    }

    /**
     * Update the SAA Server and SAA Adapter after the user do a transaction
     *
     * @param status       to update the SAA Server
     * @param updateStatus to update the SAA Adapter
     */
    public void setAuthenticationStatus(int status, final String updateStatus) {
        String strMSISDN = ServerAPI.MSISDN;
        Log.d("msisdn at setAuthen", strMSISDN);

        //Calling SAA Server to update the transaction status as cancelled.
        ServerAPI.getInstance(this).setAuthenticationStatus(strMSISDN, status, messageId, new ServerAPI.ResponseListener() {
            @Override
            public void onSuccess() throws JSONException {
                Log.e("onSuccess", "Activitymain");
                // Calling SAA Adapter to update the transaction status las cancelled.
//                ServerAPI.getInstance(context).updateAdapter(updateStatus, messageId, new ServerAPI.ResponseListener
//                        () {
//                    @Override
//                    public void onSuccess() throws JSONException {
//                        Log.e("onSuccess", "On cancel");
//                        notifyUserAndGoBackground("Authentication complete");
//                    }
//
//                    @Override
//                    public void onFailure(String reason) {
//                        Log.e("onFailure", "On cancel");
//                        notifyUserAndGoBackground("Server Error");
//                    }
//                });
            }

            @Override
            public void onFailure(String reason) {
                Log.e("onFailure", "On cancel");
                notifyUserAndGoBackground("Server Error");
            }
        });
    }

    /**
     * Dispaly the dialog box after the transaction authorized.
     *
     * @param message
     */
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
                finish();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}