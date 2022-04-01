package com.bordercloud.sparql;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.bordercloud.sparql.authorization.AuthorizationSettings;
import com.bordercloud.sparql.authorization.NoAuthSettings;
import com.bordercloud.sparql.authorization.basic.HTTPBasicAuthSettings;
import com.bordercloud.sparql.authorization.oauth2.OAuth2ClientCredentielsSettings;
import com.bordercloud.sparql.authorization.oauth2.OAuth2PasswordGrantSettings;
import com.bordercloud.sparql.authorization.oauth2.OAuth2Settings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Karima Rafes.
 */
public final class SparqlClient {

    private SparqlResult _result = null;

    private String _lastQuery;
    private HttpUriRequest _lastHTTPRequest = null;
    private CloseableHttpResponse _lastHTTPResponse = null;

    private String _userAgent = "BorderCloud/Sparql-JAVA 1";

    /**
     * URL of Endpoint to read
     */
    private URI _endpointRead = null;

    /**
     * URL of Endpoint to write
     */
    private URI _endpointWrite = null;

    /**
     * in the constructor set debug to true in order to get usefull output
     */
    private boolean _debug = false;

    /**
     * in the constructor set the proxy_host if necessary
     */
    private String _proxyHost = null;

    /**
     * in the constructor set the proxy_port if necessary
     */
    private int _proxyPort = 0;

    /**
     * Parser of XML result
     */
    private ParserSparqlResultHandler _parserSparqlResult = null;

    /**
     * Name of parameter HTTP to send a query Sparql to read data.
     */
    private String _nameParameterQueryRead = null;

    /**
     * Name of parameter HTTP to send a query Sparql to write data.
     */
    private String _nameParameterQueryWrite = null;

    /**
     * Method HTTP to send a query Sparql to read data.
     */
    private Method _methodHTTPRead = null;

    private Method _methodHTTPWrite = null;

//    public String _login = null;
//
//    public String _password = null;

    private AuthorizationSettings _authorizationSettings;

    private SAXParser _parser;
    private DefaultHandler _handler;
    //private String _response;

    //todo
    public Object _lastError = null;

    /**
     * Constructor of SparqlClient
     *
     */
    public SparqlClient()
    {
        super();
        init(
            false,
            null,
            0
        );
    }

    /**
     * Constructor of SparqlClient
     *
     * @param debug boolean
     *            : false by default, set debug to true in order to get useful output
     */
    public SparqlClient(boolean debug)
    {
        super();
        init(
                debug,
            null,
            0
        );
    }

    /**
     * Constructor of SparqlClient
     *
     * @param debug boolean 
     *            : false by default, set debug to true in order to get useful output
     * @param proxyHost String 
     *            : null by default, IP of your proxy
     * @param proxyPort int 
     *            : null by default, port of your proxy
     */
    public SparqlClient(
            boolean debug,
            String proxyHost, //todo
            int proxyPort//todo
    )
    {
        super();
        init(
             debug,
                proxyHost,
                proxyPort
        );
    }

