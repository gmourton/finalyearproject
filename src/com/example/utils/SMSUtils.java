package com.example.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class SMSUtils {
	private final String CONTENT = "content://sms";
	private Cursor inboxCursor;
	private Context context;
	private File xmlFile = new File("BackupSMS_"
			+ System.currentTimeMillis() + ".bsms");
	private String storage_path = Environment.getExternalStorageDirectory().toString()
			+ File.separator + xmlFile;

	private FileOutputStream fileos = null;
	private StringBuilder sb = new StringBuilder();

	public SMSUtils(Context con) { 
		context = con;
		inboxCursor = context.getContentResolver().query(Uri.parse(CONTENT),
				null, null, null, null);
	}

	public void backup() {
		if (inboxCursor.getCount() == 0) {
			Log.d("TAG", "SMS list is empty");
		} else {
			inboxCursor.moveToFirst();
			Log.d("TAG", "count is " + inboxCursor.getCount());
			do {
				String msgData = "";
				for (int idx = 0; idx < inboxCursor.getColumnCount(); idx++) {

					sb.append(inboxCursor.getColumnName(idx) + ":"
							+ inboxCursor.getString(idx) + "\n");

					msgData = " " + inboxCursor.getColumnName(idx) + ":"
							+ inboxCursor.getString(idx);
					Log.d("TEST", msgData);
				}
			} while (inboxCursor.moveToNext());

			try {
				fileos = new FileOutputStream(storage_path);

				fileos.write(sb.toString().getBytes());
				fileos.close();

			} catch (FileNotFoundException e) {
				Log.e("FileNotFoundException", e.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void delete() {
		Uri uri = Uri.parse("content://sms");

		ContentResolver contentResolver = context.getContentResolver();

		Cursor cursor = contentResolver.query(uri, null, null, null, null);

		while (cursor.moveToNext()) {

			long thread_id = cursor.getLong(1);
			Uri thread = Uri.parse("content://sms/conversations/" + thread_id);
			context.getContentResolver().delete(thread, null, null);
		}
	}
}
