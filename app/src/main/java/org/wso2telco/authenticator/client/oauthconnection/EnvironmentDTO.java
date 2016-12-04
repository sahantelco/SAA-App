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

package org.wso2telco.authenticator.client.oauthconnection;

/**
 * This class contains the details to get OAuth Token from the mobile application.
 */
public class EnvironmentDTO {
    final static String sandboxString = "sandbox";
    public static String environmnet = "sandbox";
    public static String scope = "openid";
    public static String login_hint = "910773524111";
    private static String sandbox_client_id = "b2IDaQ8FaVWUTg3a1O3dlt7ToB0a";
    private static String sandbox_callback_url = "https://mconnect.ncell.wso2telco.com:9763/playground2/oauth2.jsp";
    private static String sandbox_openid_endpoint = "http://sandbox.gateway.wso2telco.com/authorizemnv/v1/spark/oauth2/authorize?nonce=565qwe&state=123\n" +
            "sdsd&login_hint=910773524111&client_id=b2IDaQ8FaVWUTg3a1O3dlt7ToB0a&response_type=code&sco\n" +
            "pe=mnv&redirect_uri=http%3A%2F%2Fjenkins.wso2telco.com%3A9763%2Fplayground2%2Foauth2.jsp&a\n" +
            "cr_values=2&state=state_33945636-d3b7-4b12-b7b6-288e5a9683a7&nonce=nonce_a75674c9-2007-4e36-\n" +
            "afee-ccf7c865a25d&login_hint=910773524111";
    private static String sandbox_authorization = "Bearer YjJJRGFROEZhVldVVGczYTFPM2RsdDdUb0IwYTpLX0N1d0ZCbnJXVm1YX0F0YlBjcm5mazJrbVVh";
    private static String sandbox_userInfoEndpoint = "https://sandbox.mconnect.wso2telco.com/oauth2/userinfo?schema=openid";
    private static String sandbox_tokenEndpoint = "https://sandbox.mconnect.wso2telco.com/oauth2/token?redirect_uri=http://jenkins.wso2telco.com:9763/playground2/oauth2.jsp&grant_type=authorization_code&code=";

    public static String getClientID() {
        if (environmnet.equals(sandboxString)) {
            return sandbox_client_id;
        }
        return null;
    }

    public static String getOpenidEndpoint() {
        if (environmnet.equals(sandboxString)) {
            return sandbox_openid_endpoint;
        }
        return null;
    }

    public static String getCallBackUrl() {
        if (environmnet.equals(sandboxString)) {
            return sandbox_callback_url;
        }
        return null;
    }

    public static String getAOutherizationHeaderValue() {
        if (environmnet.equals(sandboxString)) {
            return sandbox_authorization;
        }
        return null;
    }

    public static String getTokenEndpoint() {
        if (environmnet.equals(sandboxString)) {
            return sandbox_tokenEndpoint;
        }
        return null;
    }

    public static String getUserInfoEndpoint() {
        if (environmnet.equals(sandboxString)) {
            return sandbox_userInfoEndpoint;
        }
        return null;
    }
}
