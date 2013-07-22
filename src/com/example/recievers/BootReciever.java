package com.example.recievers;

import com.example.services.MasterService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReciever extends BroadcastReceiver {
	
	 private static final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context con, Intent intent) {
		Log.d("BOOTR", "should be called on start");
		if (BOOT_ACTION.equals(intent.getAction()))
        {
			//start the masterservice
			con.startService(new Intent(con, MasterService.class));
        }
		
	}

}
