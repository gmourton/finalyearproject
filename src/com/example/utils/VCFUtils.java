package com.example.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

public class VCFUtils {
	private Cursor cursor;
	private ArrayList<String> vCard;
	private String vfile = "Contacts_" + System.currentTimeMillis() + ".vcf";
	private Context context;

	// need to pass in a context from the activity or service
	public VCFUtils(Context con) {
		context = con;
	}

	public boolean backUpContactsToVCF() throws IOException {
		vCard = new ArrayList<String>();
		cursor = context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
				null, null);
		if (cursor != null && cursor.getCount() > 0) {
			int i;
			String storage_path = Environment.getExternalStorageDirectory()
					.toString() + File.separator + vfile;
			FileOutputStream mFileOutputStream = new FileOutputStream(
					storage_path, false);
			cursor.moveToFirst();
			for (i = 0; i < cursor.getCount(); i++) {
				get(cursor);
				Log.d("TAG",
						"Contact " + (i + 1) + "VcF String is" + vCard.get(i));
				cursor.moveToNext();
				mFileOutputStream.write(vCard.get(i).toString().getBytes());
			}
			mFileOutputStream.close();
			cursor.close();
			return true;
		} else {
			Log.d("TAG", "No Contacts in Your Phone");
			return false;
		}
	}
	
	private void get(Cursor cursor2) {
	    String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)); 
	    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
	    AssetFileDescriptor fd;
	    try {
	        fd = context.getContentResolver().openAssetFileDescriptor(uri, "r");

	        FileInputStream fis = fd.createInputStream();
	        byte[] buf = new byte[(int) fd.getDeclaredLength()];
	        fis.read(buf);
	        String vcardstring= new String(buf);
	        vCard.add(vcardstring);
	    } catch (Exception e1) 
	    {
	        // TODO Auto-generated catch block
	        e1.printStackTrace();
	    }
	}
	
	public void delete() {
		ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
            contentResolver.delete(uri, null, null);
        }	
	
	}
}