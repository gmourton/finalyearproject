package com.example.services;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;

public class FImageUploadService extends IntentService {

	private String urlString = "http://www.georgemourton.com/FCameraUpload.php";
	private HttpEntity resEntity;

	public FImageUploadService() {
		super("FImageUploadService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		String path = extras.getString("FILEPATH");
		// TODO Auto-generated method stub
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(urlString);
			MultipartEntity reqEntity = new MultipartEntity();
			// files
			// example reqEntity.addPart("uploadedfile1", bin1); need to loop
			// could throw filenotfound etc
			reqEntity.addPart("uploadedfile", new FileBody(new File(
					path)));
			// params
			reqEntity.addPart( 
					"user",
					new StringBody(Secure.getString(getBaseContext()
							.getContentResolver(), Secure.ANDROID_ID)));
			post.setEntity(reqEntity);
			HttpResponse response = client.execute(post);
			resEntity = response.getEntity();
			final String response_str = EntityUtils.toString(resEntity); 
			if (resEntity != null) {
				Log.d("RESPONSE", response_str);

			}

		} catch (Exception e) {
			Log.e("Debug", "error: " + e.getMessage(), e);
		}
		Log.d("RESPONSE", "Only here after the response");
		// delete the image
		File deleteFile = new File(path);
		if (deleteFile.exists()) {
			deleteFile.delete();
		}
	}

}
