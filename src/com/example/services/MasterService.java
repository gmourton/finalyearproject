package com.example.services;

import java.io.IOException;

import com.example.multiactivitytest.SoundActivity;
import com.example.recievers.OutGoingReciever;
import com.example.utils.SMSUtils;
import com.example.utils.VCFUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.ServiceState;
import android.util.Log;

public class MasterService extends Service {
	private MyWebRequestReceiver receiver;
	private PendingIntent mAlarmSender;
	private static final long THIRTY_SECONDS_MILLIS = 80 * 1000;
	// Contains a handle to the system alarm service
	private AlarmManager mAlarmManager;
	private Intent intentTest;
	private IntentFilter screenOnFilter;
	private BroadcastReceiver mReceiver;

	private BroadcastReceiver outGoing;
	private IntentFilter outgoingFilter;

	// change this back to FALSE - changed this to true to stop this running
	// everytime i add new functionality to the application
	// also put this var to false when the state is changed
	private boolean updatedUploadService = false;

	private boolean hasActionScreenOnBeenReg = false;

	private boolean outgoingBool = false;

	public class MyWebRequestReceiver extends BroadcastReceiver {

		public static final String PROCESS_RESPONSE = "com.example.stateservicetesting.intent.action.PROCESS_RESPONSE"; 

		@Override
		public void onReceive(Context context, Intent intent) {
			String responseString = intent
					.getStringExtra(StateService.REQUEST_STRING);
			Log.d("MasterService", "Returned state: " + responseString);
			if (responseString.equalsIgnoreCase("FINE")) {
				//make sure the location service gets stopped
				stopService(new Intent(MasterService.this, LocationService.class));
				updatedUploadService = false;
				
				
				// could perhaps check the outgoingBool instead
				try {
					unregisterReceiver(outGoing);

				} catch (IllegalArgumentException e) {

				}
				try {
					unregisterReceiver(mReceiver);

				} catch (IllegalArgumentException e) {

				}
				hasActionScreenOnBeenReg = false;
				outgoingBool = false;
				Log.d("MasterService", "FINE - nothing happens in this state");
			} else if (responseString.equalsIgnoreCase("MINOR")) {
				//make sure the location service gets stopped
				stopService(new Intent(MasterService.this, LocationService.class));
				hasActionScreenOnBeenReg = false;
				updatedUploadService = false;
				try {
					unregisterReceiver(outGoing);

				} catch (IllegalArgumentException e) {

				} 
				try {
					unregisterReceiver(mReceiver);

				} catch (IllegalArgumentException e) {

				}
				outgoingBool = false;
				Log.d("MasterService", "MINOR - device is in the house etc");
				Intent inten = new Intent(context, SoundActivity.class);
				inten.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(inten);
			} else if (responseString.equalsIgnoreCase("MAJOR")) {
				Log.d("MasterService", "MAJOR - device is lost or stolen");
				if (updatedUploadService == false) {
					//backup contacts
					VCFUtils backupVCF = new VCFUtils(MasterService.this);
					try {
						backupVCF.backUpContactsToVCF();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//backup sms
					SMSUtils backupSMS = new SMSUtils(MasterService.this);
					backupSMS.backup();
					//run backup
					Log.d("MasterService", "About to start backup service");
					startService(new Intent(context, BulkBackupService.class));
					// now set the service to done
					updatedUploadService = true;
				} else {
					Log.d("MasterService",
							"Backup service has already ran, shouldn't run again unless state changes occur");
				}

				if (outgoingBool == false) {
					// register reciever for outgoing call
					// android.intent.action.NEW_OUTGOING_CALL
					outgoingFilter = new IntentFilter(
							"android.intent.action.NEW_OUTGOING_CALL");
					outGoing = new OutGoingReciever();
					registerReceiver(outGoing, outgoingFilter);
					outgoingBool = true;
				}

				// also set up an intentfilter to filter ACTION_SCREEN_ON events
				// from this event fired, call the FCameraService
				if (hasActionScreenOnBeenReg == false) {
					screenOnFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
					// screenOnFilter.addAction(Intent.ACTION_SCREEN_OFF);
					mReceiver = new ScreenOnReceiver();
					registerReceiver(mReceiver, screenOnFilter);
					hasActionScreenOnBeenReg = true; 

				}
				
				//start the location service 
				startService(new Intent(MasterService.this, LocationService.class)); 
			}

		}

	}

	// TODO:will probably need to change this to everytime - perhaps after 10
	// pictures, it can unregister
	public class ScreenOnReceiver extends BroadcastReceiver {
		// perhaps take a look at facial recognition

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (arg1.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				Log.d("ScreenOnReceiver", "Screen was turned on"); 
 
				startService(new Intent(arg0, FCameraService.class));

				// now unregister the this receiver
				// set the bool to false when state changes - need to also call unregister when the state changes
				unregisterReceiver(mReceiver);
				hasActionScreenOnBeenReg = false;
			}

		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		IntentFilter filter = new IntentFilter(
				MyWebRequestReceiver.PROCESS_RESPONSE);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new MyWebRequestReceiver();
		registerReceiver(receiver, filter);

		intentTest = new Intent(MasterService.this, StateService.class);
		// intentTest.putExtra("handler",
		// new Messenger(handler));
		// Create a PendingIntent to trigger a startService() for AlarmService
		mAlarmSender = PendingIntent.getService( // set up an intent for a call
													// to a service
				MasterService.this, // the current context
				0, // request code (not used)
				intentTest, // A new
							// Service
							// intent
				0 // flags (none are required for a service)
				);

		// Gets the handle to the system alarm service
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		// Sets the time when the alarm will first go off
		// The Android AlarmManager uses this form of the current time.
		long firstAlarmTime = SystemClock.elapsedRealtime();

		// Sets a repeating countdown timer that triggers AlarmService
		mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, // based
																			// on
																			// time
																			// since
																			// last
																			// wake
																			// up
				firstAlarmTime, // sends the first alarm immediately
				THIRTY_SECONDS_MILLIS, // repeats every thirty seconds
				mAlarmSender // when the alarm goes off, sends this Intent
				);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
