package com.bordercloud.sparql.authorization.oauth2;

public enum OAuth2GrantType {
    passwordGrant("Password Grant" ), // https://www.oauth.com/oauth2-servers/access-tokens/password-grant/
    clientCredentials("Client Credentials" ); // hhttps://www.oauth.com/oauth2-servers/access-tokens/client-credentials/

    private String grantType = "";
    
    //Constructeur
    OAuth2GrantType(String name){
        this.grantType = name;
    }
    
    public String getOAuth2GrantType() {
        return grantType;
    }

    public String toString(){
        return grantType;
    }
}