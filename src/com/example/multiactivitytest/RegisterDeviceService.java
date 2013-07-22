package com.example.multiactivitytest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.multiactivitytest.NotReg.MyWebRequestReceiver;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class RegisterDeviceService extends IntentService {
	public static final String REQUEST_STRING = "myRequest";
	private String username;
	private String password;
	private String uniqueId;
	private String serverResponse = "default"; 

	public RegisterDeviceService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	public RegisterDeviceService() {
		super("RegisterDeviceService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Bundle extras = intent.getExtras();
		username = (String) extras.get("username");
		password = (String) extras.get("password");
		uniqueId = (String) extras.get("uniqueId");
		try {
			postData();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//just send the data back as a quick test to ensue i can send data between services etc
		Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MyWebRequestReceiver.PROCESS_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("Response", serverResponse);
//        broadcastIntent.putExtra("passwordback", password + "back");
//        broadcastIntent.putExtra("idback", uniqueId + "back");
        sendBroadcast(broadcastIntent); 
		
	}
	
	private void postData() throws JSONException {
		Log.d("RegisterDeviceService", "PostData() being called");
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://www.georgemourton.com/RegisterDevice.php");
		JSONObject json = new JSONObject(); 

		try {
			// JSON data:
			json.put("USERNAME", username); 
			json.put("PASSWORD", password);
			json.put("UID", uniqueId);

			JSONArray postjson = new JSONArray();
			postjson.put(json);

			// Post the data:
			httppost.setHeader("json", json.toString());
			httppost.getParams().setParameter("jsonpost", postjson);

			// Execute HTTP Post Request
			// System.out.print(json);
			HttpResponse response = httpclient.execute(httppost);

			// for JSON:
			if (response != null) {
				InputStream is = response.getEntity().getContent();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));
				StringBuilder sb = new StringBuilder();

				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				serverResponse = sb.toString();
				Log.d("RegisterDeviceService", serverResponse);
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		} catch (Exception e) {
			Log.w("RegisterDeviceService", e.getMessage());
		}
	}
}
