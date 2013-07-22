package com.example.multiactivitytest;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SoundActivity extends Activity {
	
	private Button stopButton;
	private MediaPlayer mMediaPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound);
		
		stopButton = (Button) findViewById(R.id.button1);
		stopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					mMediaPlayer.stop();
					//close the activity here!
					finish();
				} catch (Exception e) {
				}
			}
		});

		try {
			Uri path = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm45);
			
			Uri alert = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_ALARM);
			mMediaPlayer = new MediaPlayer(); 
			mMediaPlayer.setDataSource(this, path);
			final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mMediaPlayer.setLooping(false);
				mMediaPlayer.prepare();
				mMediaPlayer.start();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_sound, menu);
		return true;
	}

}
