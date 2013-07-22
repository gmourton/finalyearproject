package com.example.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class OutgoingCallUpload extends IntentService {
	private String number;
	private String date;
	private String uniqueId;
	private String serverResponse;
	

	public OutgoingCallUpload() {
		super("OutgoingCallUpload");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		number = (String) extras.get("number");
		//date = (String) extras.get("date");
		uniqueId = (String) extras.get("uniqueId");
		
		try {
			postData();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void postData() throws JSONException {
		Log.d("RegisterDeviceService", "PostData() being called");
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://www.georgemourton.com/OutgoingCalls.php");
		JSONObject json = new JSONObject(); 

		try {
			// JSON data:
			json.put("NUMBER", number); 
			// current date and time
			Date date = new Date();
			SimpleDateFormat sdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			json.put("THEDATE", sdate.format(date));
			json.put("UID", uniqueId);
			Log.d("RegisterDeviceService", number +" " + sdate.format(date) + " " + uniqueId);
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
