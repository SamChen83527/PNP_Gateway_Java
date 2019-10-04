/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnp_gateway.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author user
 */
public class HTTPManager {
    public void HTTPManager (){        
    }
    
    public String sendGet(String url) throws Exception {
        if (url.indexOf("http://")==-1) {
            url = "http://" + url;
        }
        System.out.print("url: ");System.out.println(url);

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        String GET_resp = response.toString();

        return GET_resp;
    }
    
    public String sendPost (String url, String target, String data) throws MalformedURLException, IOException {
        url = url + "/" + target;
        if (url.indexOf("http://")==-1) {
            url = "http://" + url;
        }
        System.out.print("url: ");System.out.println(url);

        URL endpoint = new URL(url);
        HttpURLConnection httpConnection = (HttpURLConnection) endpoint.openConnection();
        httpConnection.setRequestMethod("POST");
        httpConnection.setDoInput(true);
        httpConnection.setDoOutput(true);

        // Add post data
        if(data != null && data.length() > 0) {
            httpConnection.setRequestProperty("Content-Length", String.valueOf(data.length()));
            System.out.println("\nSending 'POST' request to URL : " + url);
            DataOutputStream dos = null;
            try {
                dos = new DataOutputStream(httpConnection.getOutputStream());
                // Use utf-8 encoding for the post data.
                dos.write(data.getBytes(Charset.forName("utf-8")));
                dos.flush();
            } finally {
                if(dos != null) dos.close();
            }
        }
        
        // Read the response from server
        String location = httpConnection.getHeaderField("location");
        System.out.print("location: ");
        System.out.println(location);
        int responseCode = httpConnection.getResponseCode();
        System.out.println(responseCode);

        return location;
    }
    
    static public void sendPatch (String url, String target, String data) throws MalformedURLException, IOException {
        url = url + "/" + target;
        if (url.indexOf("http://")==-1) {
            url = "http://" + url;
        }
        System.out.print("url: ");System.out.println(url);

        try {
            StringEntity entity = new StringEntity(data, ContentType.create("application/json"));

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPatch httpPatch = new HttpPatch(url);
            httpPatch.setEntity(entity);
            CloseableHttpResponse response = httpClient.execute(httpPatch);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(HTTPManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
}
