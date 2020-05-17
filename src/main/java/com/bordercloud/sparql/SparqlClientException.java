package com.bordercloud.sparql;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SparqlClientException extends Exception {
    SparqlClient _sparqlClient;

    public SparqlClientException(
            SparqlClient sparqlClient,
            String message
    ) {
        super(message);
        _sparqlClient = sparqlClient;
    }
    
    public String getMessage()
    {
        String message = "";
        String response = "";

        int status;
        SparqlResult result;

        if(_sparqlClient.getLastHTTPRequest() !=null && _sparqlClient.getLastHTTPResponse() !=null){
            status = _sparqlClient.getLastHTTPResponse().getStatusLine().getStatusCode();
            result = _sparqlClient.getLastResult();
            if (result != null && result.resultRaw != null) {
                response = result.resultRaw;
                Matcher matcher = Pattern.compile("MalformedQueryException: *(.*)", Pattern.CASE_INSENSITIVE).matcher(response);
                if (matcher.find()) {
                    message = "ERROR QUERY: " + matcher.group(1);
                }
            }

            if(message.isEmpty()) {
                if (status == 200 && result != null) {
                    message = result.resultRaw;
                } else {
                    message = "ERROR QUERY: " + _sparqlClient.getLastSparqlQuery() + "\n" +
                              "ERROR HTTP : " + 
                            printRequestHTTP(_sparqlClient);
                }
            }
        }else{
            //return "Error query: " + _sparqlClient.getLastSparqlQuery() ;
            message = super.getMessage();
        }
        return message;
    }

    private static  String printRequestHTTP(SparqlClient sparqlClient) {
        String charset = null;
        Header charsetHeader = sparqlClient.getLastHTTPRequest().getFirstHeader("Accept-Charset");
        if(charsetHeader != null){
            charset = charsetHeader.getValue();
        }else{
            charset = "UTF-8";
        }
        if (sparqlClient.getLastHTTPRequest() instanceof HttpPost) {
            return post(sparqlClient);
        }else{
            return get(sparqlClient);
        }
    }

    private static String get(SparqlClient sparqlClient) {
        HttpGet request =  (HttpGet) sparqlClient.getLastHTTPRequest();
        HttpResponse response = sparqlClient.getLastHTTPResponse();
        String charset = null;
        Header charsetHeader = sparqlClient.getLastHTTPRequest().getFirstHeader("Accept-Charset");
        if(charsetHeader != null){
            charset = charsetHeader.getValue();
        }else{
            charset = "UTF-8";
        }

        StringBuffer str = new StringBuffer();
        final URI uri = request.getURI();
        int responseCode = response.getStatusLine().getStatusCode();
        String responsePhrase = response.getStatusLine().getReasonPhrase();

        str.append("**GET** request Url: " + uri);
        str.append(" \n\n Headers");
        for(Header header : request.getAllHeaders()){
            str.append("\n");
            str.append(header.getName() + " = " +header.getValue());
        }

        str.append("\n\nParameters");
        URIBuilder newBuilder = new URIBuilder(uri);
        List<NameValuePair> params = newBuilder.getQueryParams();
        for(NameValuePair kv : params){
            str.append("\n");
            str.append(kv.getName() + " = " + kv.getName());

            //?? todo str.append( URLDecoder.decode(par,charset));
        }

        str.append("\nResponse Code: " + responseCode + "("+ responsePhrase +")");
        str.append("\nContent:-\n");
        //str.append(EntityUtils.toString(response.getEntity(),charset));
        SparqlResult result = sparqlClient.getLastResult();
        if(result != null){
            str.append(result.resultRaw);
        }else{
            str.append("EMPTY");
        }

        return str.toString();
    }

    private static String post(SparqlClient sparqlClient) {
        HttpPost request = (HttpPost) sparqlClient.getLastHTTPRequest();
        HttpResponse response = sparqlClient.getLastHTTPResponse();
        String charset = null;
        Header charsetHeader = sparqlClient.getLastHTTPRequest().getFirstHeader("Accept-Charset");
        if(charsetHeader != null){
            charset = charsetHeader.getValue();
        }else{
            charset = "UTF-8";
        }
        StringBuffer str = new StringBuffer();
        try {
            int responseCode = response.getStatusLine().getStatusCode();
            String responsePhrase = response.getStatusLine().getReasonPhrase();

            str.append("\n**POST** request Url: " + request.getURI()+"\n");
            //str.append("Parameters : " + nameValuePairs);

            str.append("\nParameters :\n");
            String strParameters = EntityUtils.toString(request.getEntity(),charset);
            for(String par : strParameters.split("&")){
                str.append("\n");
                str.append( URLDecoder.decode(par,charset));
            }

            str.append("\nResponse Code: " + responseCode + "("+ responsePhrase +")");
            str.append("\nContent:-\n");
            //str.append(EntityUtils.toString(response.getEntity(),charset));
            SparqlResult result = sparqlClient.getLastResult();
            if(result != null){
                str.append(result.resultRaw);
            }else{
                str.append("EMPTY");
            }
        } catch (IOException e) {
        }
        return str.toString();
    }
}