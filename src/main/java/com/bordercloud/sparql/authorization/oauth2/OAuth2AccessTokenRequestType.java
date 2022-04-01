package com.bordercloud.sparql.authorization.oauth2;

public enum OAuth2AccessTokenRequestType {
    requestBody("Request Body" ), // https://www.oauth.com/oauth2-servers/access-tokens/password-grant/
    requestHeader("Request Header" ); // hhttps://www.oauth.com/oauth2-servers/access-tokens/client-credentials/

    private String requestType = "";
    
    //Constructeur
    OAuth2AccessTokenRequestType(String name){
        this.requestType = name;
    }
    
    public String getOAuth2AddTokenInRequestType() {
        return requestType;
    }

    public String toString(){
        return requestType;
    }
}