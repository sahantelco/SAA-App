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

package org.wso2telco.authenticator.client.server;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.wso2telco.authenticator.client.oauthconnection.EnvironmentDTO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerAPI {

    public static String TOKEN;
    public static String MSISDN;
    private static ServerAPI mInstance;
    private static Context mCtx;
    private final String API_ERROR = "API Error";
    private final String NETWORK_ERROR = "Network Error";
    private String END_POINT = "http://10.10.12.38:9763/SAA_Authenticator_v1.0/services/serverAPI/";
    //private String END_POINT = "http://sandbox.mconnect.wso2telco.com/SAA_Authenticator_v1.0/services/serverAPI/";
    //private String END_POINT ="http://192.168.8.103:9763/SAA_Authenticator_v1.0/services/serverAPI/";

    private RequestQueue mRequestQueue;
    private ResponseListener responseListener;

    //MCC and MNC values of the service providers who supports Mobile Connect.
    public static List<Integer> HNI_MCC = Arrays.asList(413);
    public static List<Integer> HNI_MNC = Arrays.asList(2);

    public ServerAPI(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized ServerAPI getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ServerAPI(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    /**
     * Register clients in the Database by calling "api/v1/clients" API.
     *
     * @param deviceId
     * @param platform
     * @param pushToken
     */
    public void register(final String deviceId, final String pushToken, final String platform, final ResponseListener responseListener) throws
            JSONException {
        final String url = END_POINT + "api/v1/clients";
        Log.d("URL", url);

        this.responseListener = responseListener;
        JSONObject params = new JSONObject();

        Log.d("deviceID", deviceId);
        Log.d("Platform", platform);
        Log.d("pushToken", pushToken);


        try {
            params.put("clientDeviceID", deviceId);
            params.put("platform", platform);
            params.put("pushToken", pushToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.e("response", response.toString());
                            if (response.getInt("success") == 1) {
                                responseListener.onSuccess();
                            } else {
                                JSONObject jsonErrorObject = response.getJSONObject("result");
                                responseListener.onFailure(jsonErrorObject.getString("message"));
                            }
                        } catch (Exception JSonException) {
                            Log.e("response exception", "exception");
                            JSonException.printStackTrace();
                            responseListener.onFailure(API_ERROR);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                responseListener.onFailure(NETWORK_ERROR);
                Log.e("error registered method", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + TOKEN);
                params.put("msisdn", "911111111111");
                params.put("Content-Type", "application/json; charset=utf-8");
                return params;
            }
        };

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        addToRequestQueue(jsonObjReq);

    }

    /**
     * Update the  Status for the request by calling "api/v1/clients/#/auth_response"
     * API.
     *
     * @param MSISDN
     * @param status
     * @param ref
     */
    public void setAuthenticationStatus(final String MSISDN, final int status, final String ref, final ResponseListener responseListener) {

        String url = END_POINT + "api/v1/clients/#/auth_response";
        url = url.replace("#", MSISDN);

        JSONObject params = new JSONObject();
        try {
            params.put("status", status);
            params.put("refId", ref);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getInt("success") == 1)
                                responseListener.onSuccess();
                            else {
                                JSONObject jsonErrorObject = response.getJSONObject("result");
                                responseListener.onFailure(jsonErrorObject.getString("message"));
                            }
                        } catch (Exception JSonException) {
                            JSonException.printStackTrace();
                            responseListener.onFailure(API_ERROR);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                responseListener.onFailure(NETWORK_ERROR);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + TOKEN);
                return params;
            }
        };
        addToRequestQueue(jsonObjReq);
    }

    /**
     * Send the status of the request to adapter
     *
     * @param status
     * @param ref
     */
    public void updateAdapter(final String status, final String ref, final ResponseListener responseListener) {

        String url = EnvironmentDTO.updatestatus_url;
        Log.d("Session data key", ref);

        JSONObject params = new JSONObject();
        try {
            params.put("status", status);
            params.put("sessionDataKey", ref);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            if (response.getString("updateStatus") == "success")
                                responseListener.onSuccess();
                            else {
                                JSONObject jsonErrorObject = response.getJSONObject("result");
                                responseListener.onFailure(jsonErrorObject.getString("error"));
                            }
                        } catch (Exception JSonException) {
                            JSonException.printStackTrace();
                            responseListener.onFailure(API_ERROR);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                responseListener.onFailure(NETWORK_ERROR);
            }

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + TOKEN);
                return params;
            }

        };
        addToRequestQueue(jsonObjReq);
    }

    public interface Athentication {
        public static int SUCCESS = 1;
        public static int FAILED = 0;
    }

    public interface RequestStatus {
        public static String APPROVED = "APPROVED";
        public static String REJECTED = "REJECTED";
    }

    public interface ResponseListener {
        void onSuccess() throws JSONException;

        void onFailure(String reason);
    }
}
