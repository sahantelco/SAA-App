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


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.wso2telco.authenticator.client.R;
import org.wso2telco.authenticator.client.util.MyDevice;
import org.wso2telco.authenticator.client.util.MySettings;

import java.util.ArrayList;
import java.util.List;

public class PinFragment extends Fragment {

    public static final String EMPTY_STRING = "";
    public static final int SECURITY_TYPE_ASK_FOR_PIN = 101;
    public static final int SECURITY_TYPE_NEW_PIN = 100;
    public static final int SECURITY_CHANGE_PIN = 103;
    public static final int SECURITY_CHANGE_PIN_EXT = 104;
    public static final String SECURITY_TYPE = "SECURITY_TYPE";
    private static final int PIN_LENGTH = 4;

    private View fragmentView;

    Button button_0;
    Button button_1;
    Button button_2;
    Button button_3;
    Button button_4;
    Button button_5;
    Button button_6;
    Button button_7;
    Button button_8;
    Button button_9;
    Button button_back;
    Button button_clear;

    private String firstPin;
    private int mPinEnteredCount;
    private String mPin;
    private int retryCount;
    private int SCREEN_MODE;
    TextView textCode;
    TextView textTitle;

    private PinListener pinListener;

    class OnClickListener implements View.OnClickListener {
        final int val$digit;

        OnClickListener(int i) {
            this.val$digit = i;
        }

        public void onClick(View view) {
            insertPinDigit(this.val$digit);
        }
    }

    public static PinFragment newInstance(PinListener listener) {
        PinFragment fragment = new PinFragment();
        fragment.pinListener = listener;
        return fragment;
    }

    public PinFragment() {
        this.mPinEnteredCount = 0;
        this.mPin = EMPTY_STRING;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        fragmentView = view;
        init();
    }


