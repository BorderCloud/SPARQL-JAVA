package com.bordercloud.sparql.authorization.basic;

import com.bordercloud.sparql.authorization.AuthorizationSettings;

public class HTTPBasicAuthSettings extends AuthorizationSettings {
    private String login;
    private String password;

    public void setLogin(String username) {
        this.login = username;
    }
    public String getLogin() {
        return this.login;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        return this.password;
    }
}
