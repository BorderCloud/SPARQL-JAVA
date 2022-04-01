package com.bordercloud.sparql.authorization;

public enum AuthorizationType {

    noAuth("No Auth" ),
    basicAuth("Basic Auth" ),
    oAuth2( "OAuth2");

    private String authorizationtype = "";
    //Constructeur
    AuthorizationType(String name){
        this.authorizationtype = name;
    }


    public String getAuthorizationType() {
        return authorizationtype;
    }

    public String toString(){
        return authorizationtype;
    }
}