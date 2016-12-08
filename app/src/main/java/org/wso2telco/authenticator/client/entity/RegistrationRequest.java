package org.wso2telco.authenticator.client.entity;

/**
 * Created by lakini on 12/5/16.
 */

public class RegistrationRequest {

    private String clientDeviceID;
    private String platform;
    private String pushToken;

    public RegistrationRequest(String clientDeviceID, String platform, String pushToken) {
        this.clientDeviceID = clientDeviceID;
        this.platform = platform;
        this.pushToken = pushToken;
    }
}
