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

package org.wso2telco.authenticator.client.fingerprint;

import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;


@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHelper extends FingerprintManager.AuthenticationCallback {


    private final FingerprintManager mFingerprintManager;
    private final Callback mCallback;
    private CancellationSignal mCancellationSignal;

    private boolean mSelfCancelled;

    public FingerprintHelper(FingerprintManager fingerprintManager, Callback callback) {
        mFingerprintManager = fingerprintManager;
        mCallback = callback;
    }


    public boolean isFingerprintAuthAvailable() {
        // noinspection ResourceType
        return mFingerprintManager.isHardwareDetected()
                && mFingerprintManager.hasEnrolledFingerprints();
    }

    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        if (!isFingerprintAuthAvailable()) {
            return;
        }
        mCancellationSignal = new CancellationSignal();
        mSelfCancelled = false;
        // noinspection ResourceType
        mFingerprintManager
                .authenticate(cryptoObject, mCancellationSignal, 0 /* flags */, this, null);
    }

    public void stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!mSelfCancelled) {
            mCallback.onError();
        }
    }

    @Override
    public void onAuthenticationFailed() {
        mCallback.onError();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        mCallback.onAuthenticated();
    }

    public interface Callback {
        void onAuthenticated();
        void onError();
    }
}
