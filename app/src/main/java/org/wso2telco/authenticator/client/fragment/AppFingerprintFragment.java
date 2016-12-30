
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

package org.wso2telco.authenticator.client.fragment;

        import android.annotation.TargetApi;
        import android.hardware.fingerprint.FingerprintManager;
        import android.os.Build;
        import android.os.Bundle;
        import android.support.v4.app.Fragment;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.view.animation.AlphaAnimation;
        import android.view.animation.Animation;
        import android.widget.Button;
        import android.widget.LinearLayout;
        import android.widget.RelativeLayout;
        import android.widget.TextView;


        import org.wso2telco.authenticator.client.R;
        import org.wso2telco.authenticator.client.fingerprint.FingerprintHelper;


@TargetApi(Build.VERSION_CODES.M)
public class AppFingerprintFragment extends Fragment
        implements FingerprintHelper.Callback {

    private Button btnChangeAuthenticationPin;
    private FingerprintManager.CryptoObject mCryptoObject;
    private FingerprintHelper mFingerprintUiHelper;
    private View fragmentView;

    public static final String STATUS = "STATUS";
    FingerprintListener fingerprintListener;

    public interface Return {
        public static int SHOW_PIN = 0;
        public static int SUCCESS = 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fingerprint, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        fragmentView = view;
        init();
    }


    public void init() {
        btnChangeAuthenticationPin = (Button) fragmentView.findViewById(R.id.btnPasswordAuthentication);
        this.btnChangeAuthenticationPin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                changeToPasswordAuthentication();
            }
        });

        mFingerprintUiHelper = new FingerprintHelper(getActivity().getSystemService(FingerprintManager.class), this);
        if (!mFingerprintUiHelper.isFingerprintAuthAvailable())
            mFingerprintUiHelper.stopListening();
    }

    public void changeToPasswordAuthentication() {
        mFingerprintUiHelper.stopListening();
        fingerprintListener.onChangeAuthenticationPin();
    }

    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }

    @Override
    public void onAuthenticated() {
        mFingerprintUiHelper.stopListening();
        fingerprintListener.onSuccess();
    }

    @Override
    public void onError() {
        showError();
    }

    public void showError() {
        RelativeLayout layoutError = (RelativeLayout) fragmentView.findViewById(R.id.fingerprintsettings);
        layoutError.setVisibility(View.VISIBLE);
    }

    public void setFingerListener(AppFingerprintFragment.FingerprintListener fingerprintListener) {
        this.fingerprintListener = fingerprintListener;
    }

    public interface FingerprintListener {
        void onChangeAuthenticationPin();

        void onSuccess();

        void onAttemptExceed();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFingerprintUiHelper.startListening(mCryptoObject);
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerprintUiHelper.stopListening();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fingerprintListener = null;
        mFingerprintUiHelper = null;
    }
}