    private void init(
            boolean debug,
            String proxyHost, //todo
            int proxyPort //todo
    ) {
        this._debug = debug;
        this._proxyHost = proxyHost;
        this._proxyPort = proxyPort;

        this._methodHTTPRead = Method.POST;
        this._methodHTTPWrite = Method.POST;
        this._nameParameterQueryRead = "query";
        this._nameParameterQueryWrite = "update";
        
        this._authorizationSettings = new NoAuthSettings();
        
        // init parser
        this._parserSparqlResult = new ParserSparqlResultHandler();

        this._lastError = "";

        // Init Sax class
        SAXParserFactory parserSparql = SAXParserFactory.newInstance();
        _parser = null;

        try {
            _parser = parserSparql.newSAXParser();
        }
        catch (ParserConfigurationException e) {
            //todo
            e.printStackTrace();
        }
        catch (SAXException e) {
            //todo
            e.printStackTrace();
        }

        _handler = new ParserSparqlResultHandler();
        
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        if(_debug) {
            java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
            java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);
            System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
            System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
        }
    }
    
    public String getProxyHost() {
        return _proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        _proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return _proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        _proxyPort = proxyPort;
    }

    public void setMethodHTTPRead(Method method) {
        this._methodHTTPRead = method;
    }

    public Method getMethodHTTPRead() {
        return this._methodHTTPRead;
    }

    public void setMethodHTTPWrite(Method method) {
        this._methodHTTPWrite = method;
    }

    public Method getMethodHTTPWrite() {
        return this._methodHTTPWrite;
    }

    public void setEndpointRead(URI uri) {
        // FIX for Wikidata
        if (uri.toString().equalsIgnoreCase("https://query.wikidata.org/sparql")) {
            this._methodHTTPRead = Method.GET;
        }
        this._endpointRead = uri;
    }

    public URI getEndpointRead() {
        return this._endpointRead;
    }

    public void setEndpointWrite(URI uri) {
        this._endpointWrite = uri;
    }

    public URI getEndpointWrite() {
        return this._endpointWrite;
    }

    public void setNameParameterQueryWrite(String name) {
        this._nameParameterQueryWrite = name;
    }

    public String getNameParameterQueryWrite() {
        return this._nameParameterQueryWrite;
    }

    public void setNameParameterQueryRead(String name) {
        this._nameParameterQueryRead = name;
    }

    public String getNameParameterQueryRead() {
        return this._nameParameterQueryRead;
    }

    public void setAuthorizationSettings(AuthorizationSettings settings) {
        this._authorizationSettings = settings;
    }

    public AuthorizationSettings getAuthorizationSettings() {
        return this._authorizationSettings ;
    }

    public void setUserAgent(String userAgent) {
        this._userAgent = userAgent;
    }

    public String getUserAgent() {
        return this._userAgent;
    }

    /**
     * Check if the Sparql endpoint for reading is up.
     * 
     * @return bool true if the service is up.
     */
    public boolean checkEndpointRead()
    {
        return Network.ping(_endpointRead) != - 1;
    }

    /**
     * Check if the Sparql endpoint for writing is up.
     *
     * @return bool true if the service is up.
     */
    public boolean checkEndpointWrite()
    {
        return Network.ping(_endpointWrite) != - 1;
    }

    public String getLastSparqlQuery() {
        return _lastQuery;
    }

    public HttpUriRequest getLastHTTPRequest() {
        return _lastHTTPRequest;
    }

    public HttpResponse getLastHTTPResponse() {
        return _lastHTTPResponse;
    }

    public SparqlResult getLastResult() {
        return _result;
    }
    
    public SparqlResult query(String query) throws SparqlClientException {
        return query(query, MimeType.xml, "UTF-8");
    }

    public SparqlResult query(String query, MimeType typeOutput) throws SparqlClientException {
        return query(query, typeOutput, "UTF-8");
    }

    public SparqlResult query(String q, MimeType typeOutput, String typeCharset) throws SparqlClientException {
        _lastQuery = q;
        long t1 = System.currentTimeMillis();
        SparqlQueryType type = findType(q);
        switch (type){
            case UPDATE:
                _result = this.queryUpdate(q, typeOutput, typeCharset);
                break;
            case SELECT:
            case CONSTRUCT:
            case ASK:
            case DESCRIBE:
                _result = this.queryRead(q, typeOutput, typeCharset);
                break;
        }

        //parse if possible
        _result.queryType = type;
        switch (type){
            case UPDATE:
                //do nothing
                break;
            case SELECT:
                switch (_result.accept){
                    case xml:
                        _result.resultHashMap = getResultXml(_result.resultRaw);
                        break;
                    case json:
                        _result.resultHashMap = getResultJson(_result.resultRaw);
                        break;
                    default:
                        //do nothing
                }
                break;
            case CONSTRUCT:
                //do nothing
                break;
            case ASK:
                switch (_result.accept){
                    case xml:
                        _result.resultHashMap = getResultXml(_result.resultRaw);
                        break;
                    case json:
                        _result.resultHashMap = getResultJson(_result.resultRaw);
                        break;
                    default:
                        //do nothing
                }
                break;
            case DESCRIBE:
                //do nothing
                break;
            default:
        }
        return _result;
    }

    private static Pattern PATTERN_UPDATE = Pattern.compile("(INSERT|DELETE|CLEAR|LOAD)",Pattern.CASE_INSENSITIVE);
    private static Pattern PATTERN_CONSTRUCT = Pattern.compile("CONSTRUCT",Pattern.CASE_INSENSITIVE);
    private static Pattern PATTERN_ASK = Pattern.compile("ASK",Pattern.CASE_INSENSITIVE);
    private static Pattern PATTERN_DESCRIBE = Pattern.compile("DESCRIBE",Pattern.CASE_INSENSITIVE);
    private static SparqlQueryType findType(String q){
        SparqlQueryType result = SparqlQueryType.SELECT;
        if(PATTERN_UPDATE.matcher(q).find()){
            result = SparqlQueryType.UPDATE;
        }else if(PATTERN_CONSTRUCT.matcher(q).find()){
            result = SparqlQueryType.CONSTRUCT;
        }else if(PATTERN_ASK.matcher(q).find()){
            result = SparqlQueryType.ASK;
        }else if(PATTERN_DESCRIBE.matcher(q).find()){
            result = SparqlQueryType.DESCRIBE;
        }
        return result;
    }

    private HashMap<String, Object> getResultJson(String jsonString) throws SparqlClientException {
        try {
            return new ObjectMapper().readValue(jsonString, new TypeReference<HashMap<String, Object>>(){});
        } catch (JsonProcessingException e) {
            throw new SparqlClientException(
                    this,
                    e.getMessage());
        }
    }

    private  HashMap<String, Object> getResultXml(String response) throws SparqlClientException {
        //parse the message
        _handler = new ParserSparqlResultHandler();

        try {
            _parser.parse(new InputSource(new StringReader(response)), _handler);
        }
        catch (SAXException e) {
            throw new SparqlClientException(this,e.getMessage());
        }
        catch (IOException e) {
            throw new SparqlClientException(this,e.getMessage());
        }

        if (_handler != null) {
            return ((ParserSparqlResultHandler) _handler).getResult();//new HashMap<String, HashMap>();
        } else {
            return null;
        }
    }
    
    public SparqlResult queryRead(String query, MimeType typeOutput, String typeCharset) throws SparqlClientException {
        if (_endpointRead != null) {
            if (_methodHTTPRead == Method.POST) {
                return sendQueryPOST(_endpointRead, _nameParameterQueryRead, query,typeOutput, typeCharset, _authorizationSettings);
            } else {
                return sendQueryGET(_endpointRead, _nameParameterQueryRead, query,typeOutput, typeCharset, _authorizationSettings);
            }
        }else{
            throw new SparqlClientException(this,"The endpoint for reading is not defined.");
        }
    }

    public SparqlResult queryUpdate(String query, MimeType typeOutput, String typeCharset) throws SparqlClientException {
        if (_endpointWrite != null) {
            if (_methodHTTPWrite == Method.POST) {
                return sendQueryPOST(_endpointWrite, _nameParameterQueryWrite, query, typeOutput, typeCharset, _authorizationSettings);
            } else {
                return sendQueryGET(_endpointWrite, _nameParameterQueryWrite, query, typeOutput, typeCharset, _authorizationSettings);
            }
        }else{
            throw new SparqlClientException(this,"The endpoint for writing is not defined.");
        }
    }

    private SparqlResult sendQueryGET(
        URI endpoint,
        String nameParameter,
        String query,
        MimeType typeOutput,
        String typeCharset,
        AuthorizationSettings settings)
            throws SparqlClientException {
        SparqlResult result = new SparqlResult();
        if( settings instanceof NoAuthSettings || settings instanceof HTTPBasicAuthSettings) {
            result = sendQueryGETNoAuthAndHTTPBasicAuth(endpoint, nameParameter, query, typeOutput, typeCharset, settings );
        } else if( settings instanceof OAuth2ClientCredentielsSettings || settings instanceof OAuth2PasswordGrantSettings) {
            result = sendQueryGET(endpoint, nameParameter, query, typeOutput, typeCharset, (OAuth2Settings) settings );
//        } else if( settings instanceof OAuthPasswordGrantSettings) {
//            result = sendQueryGET(endpoint, nameParameter, query, typeOutput, typeCharset, (OAuthPasswordGrantSettings) settings );
        } else {
            throw new SparqlClientException(this,"This authorization type is not implemented.");
        }
        return result;
    }
    private SparqlResult sendQueryPOST(
            URI endpoint,
            String nameParameter,
            String query,
            MimeType typeOutput,
            String typeCharset,
            AuthorizationSettings settings)
            throws SparqlClientException {
        SparqlResult result = new SparqlResult();
        if( settings instanceof NoAuthSettings ) {
            result = sendQueryPOST(endpoint, nameParameter, query, typeOutput, typeCharset, (NoAuthSettings) settings );
        } else if( settings instanceof HTTPBasicAuthSettings) {
            result = sendQueryPOST(endpoint, nameParameter, query, typeOutput, typeCharset, (HTTPBasicAuthSettings) settings );
        } else if( settings instanceof OAuth2ClientCredentielsSettings || settings instanceof OAuth2PasswordGrantSettings) {
            result = sendQueryPOST(endpoint, nameParameter, query, typeOutput, typeCharset, (OAuth2ClientCredentielsSettings) settings );
//        } else if( settings instanceof OAuthPasswordGrantSettings) {
//            result = sendQueryPOST(endpoint, nameParameter, query, typeOutput, typeCharset, (OAuthPasswordGrantSettings) settings );
        } else {
            throw new SparqlClientException(this,"This authorization type is not implemented.");
        }
        return result;
    }
    
    //region sendQueryGET
    private SparqlResult sendQueryGETNoAuthAndHTTPBasicAuth(
            URI endpoint,
            String nameParameter,
            String query,
            MimeType typeOutput,
            String typeCharset,
            AuthorizationSettings settings)
            throws SparqlClientException{
        SparqlResult result = new SparqlResult();
        try {
            final URIBuilder uriBuilder = new URIBuilder(endpoint);
            uriBuilder.setCharset(Charset.forName(typeCharset));
            uriBuilder.addParameter(nameParameter,query);
            if (settings instanceof HTTPBasicAuthSettings) {
                uriBuilder.setUserInfo(((HTTPBasicAuthSettings) settings).getLogin(),((HTTPBasicAuthSettings) settings).getPassword());
            }

            URI url = uriBuilder.build();

            CloseableHttpClient httpclient = Network.getCloseableHttpClient(null);

            try {
                HttpGet httpget = new HttpGet(url);
                buildHTTPHeader(httpget, typeOutput, typeCharset,null, null);
                _lastHTTPRequest = httpget;

                //System.out.println("Executing request " + httpget.getRequestLine());
                _lastHTTPResponse = httpclient.execute(_lastHTTPRequest);
                try {
                    int statusCode = _lastHTTPResponse.getStatusLine().getStatusCode();

                    //System.out.println("----------------------------------------");
                    //System.out.println(response.getStatusLine());
                    if ( statusCode != 204) { //no content
                        HttpEntity entity = _lastHTTPResponse.getEntity();
                        result.resultRaw = EntityUtils.toString(entity,typeCharset);
                    }
                    //EntityUtils.consume(entity);
                    result.accept = typeOutput;

                    if ( statusCode < 200 || statusCode >= 300) {
                        _result = result;
                        throw new SparqlClientException(this,
                                _lastHTTPResponse.getStatusLine().toString()
                        );
                    }
                }
                finally {
                    _lastHTTPResponse.close();
                }
            }
            finally {
                httpclient.close();
            }
        }
        catch (IOException | URISyntaxException e) {
//        System.out.println(e.getMessage());
//        e.printStackTrace();
            _result = result;
            throw new SparqlClientException(
                    this,
                    e.getMessage());
        }
        return result;
    }
    
    private SparqlResult sendQueryGET(
            URI endpoint,
            String nameParameter,
            String query,
            MimeType typeOutput,
            String typeCharset,
            OAuth2Settings settings)
            throws SparqlClientException{
        SparqlResult result = new SparqlResult();
        
        try {
            final String token = settings.getToken(this);
            final URIBuilder uriBuilder = new URIBuilder(endpoint);
            uriBuilder.setCharset(Charset.forName(typeCharset));
            uriBuilder.addParameter(nameParameter,query);

            URI url = uriBuilder.build();
            CloseableHttpClient httpclient = Network.getCloseableHttpClient(null);

            try {
                HttpGet httpget = new HttpGet(url);

                buildHTTPHeader(httpget, typeOutput, typeCharset,settings.headerPrefix, token);
                _lastHTTPRequest = httpget;

                //System.out.println("Executing request " + httpget.getRequestLine());
                _lastHTTPResponse = httpclient.execute(_lastHTTPRequest);
                try {
                    int statusCode = _lastHTTPResponse.getStatusLine().getStatusCode();

                    //System.out.println("----------------------------------------");
                    //System.out.println(response.getStatusLine());
                    if ( statusCode != 204) { //no content
                        HttpEntity entity = _lastHTTPResponse.getEntity();
                        result.resultRaw = EntityUtils.toString(entity,typeCharset);
                    }
                    //EntityUtils.consume(entity);
                    result.accept = typeOutput;

                    if ( statusCode < 200 || statusCode >= 300) {
                        _result = result;
                        if ( OAuth2Settings.isInvalidToken(_lastHTTPResponse)) {
                            settings.refreshToken(this);
                            return sendQueryGET(
                                    endpoint,
                                    nameParameter,
                                    query,
                                    typeOutput,
                                    typeCharset,
                                    settings);
                        }
                        throw new SparqlClientException(this,
                                _lastHTTPResponse.getStatusLine().toString()
                        );
                    }
                }
                finally {
                    _lastHTTPResponse.close();
                }
            }
            finally {
                httpclient.close();
            }
        }
        catch (IOException | URISyntaxException e) {
//        System.out.println(e.getMessage());
//        e.printStackTrace();
            _result = result;
            throw new SparqlClientException(
                    this,
                    e.getMessage());
        }
        return result;
    }

    //endregion
    // region sendQueryPOST

    private SparqlResult sendQueryPOST(
            URI endpoint,
            String nameParameter,
            String query,
            MimeType typeOutput,
            String typeCharset,
            HTTPBasicAuthSettings settings)
            throws SparqlClientException
    {
        SparqlResult result = new SparqlResult();
        try {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(settings.getLogin(), settings.getPassword()));
            CloseableHttpClient httpclient = Network.getCloseableHttpClient(credsProvider);
            try {
                HttpPost httpPost = new HttpPost(endpoint);
                buildHTTPHeader(httpPost, typeOutput, typeCharset,null, null);
                List <NameValuePair> nvps = new ArrayList <NameValuePair>();
                nvps.add(new BasicNameValuePair(nameParameter, query));
                httpPost.setEntity(new UrlEncodedFormEntity(nvps,typeCharset));
                _lastHTTPRequest = httpPost;

                _lastHTTPResponse = httpclient.execute(_lastHTTPRequest);
                try {
                    int statusCode = _lastHTTPResponse.getStatusLine().getStatusCode();
                    if ( statusCode != 204) { //no content
                        HttpEntity entity = _lastHTTPResponse.getEntity();
                        result.resultRaw = EntityUtils.toString(entity,typeCharset);
                    }
                    result.accept = typeOutput;

                    if ( statusCode < 200 || statusCode >= 300) {
                        _result = result;
                        throw new SparqlClientException(this,
                                _lastHTTPResponse.getStatusLine().toString()
                        );
                    }
                }
                finally {
                    _lastHTTPResponse.close();
                }
            }
            finally {
                httpclient.close();
            }
        }
        catch (IOException e) {
//        System.out.println(e.getMessage());
//        e.printStackTrace();
            _result = result;
            throw new SparqlClientException(
                    this,
                    e.getMessage());
        }

        return result;
    }

    private SparqlResult sendQueryPOST(
            URI endpoint,
            String nameParameter,
            String query,
            MimeType typeOutput,
            String typeCharset,
            NoAuthSettings settings)
            throws SparqlClientException
    {
        SparqlResult result = new SparqlResult();
        try {
            CloseableHttpClient httpclient = Network.getCloseableHttpClient(null);
            try {
                HttpPost httpPost = new HttpPost(endpoint);
                buildHTTPHeader(httpPost, typeOutput, typeCharset,null, null);
                List <NameValuePair> nvps = new ArrayList <NameValuePair>();
                nvps.add(new BasicNameValuePair(nameParameter, query));
                httpPost.setEntity(new UrlEncodedFormEntity(nvps,typeCharset));
                _lastHTTPRequest = httpPost;

                _lastHTTPResponse = httpclient.execute(_lastHTTPRequest);
                try {
                    int statusCode = _lastHTTPResponse.getStatusLine().getStatusCode();
                    if ( statusCode != 204) { //no content
                        HttpEntity entity = _lastHTTPResponse.getEntity();
                        result.resultRaw = EntityUtils.toString(entity,typeCharset);
                    }
                    result.accept = typeOutput;

                    if ( statusCode < 200 || statusCode >= 300) {
                        _result = result;
                        throw new SparqlClientException(this,
                                _lastHTTPResponse.getStatusLine().toString()
                        );
                    }
                }
                finally {
                    _lastHTTPResponse.close();
                }
            }
            finally {
                httpclient.close();
            }
        }
        catch (IOException e) {
            _result = result;
            throw new SparqlClientException(
                    this,
                    e.getMessage());
        }
        return result;
    }


    private SparqlResult sendQueryPOST(
            URI endpoint,
            String nameParameter,
            String query,
            MimeType typeOutput,
            String typeCharset,
            OAuth2Settings settings
            )
            throws SparqlClientException
    {
        SparqlResult result = new SparqlResult();
        try {
            CloseableHttpClient httpclient = Network.getCloseableHttpClient(null);
            try {
                HttpPost httpPost = new HttpPost(endpoint);
                String token = settings.getToken(this);
                buildHTTPHeader(httpPost, typeOutput, typeCharset,settings.headerPrefix, token);
                List <NameValuePair> nvps = new ArrayList <NameValuePair>();
                nvps.add(new BasicNameValuePair(nameParameter, query));
                httpPost.setEntity(new UrlEncodedFormEntity(nvps,typeCharset));
                _lastHTTPRequest = httpPost;

                _lastHTTPResponse = httpclient.execute(_lastHTTPRequest);
                try {
                    int statusCode = _lastHTTPResponse.getStatusLine().getStatusCode();
                    if ( statusCode != 204) { //no content
                        HttpEntity entity = _lastHTTPResponse.getEntity();
                        result.resultRaw = EntityUtils.toString(entity,typeCharset);
                    }
                    result.accept = typeOutput;

                    if ( statusCode < 200 || statusCode >= 300) {
                        _result = result;
                        if ( OAuth2Settings.isInvalidToken(_lastHTTPResponse)) {
                                settings.refreshToken(this);
                                return sendQueryPOST(
                                        endpoint,
                                         nameParameter,
                                         query,
                                         typeOutput,
                                         typeCharset,
                                         settings
                                );
                        }
                        throw new SparqlClientException(this,
                                _lastHTTPResponse.getStatusLine().toString()
                        );
                    }
                }
                finally {
                    _lastHTTPResponse.close();
                }
            }
            finally {
                httpclient.close();
            }
        }
        catch (IOException e) {
            _result = result;
            throw new SparqlClientException(
                    this,
                    e.getMessage());
        }
        return result;
    }
    
    private void buildHTTPHeader(HttpRequestBase httpBase, MimeType typeOutput, String typeCharset, String headerPrefixToken, String token) {
        httpBase.setHeader("Accept", typeOutput.getMimetype());
        httpBase.setHeader("Accept-Charset", typeCharset);
        httpBase.setHeader("User-Agent", _userAgent);
        if(token != null) {
            httpBase.setHeader("Authorization", headerPrefixToken + " " + token);
        }
    }

    //endregion

    public void printLastQueryAndResult() {
        SparqlClient.printLastQueryAndResult(this);
    }

    public static void printLastQueryAndResult(SparqlClient c ){
        System.out.println("");
        System.out.println("Endpoint Read : " + c.getEndpointRead());
        System.out.println("Endpoint Write : " + c.getEndpointRead());
        System.out.println("Query : ");
        System.out.println(c.getLastSparqlQuery());
        System.out.println("");
        System.out.println("Result : ");
        SparqlQueryType type = findType(c.getLastSparqlQuery());
        switch (type){
            case UPDATE:
                System.out.println("Result : "+ c._result.resultRaw);
                break;
            case SELECT:
            case CONSTRUCT:
            case ASK:
            case DESCRIBE:
                printSparqlResult(c);
                break;
        }
    }

    private static void printSparqlResult(SparqlClient c ){
        switch (c.getLastResult().accept){
            case xml:
                SparqlResultModel srmx = new SparqlResultModelWithXML(c.getLastResult().resultHashMap);
                SparqlResultModel.printResult(srmx,  30);
                break;
            case json:
                SparqlResultModel srmj = new SparqlResultModelWithJSON(c.getLastResult().resultHashMap);
                SparqlResultModel.printResult(srmj,  30);
                break;
            default:
                System.out.println(c.getLastResult().resultRaw);
        }
    }
}
