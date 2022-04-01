package com.bordercloud.sparql.authorization;

import com.bordercloud.sparql.authorization.basic.HTTPBasicAuthSettings;
import com.bordercloud.sparql.authorization.oauth2.*;

import java.security.InvalidParameterException;

public class AuthorizationSettings {
    public static AuthorizationSettings makeAuthorizationSettings(
            AuthorizationType authorizationType,
            String basicAuthLogin,
            String basicAuthPassword,
            OAuth2GrantType oAuth2GrantType,
            String oAuth2AccessTokenURL,
            String oAuth2HeaderPrefix,
            String oAuth2Token,
            OAuth2AccessTokenRequestType oAuth2AccessTokenRequestType,
            OAuth2AddTokenInRequestType oAuthTokenInRequestType,
            String oAuth2ClientID,
            String oAuth2ClientSecret,
            String oAuth2Username,
            String oAuth2Password
    ) throws InvalidParameterException {
        AuthorizationSettings result;
        switch (authorizationType){
            case basicAuth:
                HTTPBasicAuthSettings basicAuthObj = new HTTPBasicAuthSettings();
                if(basicAuthLogin != null && ! basicAuthLogin.isEmpty()){
                    basicAuthObj.setLogin(basicAuthLogin);
                }else{
                    throw new InvalidParameterException("For basic HTTP authentication, login is required. ");
                }
                if(basicAuthPassword != null){
                    basicAuthObj.setPassword(basicAuthPassword);
                }else{
                    basicAuthObj.setPassword("");
                }
                result = basicAuthObj;
                break;
            case oAuth2:
                switch (oAuth2GrantType){
                    case clientCredentials:
                        OAuth2ClientCredentielsSettings clientCredentialsObj = new OAuth2ClientCredentielsSettings();
                        if(oAuth2ClientID != null && ! oAuth2ClientID.isEmpty()){
                            clientCredentialsObj.setClientID(oAuth2ClientID);
                        }else{
                            throw new InvalidParameterException("For OAuth2 authentication with client credentials, clientID is required. ");
                        }
                        if(oAuth2ClientSecret != null){
                            clientCredentialsObj.setClientSecret(oAuth2ClientSecret);
                        }else{
                            clientCredentialsObj.setClientSecret("");
                        }
                        result = clientCredentialsObj;
                        break;
                    case passwordGrant:
                        OAuth2PasswordGrantSettings passwordGrantObj = new OAuth2PasswordGrantSettings();
                        if(oAuth2ClientID != null && ! oAuth2ClientID.isEmpty()){
                            passwordGrantObj.setClientID(oAuth2ClientID);
                        }else{
                            throw new InvalidParameterException("For OAuth2 authentication with password credentials, clientID is required. ");
                        }
                        if(oAuth2ClientSecret != null){
                            passwordGrantObj.setClientSecret(oAuth2ClientSecret);
                        }else{
                            passwordGrantObj.setClientSecret("");
                        }

                        if(oAuth2Username != null && ! oAuth2Username.isEmpty()){
                            passwordGrantObj.setUsername(oAuth2Username);
                        }else{
                            throw new InvalidParameterException("For OAuth2 authentication with password credentials, username is required. ");
                        }
                        if(oAuth2Password != null){
                            passwordGrantObj.setPassword(oAuth2Password);
                        }else{
                            throw new InvalidParameterException("For OAuth2 authentication with password credentials, password is required. ");
                        }
                        result = passwordGrantObj;
                        break;
                    default:
                        return new NoAuthSettings();
                        //throw new Exception("The OAuth2GrantType option " + oAuth2GrantType.toString() +" is not implemented." );
                }
                if(oAuth2AccessTokenURL != null && ! oAuth2AccessTokenURL.isEmpty()){
                    ((OAuth2Settings)result).accessTokenURL = oAuth2AccessTokenURL;
                }else{
                    throw new InvalidParameterException("For OAuth2 authentication, access token URL is required. ");
                }
                ((OAuth2Settings)result).token =  oAuth2Token;
                if(oAuthTokenInRequestType != null){
                    ((OAuth2Settings)result).tokenInRequestType = oAuthTokenInRequestType ;
                }
                if(oAuth2AccessTokenRequestType != null){
                    ((OAuth2Settings)result).accessTokenRequestType = oAuth2AccessTokenRequestType ;
                }
                if(oAuth2HeaderPrefix != null && ! oAuth2HeaderPrefix.isEmpty()){
                    ((OAuth2Settings)result).headerPrefix = oAuth2HeaderPrefix ;
                }
                break;
            default:
                result = new NoAuthSettings();
        }
        return result;
    }
}
