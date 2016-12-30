package org.wso2telco.authenticator.client.fingerprint;

import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;

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

@TargetApi(Build.VERSION_CODES.M)
public class AppFingerprintHelper extends FingerprintManager.AuthenticationCallback {


    private final FingerprintManager fingerPrintManager;
    private final Callback callBack;
    private CancellationSignal cancellationSignal;

    private boolean mSelfCancelled;

    public AppFingerprintHelper(FingerprintManager fingerprintManager, Callback callback) {
        fingerPrintManager = fingerprintManager;
        callBack = callback;
    }

    public boolean isFingerprintAuthAvailable() {
        // noinspection ResourceType
        return fingerPrintManager.isHardwareDetected()
                && fingerPrintManager.hasEnrolledFingerprints();
    }

    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        if (!isFingerprintAuthAvailable()) {
            return;
        }
        cancellationSignal = new CancellationSignal();
        mSelfCancelled = false;
        // noinspection ResourceType
        fingerPrintManager
                .authenticate(cryptoObject, cancellationSignal, 0 /* flags */, this, null);
    }

    public void stopListening() {
        if (cancellationSignal != null) {
            mSelfCancelled = true;
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!mSelfCancelled) {
            callBack.onError();
        }
    }

    @Override
    public void onAuthenticationFailed() {
        callBack.onError();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        callBack.onAuthenticated();
    }

    public interface Callback {
        void onAuthenticated();

        void onError();
    }
}
