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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.wso2telco.authenticator.client.R;

import org.wso2telco.authenticator.client.fragment.PinFragment;
import org.wso2telco.authenticator.client.util.MyDevice;


public class ActivityPin extends FragmentActivity  {

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_pin);
        MyDevice.setTaskBarColored(this);

        PinFragment pinFragment = (PinFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_pin);
        pinFragment.setPinListener(new PinFragment.PinListener() {
            @Override
            public void onSuccess(int mode) {
                setResult(Activity.RESULT_OK);
                finish() ;
            }

            @Override
            public void onAttemptExceed(int mode) {
                setResult(Activity.RESULT_CANCELED);
                finish() ;
            }

            @Override
            public void onDataNotFound() {
                setResult(Activity.RESULT_CANCELED);
                finish() ;
            }
        });
    }
}
