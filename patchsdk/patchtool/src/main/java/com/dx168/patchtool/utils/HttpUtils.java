package com.dx168.patchtool.utils;

import com.dx168.patchtool.HttpCallback;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by jianjun.lin on 2016/11/30.
 */

public class HttpUtils {
    private static Executor sThreadPool = Executors.newSingleThreadExecutor();

    public static void request(final String url, final Map<String, Object> paramMap, final HttpCallback callback) {
        sThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                OutputStream outputStream = null;
                InputStream inputStream = null;
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(30 * 1000);
                    conn.setDoInput(true);
                    if (paramMap != null && !paramMap.isEmpty()) {
                        conn.setDoOutput(true);
                        StringBuilder params = new StringBuilder();
                        for (String key : paramMap.keySet()) {
                            params.append(key).append("=").append(paramMap.get(key)).append("&");
                        }
                        outputStream = conn.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                        writer.write(params.toString());
                        writer.flush();
                        writer.close();
                    }
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        inputStream = conn.getInputStream();
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int read;
                        while ((read = inputStream.read(buf)) != -1) {
                            byteArrayOutputStream.write(buf, 0, read);
                        }
                        if (callback != null) {
                            callback.onSuccess(code, byteArrayOutputStream.toByteArray());
                        }
                        byteArrayOutputStream.close();
                    } else {
                        inputStream = conn.getErrorStream();
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int read;
                        while ((read = inputStream.read(buf)) != -1) {
                            byteArrayOutputStream.write(buf, 0, read);
                        }
                        if (callback != null) {
                            callback.onFailure(new Exception(byteArrayOutputStream.toString()));
                        }
                        byteArrayOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

}