package com.example.multiactivitytest;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NotReg extends Activity {
	private EditText usernameField;
	private EditText passwordField;
	private TextView responseFromServer;
	private EditText uniqueId;
	private String android_id;
	private MyWebRequestReceiver receiver;
	private Intent intentTest;
	private IntentFilter filter;
	private Button okButton;
	private EditText responseView;

	private String username;
	private String password;
	private String id;

	private boolean registerReceiverOnce = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_not_reg);

		usernameField = (EditText) findViewById(R.id.editText1);
		passwordField = (EditText) findViewById(R.id.editText2);
		responseFromServer = (TextView) findViewById(R.id.editText3);
		// uniqueId = (EditText) findViewById(R.id.editTextUnique);
		okButton = (Button) findViewById(R.id.button1);
		android_id = Secure.getString(getBaseContext().getContentResolver(),
				Secure.ANDROID_ID);
		// uniqueId.setText(android_id);
		if (registerReceiverOnce == false) {
			registerReceiverOnce = true;
			filter = new IntentFilter(MyWebRequestReceiver.PROCESS_RESPONSE);
			filter.addCategory(Intent.CATEGORY_DEFAULT);
			receiver = new MyWebRequestReceiver();
			registerReceiver(receiver, filter);

		}
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				username = usernameField.getText().toString();
				password = md5(passwordField.getText().toString());
				//md5 the password
				
				// id = uniqueId.getText().toString();
				id = android_id; 
				intentTest = new Intent(NotReg.this,
						RegisterDeviceService.class);
				intentTest.putExtra("username", username);
				intentTest.putExtra("password", password);
				intentTest.putExtra("uniqueId", id);
				startService(intentTest);
			}
		});
	}
	/* Did not get enough time to implement/find a library for sha etc */
	public static String md5(String string) {
	    byte[] hash;

	    try {
	        hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
	    } catch (NoSuchAlgorithmException e) {
	        throw new RuntimeException("Huh, MD5 should be supported?", e);
	    } catch (UnsupportedEncodingException e) {
	        throw new RuntimeException("Huh, UTF-8 should be supported?", e);
	    }

	    StringBuilder hex = new StringBuilder(hash.length * 2);

	    for (byte b : hash) {
	        int i = (b & 0xFF);
	        if (i < 0x10) hex.append('0');
	        hex.append(Integer.toHexString(i));
	    }

	    return hex.toString();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_not_reg, menu);
		return true;
	}

	public class MyWebRequestReceiver extends BroadcastReceiver {

		public static final String PROCESS_RESPONSE = "com.intent.action.PROCESS_RESPONSE";

		@Override
		public void onReceive(Context context, Intent intent) {
			responseFromServer.setText(intent.getStringExtra("Response"));
			Log.d("RESONSE", intent.getStringExtra("Response"));
			if (intent.getStringExtra("Response").contains("SUCCESS")) {
				// create the dummy file
				File dummyFile = new File(Environment
						.getExternalStorageDirectory().getPath() + "/dummy.txt");
				File dummyFile2 = new File(Environment.getExternalStorageDirectory()
						.getPath()
						+ "/Android/data/"
						+ Secure.getString(getBaseContext().getContentResolver(),
								Secure.ANDROID_ID) + ".txt");
				try {
					dummyFile.createNewFile();
					dummyFile2.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Intent intentAct = new Intent(context, InitialAct.class); 
				startActivity(intentAct);
				// unregister the receiver
				try {
					unregisterReceiver(receiver);
				} catch (IllegalArgumentException e) {
				}
				finish();
			}
		}

	}

}
