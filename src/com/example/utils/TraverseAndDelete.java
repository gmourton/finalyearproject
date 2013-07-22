package com.example.utils;

import java.io.File;
import java.io.IOException;

import android.util.Log;

public class TraverseAndDelete {
	private String directory;
	
	public TraverseAndDelete(String dir) {
		directory = dir;
	}

	public void traverse(File parentNode) throws IOException {
		if (parentNode.isDirectory()) {

			// get and list all child nodes, if any
			File[] childNodes = parentNode.listFiles();
			for (File child : childNodes) {
				// .listFiles() returns null if not a dir
				// recurse
				traverse(child);
			}
		} else {
			//delete file
		//	Log.d("DELETE", parentNode.getPath());
			Log.d("DELETE",parentNode.getPath() + " has been deleted: "+ parentNode.delete());
		}
	}
}
