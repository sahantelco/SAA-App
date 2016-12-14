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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.wso2telco.authenticator.client.R;
import org.wso2telco.authenticator.client.fragment.PinFragment;
import org.wso2telco.authenticator.client.util.MySettings;
import org.wso2telco.authenticator.client.util.MyDevice;

public class ActivitySettings extends Activity {

    static final int SETTINGS = 100;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
        MyDevice.setTaskBarColored(this);
    }

    private void init() {
        String transAuthMode = MySettings.getTransAuthMode(this);
        String appAuthMode = MySettings.getAppAuthMode(this);

        RadioGroup opt_trans = (RadioGroup) findViewById(R.id.opt_trans);
        opt_trans.setOnCheckedChangeListener(listener);

        RadioGroup opt_app = (RadioGroup) findViewById(R.id.opt_app);
        opt_app.setOnCheckedChangeListener(listener);

        switch (transAuthMode) {
            case MySettings.Authentication.PIN:
                opt_trans.check(R.id.opt_trans_pin);
                break;
            case MySettings.Authentication.FINGER_PRINT:
                opt_trans.check(R.id.opt_trans_fingerprint);
                break;
        }

        switch (appAuthMode) {
            case MySettings.Authentication.PIN:
                opt_app.check(R.id.opt_app_pin);
                break;
            case MySettings.Authentication.FINGER_PRINT:
                opt_app.check(R.id.opt_app_fingerprint);
                break;
        }
    }

    private void showChangePinActivity() {
        Intent i = new Intent(ActivitySettings.this, ActivityPin.class);
        i.putExtra(PinFragment.SECURITY_TYPE, PinFragment.SECURITY_CHANGE_PIN);
        startActivityForResult(i, SETTINGS);
    }

    public void onClickChangePin(View v) {
        showChangePinActivity();
    }

    public void onClickClose(View v) {
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Toast.makeText(ActivitySettings.this, R.string.success_pin_change, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener() {

        public void onCheckedChanged(RadioGroup group, int checkedId) {

            int deviceFingerprintStatus;

            switch (checkedId) {
                case R.id.opt_app_pin:
                    MySettings.setAppAuthMode(ActivitySettings.this, MySettings.Authentication.PIN);
                    break;
                case R.id.opt_app_fingerprint:
                    deviceFingerprintStatus = MyDevice.fingerprintStatus(ActivitySettings.this);
                    switch (deviceFingerprintStatus) {
                        case MyDevice.FingerprintStatus.HARDWARE_SUPPORTED_AND_SET:
                            MySettings.setAppAuthMode(ActivitySettings.this, MySettings.Authentication.FINGER_PRINT);
                            break;
                        case MyDevice.FingerprintStatus.HARDWARE_SUPPORTED_AND_NOT_SET:
                            Toast.makeText(ActivitySettings.this,
                                    getResources().getString(R.string.error_not_set_finger_print),
                                    Toast.LENGTH_LONG).show();
                            group.check(R.id.opt_app_pin);
                            break;
                        case MyDevice.FingerprintStatus.HARDWARE_NOT_SUPPORTED:
                            Toast.makeText(ActivitySettings.this,
                                    getResources().getString(R.string.error_not_supported_finger_print),
                                    Toast.LENGTH_LONG).show();
                            group.check(R.id.opt_app_pin);
                            break;
                    }
                    break;
                case R.id.opt_trans_pin:
                    MySettings.setTransAuthMode(ActivitySettings.this, MySettings.Authentication.PIN);
                    break;
                case R.id.opt_trans_fingerprint:
                    deviceFingerprintStatus = MyDevice.fingerprintStatus(ActivitySettings.this);
                    switch (deviceFingerprintStatus) {
                        case MyDevice.FingerprintStatus.HARDWARE_SUPPORTED_AND_SET:
                            MySettings.setTransAuthMode(ActivitySettings.this, MySettings.Authentication.FINGER_PRINT);
                            break;
                        case MyDevice.FingerprintStatus.HARDWARE_SUPPORTED_AND_NOT_SET:
                            Toast.makeText(ActivitySettings.this,
                                    getResources().getString(R.string.error_not_set_finger_print),
                                    Toast.LENGTH_LONG).show();
                            group.check(R.id.opt_trans_pin);
                            break;
                        case MyDevice.FingerprintStatus.HARDWARE_NOT_SUPPORTED:
                            Toast.makeText(ActivitySettings.this,
                                    getResources().getString(R.string.error_not_supported_finger_print),
                                    Toast.LENGTH_LONG).show();
                            group.check(R.id.opt_trans_pin);
                            break;
                    }
            }
        }
    };
}
