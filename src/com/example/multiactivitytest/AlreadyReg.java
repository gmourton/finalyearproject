package com.example.multiactivitytest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import com.example.services.MasterService;

public class AlreadyReg extends Activity {
	private boolean runOnce = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_already_reg);
		
		if(runOnce == false) {
			runOnce = true;
			//start the service
			startService(new Intent(this, MasterService.class));
			//finish activity - should be none
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_already_reg, menu);
		return true;
	}

}
