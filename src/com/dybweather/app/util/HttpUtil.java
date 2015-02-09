package com.dybweather.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class HttpUtil {

	public static void sendHttpRequest(final String address, final HttpCallbackListener listener){
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection)url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(60000);
					connection.setReadTimeout(60000);
					Log.d("HttpUtil", connection.getResponseCode() + " ,responseMessage:" + connection.getResponseMessage());
					InputStream in = connection.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					StringBuilder response = new StringBuilder();
					String line = "";
					
					while((line = reader.readLine()) != null){
						if(line.startsWith("<")){
							throw new Exception("网页无法访问");
						} else {
							response.append(line);
						}
					}
					//Log.d("HttpUtil", "result====>: " + response.toString());
					if(listener != null){
						//回调onFinish()方法
						listener.onFinish(response.toString());
					}
				} catch(Exception e){
					if(listener != null){
						//回调onError()方法
						listener.onError(e);
					} else {
						e.printStackTrace();
					}
				} finally {
					if(connection != null){
						connection.disconnect();
					}
				}
			}
		}).start();
	}
}
