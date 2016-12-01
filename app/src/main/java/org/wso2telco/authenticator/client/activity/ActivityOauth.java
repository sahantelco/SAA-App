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
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.wso2telco.authenticator.client.R;
import org.wso2telco.authenticator.client.oauthconnection.EnvironmentDTO;

public class ActivityOauth extends Activity {
    private WebView wv1;
    String endpoint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        endpoint = getMyUrl();
        //Log.d("endpoint", endpoint);
        wv1 = (WebView) findViewById(R.id.webview01);
        //wv1.setWebViewClient(new myWebClient());
        wv1.getSettings().setJavaScriptEnabled(true);
        wv1.getSettings().setDomStorageEnabled(true);

        wv1.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")

            public boolean shouldOverrideUrlLoading(WebView view,String url) {
                try{
                    view.loadUrl(url);
                    Log.d("url", url);
                    Log.d("zz", "boolean method!!");
                    if(view.getUrl().contains("playground2")&& view.getUrl().contains("&code=")){
                        Log.d("zz", "boolean method-inside!!");
                        Intent intent = new Intent(getBaseContext(), ActivitySplash.class);
                        intent.putExtra("URL", view.getUrl());
                        finish();
                        startActivity(intent);
                    }

                }
                catch(Exception e){
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        wv1.clearCache(true);
        wv1.clearHistory();
        wv1.loadUrl(endpoint);
    }

    private String getMyUrl(){
        String scope = EnvironmentDTO.scope;
        if(scope.contains("openid")){
            String url = EnvironmentDTO.getOpenidEndpoint()+"&scope="+scope+"&redirect_uri="+EnvironmentDTO.getCallBackUrl()+"&client_id="+EnvironmentDTO.getClientID();
            return url;
        }
        return null;
    }
}
