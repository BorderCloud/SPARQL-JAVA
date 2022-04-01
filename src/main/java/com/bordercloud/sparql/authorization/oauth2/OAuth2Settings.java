package com.bordercloud.sparql.authorization.oauth2;

import com.bordercloud.sparql.SparqlClient;
import com.bordercloud.sparql.SparqlClientException;
import com.bordercloud.sparql.authorization.AuthorizationSettings;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

public abstract class OAuth2Settings extends AuthorizationSettings {
    private int maxTryRefreshToken = 5;
    public String token;
    public OAuth2AddTokenInRequestType tokenInRequestType;
    public OAuth2AccessTokenRequestType accessTokenRequestType;

    public String typeCharset = "UTF-8";
    public String accessTokenURL;
    public String headerPrefix = "Bearer";
    
    abstract public void refreshToken(SparqlClient client)
            throws SparqlClientException ;

    protected void decrementCounterRequestAccessToken(SparqlClient client) throws SparqlClientException {
        maxTryRefreshToken--;
        if(maxTryRefreshToken == 0){
            throw new SparqlClientException(
                    client,
                    "Refreshed tokens are invalid");
        }
    }
    
    public String getToken(SparqlClient client)
            throws SparqlClientException{
        if(token == null || token.isEmpty()){
            refreshToken(client);
        }
        return token;
    }
    
    public static boolean isInvalidToken(HttpResponse response){
        if ( response.getStatusLine().getStatusCode() == 401 ) {
            Header[] headerAuth = response.getHeaders("WWW-Authenticate");
            if(headerAuth.length > 0 && headerAuth[0].toString().contains("invalid_token")) {
                return true;
            }
        }
        return false;
    }
}