    @SuppressWarnings("rawtypes")
    private void init() {
        button_0 = (Button) fragmentView.findViewById(R.id.button_security_0);
        button_1 = (Button) fragmentView.findViewById(R.id.button_security_1);
        button_2 = (Button) fragmentView.findViewById(R.id.button_security_2);
        button_3 = (Button) fragmentView.findViewById(R.id.button_security_3);
        button_4 = (Button) fragmentView.findViewById(R.id.button_security_4);
        button_5 = (Button) fragmentView.findViewById(R.id.button_security_5);
        button_6 = (Button) fragmentView.findViewById(R.id.button_security_6);
        button_7 = (Button) fragmentView.findViewById(R.id.button_security_7);
        button_8 = (Button) fragmentView.findViewById(R.id.button_security_8);
        button_9 = (Button) fragmentView.findViewById(R.id.button_security_9);
        button_back = (Button) fragmentView.findViewById(R.id.button_security_back);
        button_clear = (Button) fragmentView.findViewById(R.id.button_security_clear);
        textCode = (TextView) fragmentView.findViewById(R.id.pin_display);
        textTitle = (TextView) fragmentView.findViewById(R.id.text_security_title);

        //List<Button> arrayList = new ArrayList();

        ArrayList<Button> arrayList = new ArrayList<Button>();

        arrayList.add(this.button_0);
        arrayList.add(this.button_1);
        arrayList.add(this.button_2);
        arrayList.add(this.button_3);
        arrayList.add(this.button_4);
        arrayList.add(this.button_5);
        arrayList.add(this.button_6);
        arrayList.add(this.button_7);
        arrayList.add(this.button_8);
        arrayList.add(this.button_9);

        int buttonIndex = 0;
        for (Button onClickListener : arrayList) {
            onClickListener.setOnClickListener(new OnClickListener(buttonIndex));
            buttonIndex++;
        }

        this.button_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                removeLastDigit();
            }
        });
        this.button_clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                removeAllDigits();
            }
        });
    }

    private void check() {
        switch (this.SCREEN_MODE) {
            case SECURITY_TYPE_NEW_PIN:
                if (this.firstPin.equals(EMPTY_STRING)) {
                    this.firstPin = this.mPin;
                    this.textTitle.setText(getString(R.string.pin_enter_second));
                    removeAllDigits();
                } else if (this.firstPin.equals(this.mPin)) {
                    setNewPin(this.mPin);
                    pinListener.onSuccess(SECURITY_TYPE_NEW_PIN);
                } else {
                    this.firstPin = EMPTY_STRING;
                    this.textTitle.setText(getString(R.string.pin_enter_first));
                    removeAllDigits();
                    Toast.makeText(getActivity(), R.string.pin_mismatch, Toast.LENGTH_SHORT).show();
                }
                break;

            case SECURITY_TYPE_ASK_FOR_PIN:
                if (isValidPin(this.mPin)) {
                    pinListener.onSuccess(SECURITY_TYPE_ASK_FOR_PIN);
                } else {
                    removeAllDigits();
                    Toast.makeText(getActivity(), R.string.pin_mismatch, Toast.LENGTH_SHORT).show();
                    int i = this.retryCount + 1;
                    this.retryCount = i;
                    if (i == 3)
                        pinListener.onAttemptExceed(SECURITY_TYPE_ASK_FOR_PIN);
                }
                break;

            case SECURITY_CHANGE_PIN:
                if (!isValidPin(this.mPin)) {
                    removeAllDigits();
                    this.mPin = MySettings.getPinCode(getActivity());
                    Toast.makeText(getActivity(), R.string.pin_mismatch, Toast.LENGTH_SHORT).show();
                } else if (isValidPin(this.mPin)) {
                    removeAllDigits();
                    this.firstPin = EMPTY_STRING;
                    this.textTitle.setText(getString(R.string.pin_enter_new));
                    this.SCREEN_MODE = SECURITY_CHANGE_PIN_EXT;
                }
                break;

            case SECURITY_CHANGE_PIN_EXT:
                if (this.firstPin.equals(EMPTY_STRING)) {
                    this.firstPin = this.mPin;
                    this.textTitle.setText(getString(R.string.pin_enter_second));
                    removeAllDigits();
                } else if (this.firstPin.equals(this.mPin)) {
                    MySettings.setPinCode(getActivity(), this.firstPin);
                    pinListener.onSuccess(SECURITY_CHANGE_PIN);
                } else {
                    this.firstPin = EMPTY_STRING;
                    this.textTitle.setText(getString(R.string.pin_enter_first));
                    removeAllDigits();
                    Toast.makeText(getActivity(), R.string.pin_mismatch, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    public void onResume() {
        super.onResume();
        this.retryCount = 0;
        this.firstPin = EMPTY_STRING;
        this.mPinEnteredCount = 0;
        Bundle extras = getActivity().getIntent().getExtras();

        try {
            this.SCREEN_MODE = extras.getInt(SECURITY_TYPE);
        } catch (Exception e) {
            this.SCREEN_MODE = SECURITY_TYPE_ASK_FOR_PIN;
        }

        switch (this.SCREEN_MODE) {
            case SECURITY_TYPE_NEW_PIN:
                this.textTitle.setText(R.string.pin_enter_new);
                break;
            case SECURITY_TYPE_ASK_FOR_PIN:
                this.textTitle.setText(R.string.pin_enter_first);
                break;
            case SECURITY_CHANGE_PIN:
                this.firstPin = MySettings.getPinCode(getActivity());
                this.textTitle.setText(R.string.pin_enter_old_pin);
                break;
            default:
                this.SCREEN_MODE = SECURITY_TYPE_ASK_FOR_PIN;
                this.textTitle.setText(R.string.pin_enter_first);
        }
    }

    private void insertPinDigit(int i){
        int count = this.mPinEnteredCount + 1;
        this.mPinEnteredCount = count;
        if (count <= PIN_LENGTH) {
            this.mPin += i;
            this.textCode.append("\u2022");
            if (this.mPinEnteredCount == PIN_LENGTH) {
                check();
            }
        }
    }

    private void removeLastDigit() {
        if (this.mPin.length() != 0) {
            String charSequence = this.textCode.getText().toString();
            if (charSequence.length() - 1 >= 0) {
                this.textCode.setText(charSequence.substring(0, charSequence.length() - 1));
                this.mPinEnteredCount--;
                this.mPin = this.mPin.substring(0, this.mPin.length() - 1);
            }
        }
    }

    private void removeAllDigits() {
        this.textCode.setText(EMPTY_STRING);
        this.mPin = EMPTY_STRING;
        this.mPinEnteredCount = 0;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        pinListener = null;
    }

    public void setPinListener(PinListener pinListener) {
        this.pinListener = pinListener;
    }

    public interface PinListener {
        void onSuccess(int mode);

        void onAttemptExceed(int mode);

        void onDataNotFound();
    }

    private boolean isValidPin(String pin) {
        String salt = MySettings.getPinSalt(getActivity());
        MyDevice.PinHash pinHash = MyDevice.pinToHash(pin, salt);
        String originalPinHash = MySettings.getPinCode(getActivity());
        if (originalPinHash.equals(pinHash.getHash()))
            return true;
        else
            return false;
    }

    private void setNewPin(String pin) {
        MyDevice.PinHash pinHash = MyDevice.pinToHash(pin, "");
        MySettings.setPinCode(getActivity(), pinHash.getHash());
        MySettings.setPinSalt(getActivity(), pinHash.getSalt());
    }
}
