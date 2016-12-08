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
import android.os.Bundle;
import org.wso2telco.authenticator.client.util.MySettings;

public class ActivityLauncher extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean status = MySettings.getActivityOAuth(this);
        if (status == false) {
            startActivity(new Intent(this, ActivityOauth.class));
            MySettings.setActivityOAuth(this,true);
        } else {
            startActivity(new Intent(this, ActivityMain.class));
        }
        finish();
    }
}
