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

    public static String scope = "openid";
    public static String login_hint = "910773524111";
    private static String clientId = "b2IDaQ8FaVWUTg3a1O3dlt7ToB0a";
    private static String callbackUrl = "http://jenkins.wso2telco.com:9763/playground2/oauth2.jsp";
    private static String hostUrl1 = "https://sandbox.gateway.wso2telco.com";
    private static String hostUrl2 = "https://sandbox.mconnect.wso2telco.com";

//    private static String openidEndpoint =
//        hostUrl1+"/authorizemnv/v1/spark/oauth2/authorize?nonce=565qwe&state=123\n" +
//        "sdsd&login_hint="+login_hint+"&client_id="+clientId+"&response_type=code&sco\n" +
//        "pe=mnv&redirect_uri="+callbackUrl+"&a\n" +
//        "cr_values=2&state=state_33945636-d3b7-4b12-b7b6-288e5a9683a7&nonce=nonce_a75674c9-2007-4e36-\n" +
//        "afee-ccf7c865a25d";

    private static String openidEndpoint =
            hostUrl1+"/authorize/v1/spark/oauth2/authorize?scope="+scope+"&response_type=code" +
            "&redirect_uri="+callbackUrl+"&client_id="+clientId+
                    "&acr_values=2&state=stateNjgwZjRhMzdlO&nonce=nonceOWFlMTE5NjUyN";

    private static String authorizationUrl = "Bearer " +
            "YjJJRGFROEZhVldVVGczYTFPM2RsdDdUb0IwYTpLX0N1d0ZCbnJXVm1YX0F0YlBjcm5mazJrbVVh";
    private static String userInfoEndpoint = hostUrl2+"/oauth2/userinfo?schema=openid";
    private static String tokenRequestEndpoint =
            hostUrl2+"/oauth2/token?redirect_uri="+callbackUrl+"&grant_type=authorization_code" +
                    "&code=";
    private static String updatestatusUrl =
            hostUrl2+"/SessionUpdater/tnspoints/endpoint/saa/status";

    public static String getOpenidEndpoint() {
            return openidEndpoint;
    }

    public static String getOautherizationHeaderValue() {
            return authorizationUrl;
    }

    public static String getupdatestatusUrl() {
        return updatestatusUrl;
    }

    public static String getTokenEndpoint() {
            return tokenRequestEndpoint;
    }

    public static String getUserInfoEndpoint() {
            return userInfoEndpoint;
    }
}
