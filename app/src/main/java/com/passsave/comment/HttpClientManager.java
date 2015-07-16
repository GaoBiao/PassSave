package com.passsave.comment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class HttpClientManager extends Thread {
    private String url;
    private String data;
    private String method = "GET";
    private Handler handler;
    public final static int COMPLETE = 0;
    public final static int ERROR = 1;

    public interface HttpClientListener {
        void onSuccess(String result);

        void onComplete(int status);
    }

    public HttpClientManager(String url, String data) {
        this.url = url;
        this.data = data;
    }

    public HttpClientManager(String url, String data, String method) {
        this.url = url;
        this.data = data;
        this.method = method;
    }

    @Override
    public synchronized void run() {
        String result = null;
        URL _url = null;
        HttpURLConnection connection = null;
        InputStreamReader in = null;
        Bundle bundle = new Bundle();
        try {
            _url = new URL(url);
            connection = (HttpURLConnection) _url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod(this.method);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Charset", "utf-8");
            DataOutputStream dop = new DataOutputStream(connection.getOutputStream());
            dop.writeBytes(data);
            dop.flush();
            dop.close();
            in = new InputStreamReader(connection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(in);
            StringBuffer strBuffer = new StringBuffer();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                strBuffer.append(line);
            }
            result = strBuffer.toString();
            int status = connection.getResponseCode();
            bundle.putInt("status", status);
            if (handler != null) {
                Message msg = new Message();
                msg.what = COMPLETE;
                bundle.putString("result", result);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (handler != null) {
                Message msg = new Message();
                msg.what = ERROR;
                bundle.putString("message", e.getMessage());
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
