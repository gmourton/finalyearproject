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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.util.Log;

/* --WONT MATTER AS STOP SERVICE IS CALLED FROM OUTSIDE NOW
 * Could be a problem later on in this class where the service still runs - like now
 * edit: seems to not be a problem after removing coord updates and calling stopself
 * still might be running though
 */
public class LocationService extends Service implements LocationListener {
	private LocationManager locationManager;
	private Criteria criteria;
	private String bestProvider;
	private double lon;
	private double lat;
	private Thread thread;
	private NetworkRunner runner = new NetworkRunner();

	boolean gps = false;
	boolean network = false;

	@Override
	public void onDestroy() {
		try {
			locationManager.removeUpdates(this);
		} catch (IllegalArgumentException e) {

		}
		Log.d("MainActivity", "stopped");
	}

	@Override
	public void onCreate() {
		Log.d("LocationService", "ONCREATE CALLED, SHOULD ONLY SEE THIS ONCE");
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// catch exceptions and just swallow them
		try {
			gps = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception e) {

		}

		try {
			network = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception e) {

		}

		if (gps == false && network == false) {
			// no data available, stop the service
			stopSelf();
		}

		if (gps == true) {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 30000, 15, this);
			Log.d("LocationService", "gps enabled, requesting updates now");
		} else {
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 30000, 50, this);
			Log.d("LocationService", "network enabled, requesting updates now"); 
		}
	}

	private class NetworkRunner implements Runnable {
		public void run() {
			try {
				postData();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		lat = location.getLatitude();
		lon = location.getLongitude();
		Log.d("LocationService", "lat: " + lat);
		Log.d("LocationService", "lon: " + lon);

		// run a test thread here to just simply upload
		thread = new Thread(runner);
		thread.start();
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private void postData() throws JSONException {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://www.georgemourton.com/LocationTest.php");
		JSONObject json = new JSONObject();
		Log.d("MainActivity", "Thread being ran, serverside problem? number1");
		try {
			// JSON data:
			json.put("UID", Secure.getString(getBaseContext()
					.getContentResolver(), Secure.ANDROID_ID));
			json.put("lon", lon);
			json.put("lat", lat);

			// current date and time
			Date date = new Date();
			SimpleDateFormat sdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			json.put("thedate", sdate.format(date));

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
				// latituteField.append(sb.toString());
				// message = sb.toString();
				Log.d("MainActivity", "Response made by server" + sb.toString());
			} else if (response == null) {
				// making sure
				Log.d("MainActivity", "Response is null");
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		} catch (Exception e) {
			Log.w("AlarmService", e.getMessage());
		}
		// locationManager.removeUpdates(this);
		// stopSelf();
	}
}