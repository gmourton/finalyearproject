package com.example.multiactivitytest;

import java.io.File;
import java.io.IOException;

import com.example.services.CheckandDelete;

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

public class InitialAct extends Activity {
	private String uniqueid;
	private Context c = this;
	private MyWebRequestReceiver receiver;
	private Intent intentTest;
	private IntentFilter filter;
	private boolean onlyOncePlease = false;
	private File dummyFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_initial);
		uniqueid = Secure.getString(getBaseContext().getContentResolver(),
				Secure.ANDROID_ID);
		dummyFile = new File(Environment.getExternalStorageDirectory()
				.getPath()
				+ "/Android/data/"
				+ Secure.getString(getBaseContext().getContentResolver(),
						Secure.ANDROID_ID) + ".txt");

		// check and delete whether the device is already registered
		filter = new IntentFilter(MyWebRequestReceiver.PROCESS_RESPONSE);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new MyWebRequestReceiver();
		registerReceiver(receiver, filter);

		intentTest = new Intent(InitialAct.this, CheckandDelete.class); 
		intentTest.putExtra("uniqueId", uniqueid);
		startService(intentTest);

	}

	public class MyWebRequestReceiver extends BroadcastReceiver {

		public static final String PROCESS_RESPONSE = "com.intent.action.PROCESS_RESPONSE";

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("RESONSE",
					"Server says: " + intent.getStringExtra("Response"));

			if (intent.getStringExtra("Response").contains("SUCCESS")) {
				// found that the phone should already be registered - make the
				// file and start the init activity
				// File dummyFile = new File(Environment
				// .getExternalStorageDirectory().getPath() + "/dummy.txt");
				try {
					dummyFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// mightr be starting the service twice - might need a check
				// somehow

				if (onlyOncePlease == false) {
					Intent intentt = new Intent(context, AlreadyReg.class);
					startActivity(intentt);
					onlyOncePlease = true;
					try {
						unregisterReceiver(receiver);
					} catch (IllegalArgumentException e) {
					}
					finish();
				}
				finish();

			} else if (intent.getStringExtra("Response").contains("FAIL")) {
				// found that the phone hasn't been registered - start the reg
				// activity

				// check if there is a file there somehow -if so delete
				// File dummyFile = new File(Environment
				// .getExternalStorageDirectory().getPath() + "/dummy.txt");
				if (dummyFile.exists()) {
					dummyFile.delete();
				}

				Intent intentt = new Intent(context, NotReg.class);
				startActivity(intentt);
				try {
					unregisterReceiver(receiver);
				} catch (IllegalArgumentException e) {
				}
				finish();
			} else {
				Log.d("RESONSE",
						"is it getting here?Server says: "
								+ intent.getStringExtra("Response"));
				try {
					unregisterReceiver(receiver);
				} catch (IllegalArgumentException e) {
				}
			}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_initial, menu);
		return true;
	}

}
