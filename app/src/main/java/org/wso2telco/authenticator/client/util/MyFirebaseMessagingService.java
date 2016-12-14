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

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.wso2telco.authenticator.client.activity.ActivityAuthorize;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    public static final String INTENT_MSG = "msg";
    public static final String INTENT_APP_NAME = "sp";
    public static final String INTENT_MSG_ID = "ref";
    public static final String INTENT_LOA = "acr";
    public static final String INTENT_SP_URL = "sp_url";

    public interface LOA {
        public static int Level2 = 2;
        public static int Level3 = 3;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e("Message", "Message Receiver");
        if (remoteMessage.getData().size() > 0) {
            showAuthorizeActivity(remoteMessage);
        }
    }

    public void showAuthorizeActivity(RemoteMessage remoteMessage) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(INTENT_MSG, remoteMessage.getData().get(INTENT_MSG));
            Log.d("Intent_msg", remoteMessage.getData().get(INTENT_MSG));
            intent.putExtra(INTENT_MSG_ID, remoteMessage.getData().get(INTENT_MSG_ID));
            intent.putExtra(INTENT_APP_NAME, remoteMessage.getData().get(INTENT_APP_NAME));
            Log.d("Intent_AppNAm", remoteMessage.getData().get(INTENT_APP_NAME));
            intent.putExtra(INTENT_LOA, remoteMessage.getData().get(INTENT_LOA));
            intent.putExtra(INTENT_SP_URL, remoteMessage.getData().get(INTENT_SP_URL));
            Log.d("Intent_apurl", remoteMessage.getData().get(INTENT_SP_URL));
            ComponentName cn = new ComponentName(getApplicationContext(), ActivityAuthorize.class);
            intent.setComponent(cn);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}