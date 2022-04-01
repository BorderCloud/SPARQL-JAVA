package com.bordercloud.sparql.authorization.oauth2;

import com.bordercloud.sparql.Network;
import com.bordercloud.sparql.SparqlClient;
import com.bordercloud.sparql.SparqlClientException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class OAuth2PasswordGrantSettings extends OAuth2ClientCredentielsSettings {
    private String username;
    private String password;

    public OAuth2PasswordGrantSettings(){
        tokenInRequestType = OAuth2AddTokenInRequestType.requestHeader;
        accessTokenRequestType = OAuth2AccessTokenRequestType.requestBody;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void refreshToken(SparqlClient client)
            throws SparqlClientException {
        this.decrementCounterRequestAccessToken(client);
        if (accessTokenRequestType == OAuth2AccessTokenRequestType.requestBody) {
            try {
                CloseableHttpClient httpclient = Network.getCloseableHttpClient(null);
                try {
                    HttpPost httpPost = new HttpPost(accessTokenURL);
                    httpPost.setHeader("Accept", "application/json");
                    httpPost.setHeader("Accept-Charset", typeCharset);
                    //                httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    httpPost.setHeader("User-Agent", client.getUserAgent());
                    List<NameValuePair> nvps = new ArrayList<NameValuePair>();

                    nvps.add(new BasicNameValuePair("grant_type","password"));
                    nvps.add(new BasicNameValuePair("client_id",clientID));
                    if ( clientSecret != null && ! clientSecret.isEmpty() ) {
                        nvps.add(new BasicNameValuePair("clientSecret",clientSecret));
                    }
                    nvps.add(new BasicNameValuePair("username",username));
                    nvps.add(new BasicNameValuePair("password",password));

                    httpPost.setEntity(new UrlEncodedFormEntity(nvps,typeCharset));

                    CloseableHttpResponse response = httpclient.execute(httpPost);
                    try {
                        int statusCode = response.getStatusLine().getStatusCode();
                        HttpEntity entity = response.getEntity();
                        String jsonString = EntityUtils.toString(entity,typeCharset);

                        HashMap<String, Object> resultHashMap = new ObjectMapper().readValue(jsonString, new TypeReference<HashMap<String, Object>>(){});

                        if (resultHashMap != null
                                && resultHashMap.get("token_type") != null
                                && resultHashMap.get("token_type").equals("Bearer")
                        ) {
                            token = (String) resultHashMap.get("access_token");
                        }

                        if ( statusCode < 200 || statusCode >= 300) {
                            throw new SparqlClientException(client,
                                    response.getStatusLine().toString()
                            );
                        }
                    }
                    finally {
                        response.close();
                    }
                } finally {
                    httpclient.close();
                }
            }
            catch (IOException e) {
                //        System.out.println(e.getMessage());
                //        e.printStackTrace();
                throw new SparqlClientException(
                        client,
                        e.getMessage() ,
                        e);
            }
        } else { //header
            throw new SparqlClientException(client,
                    "The option OAuth2AccessTokenRequestType.requestHeader is not implemented."
            );
        }
    }
}
