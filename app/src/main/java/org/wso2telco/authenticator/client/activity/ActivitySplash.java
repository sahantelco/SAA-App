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

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2telco.authenticator.client.R;
import org.wso2telco.authenticator.client.oauthconnection.EnvironmentDTO;
import org.wso2telco.authenticator.client.server.ServerAPI;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

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

public class ActivitySplash extends Activity {
    String aouthCodeValue;
    String tokenCodeValue;
    private static int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            aouthCodeValue = getAouthCodeFromUrl(extras.getString("URL"));
            Log.d("Aouth Code value", aouthCodeValue);
        }

        TokenRequest tokenTask = new TokenRequest();
        if (Build.VERSION.SDK_INT >= 11)
            tokenTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            tokenTask.execute();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent i = new Intent(ActivitySplash.this, ActivityMain.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
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
            httpPost.addHeader("Authorization", EnvironmentDTO.getAOutherizationHeaderValue());
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
            ServerAPI.TOKEN = tokenCodeValue;
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

//    private class UserInfoRequest extends AsyncTask<Void, Void, String> {
//        HttpResponse response;
//
//        protected String doInBackground(Void... urls) {
//
//            DefaultHttpClient httpclient = new DefaultHttpClient();
//            HttpGet httpGet = new HttpGet(EnvironmentDTO.getUserInfoEndpoint());
//            httpGet.addHeader("Authorization", "Bearer " + tokenCodeValue);
//            httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");
//
//            try {
//                //disableSSLCertificateChecking();
//                response = httpclient.execute(httpGet);
//                getJsonObject(response);
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            } catch (ClientProtocolException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (RuntimeException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        protected void onPostExecute(String result) {
//            user_Info.setText(userInfo);
//        }
//
//        private void getJsonObject(HttpResponse response) {
//            BufferedReader rd = null;
//            //String token =null;
//            try {
//                rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//            } catch (UnsupportedOperationException e1) {
//                e1.printStackTrace();
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//
//            StringBuffer result = new StringBuffer();
//            String line = "";
//            try {
//                while ((line = rd.readLine()) != null) {
//                    result.append(line);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            try {
//                JSONObject o = new JSONObject(result.toString());
//                userInfo = o.toString();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
//
//    }

}
