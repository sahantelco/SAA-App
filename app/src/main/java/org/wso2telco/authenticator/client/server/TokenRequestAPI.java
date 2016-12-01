package org.wso2telco.authenticator.client.server;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lakini on 11/28/16.
 */

public class TokenRequestAPI {
//
//    // Device Registration
//    public void register (final String deviceId, final String pushToken, final String platform, final ServerAPI.ResponseListener responseListener) throws JSONException {
//        String url = END_POINT + "api/v1/clients";
//        this.responseListener = responseListener ;
//        Log.e("inside register method","device ID");
//
//        JSONObject params = new JSONObject();
//        try {
//            params.put("clientDeviceID", deviceId);
//            params.put("platform", platform);
//            params.put("pushToken", pushToken);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
//                url, params,
//                new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            Log.e("rsponse",response.toString());
//                            if (response.getInt("success") == 1){
//                                Log.e("rsponse success","Success API");
//                                responseListener.onSuccess();
//                            }
//                            else {
//                                JSONObject jsonErrorObject = response.getJSONObject("result") ;
//                                responseListener.onFailure(jsonErrorObject.getString("message"));
//                            }
//                        } catch (Exception JSonException) {
//                            JSonException.printStackTrace();
//                            responseListener.onFailure(API_ERROR);
//                        }
//                    }
//                }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                error.printStackTrace();
//                responseListener.onFailure(NETWORK_ERROR);
//                Log.e("error registered method",error.toString());
//            }
//        }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String>  params = new HashMap<String, String>();
//                //params.put("Authorization", "Bearer " + TOKEN);
//                params.put("msisdn", "712295446");
//                params.put("Content-Type", "application/json; charset=utf-8");
//                return params;
//            }
//
//        };
//        addToRequestQueue(jsonObjReq);
//
//    }
//
//
//


}
