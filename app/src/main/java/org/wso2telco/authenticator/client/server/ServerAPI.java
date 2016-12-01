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

import java.util.HashMap;
import java.util.Map;

//LAkiniiiiiiiiiiiiiiiiiiiiiiiiiiii correct
public class ServerAPI {

    private String TOKEN = "ya29.Ci-SA6OJqrlH8_KHXP2VakCj69qi_cxYN_ONfzYQXKbOyQ6-oriHmk0urBDm4dvw" ;
    private String END_POINT = "http://10.10.12.38:9763/SAA_Authenticator_v1.0/services/serverAPI/" ;

    ///lak
    String aouthCodeValue;
    String tokenCodeValue;
    String userInfo;
    //lak end

    //private String END_POINT = "http://192.168.8.105:9763/SAA_Authenticator_v1.0/services/serverAPI/" ;
    private final String API_ERROR = "API Error";
    private final String NETWORK_ERROR = "Network Error";

    private final String STATUS = "status";
    private final String MESSAGE_ID = "ref";

    private static ServerAPI mInstance;
    private static Context mCtx;
    private RequestQueue mRequestQueue;

    public interface Athentication {
        public static int SUCCESS = 1;
        public static int FAILED = 0 ;
    }

    private ResponseListener responseListener  ;

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public static synchronized ServerAPI getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ServerAPI(context);


        }
        return mInstance;
    }

    public ServerAPI(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }


    // Device Registration
    public void register (final String deviceId, final String pushToken, final String platform, final ResponseListener responseListener) throws
            JSONException {
        String url = END_POINT + "api/v1/clients";
        this.responseListener = responseListener ;
        Log.e("inside regendpoint",url);

        JSONObject params = new JSONObject();
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
                            Log.e("rsponse",response.toString());
                            if (response.getInt("success") == 1){
                                Log.e("rsponse success","Success API");
                                responseListener.onSuccess();
                            }
                            else {
                                JSONObject jsonErrorObject = response.getJSONObject("result") ;
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
                Log.e("error registered method",error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                //params.put("Authorization", "Bearer " + TOKEN);
                params.put("msisdn", "712295446");
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

    // Send Authentication Status
    public void setAuthenticationStatus (final String MSISDN,final int status,final String ref,final ResponseListener responseListener) {

        String url = END_POINT + "api/v1/clients/#/auth_response";
        url = url.replace("#",MSISDN);

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
                                JSONObject jsonErrorObject = response.getJSONObject("result") ;
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
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + TOKEN);
                return params;
            }

//            @Override
//            public byte[] getBody() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put(STATUS,String.valueOf(status));
//                params.put(MESSAGE_ID,ref);
//                return params.toString().getBytes();
//            }
        };
        addToRequestQueue(jsonObjReq);
    }

    public interface ResponseListener {
        void onSuccess() throws JSONException;
        void onFailure(String reason) ;
    }

}
