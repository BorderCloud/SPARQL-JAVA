package com.bordercloud.sparql;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;

/**
 * Class Network gives several tools : ping, port,...
 */
public class Network {

    /**
     * Ping a service HTTP
     *
     * @param uri address
     * @return double if -1 the server is down
     */
    static double ping(URI uri)
    {
        long status = 0;
        long startTime = System.currentTimeMillis();
        if (! pingHost(uri.getHost(), uri.getPort(), 10000)) {
            // Site is down
            status = - 1;
        } else {
            long stopTime = System.currentTimeMillis();
            status = stopTime - startTime;
        }
        return status;
    }

    private static boolean pingHost(String host, int port, int timeout) {
        try{
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    public static CloseableHttpClient getCloseableHttpClient(CredentialsProvider credsProvider) {
        HttpClientBuilder httpclientBuilder = HttpClients.custom();
        if (credsProvider != null) {
            httpclientBuilder.setDefaultCredentialsProvider(credsProvider);
        }
        CloseableHttpClient httpclient =  httpclientBuilder.setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultRequestConfig(RequestConfig.custom()
                        // Waiting for a connection from connection manager
                        .setConnectionRequestTimeout(10000)
                        // Waiting for connection to establish
                        .setConnectTimeout(60000)
                        .setExpectContinueEnabled(false)
                        // Waiting for data
                        .setSocketTimeout(60000)
                        .setCookieSpec("easy")
                        .build())
                .setMaxConnPerRoute(20)
                .setMaxConnTotal(100)
                .build();
        return httpclient;
    }
}
