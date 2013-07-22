package com.example.recievers;

import com.example.services.OutgoingCallUpload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;

public class OutGoingReciever extends BroadcastReceiver {
	private String number;
	private String date;
	private String uniqueId;
	
	private Intent intentUpload;

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		// check the intent is outgoing
		if (extras != null) {
			// listen for outgoing calls
			number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			uniqueId = Secure.getString(context.getContentResolver(),
					Secure.ANDROID_ID);
			Log.d("CallDetection", number);
			Log.d("CallDetection", extras.toString());

			// call a service to input the number and time into a database
			
			intentUpload = new Intent(context,
					OutgoingCallUpload.class);
			intentUpload.putExtra("number", number);
			intentUpload.putExtra("date", String.valueOf(System.currentTimeMillis()));
			intentUpload.putExtra("uniqueId", uniqueId);
			context.startService(intentUpload);
		}
	}

}