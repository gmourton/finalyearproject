package com.example.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

/*credits to stackoverflow */


public class DynaMounts {
	private ArrayList<String> mounts = new ArrayList<String>();
	private ArrayList<String> vold = new ArrayList<String>();
	public String[] labels;
	public String[] paths;
	public int count = 0;

	public void findStorage() {
		readMount();
		readVold();
		compareMountAndVold();
		testMounts();
		setProperties();
		//debugPrint(); 
	}
	
	//returns the internal and external storage paths along with their label
	public HashMap<String, String> getPaths() {
		HashMap<String, String> map = new HashMap<String, String>();
		
		for(int i = 0; i < paths.length; i++) {
			map.put(paths[i], labels[i]);
		}
		return map;
		
	}

	private void debugPrint() {
		for(int i = 0; i < labels.length; i++) {
			Log.d("LABELS", labels[i]);
		}
		for(int i = 0; i < paths.length; i++) {
			Log.d("PATHS", paths[i]);
		}
		
	}

	private void readMount() {
		// search the /proc/mounts file

		mounts.add("/mnt/sdcard");

		try {
			Scanner scanner = new Scanner(new File("/proc/mounts"));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("/dev/block/vold/")) {
					String[] lineElements = line.split(" ");
					String element = lineElements[1];

					// don't add the default mount path
					// it's already in the list.
					if (!element.equals("/mnt/sdcard"))
						mounts.add(element);
				}
			}
		} catch (Exception e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void readVold() {
		vold.add("/mnt/sdcard");

		try {
			Scanner scanner = new Scanner(new File("/system/etc/vold.fstab"));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("dev_mount")) {
					String[] lineElements = line.split(" ");
					String element = lineElements[2];

					if (element.contains(":"))
						element = element.substring(0, element.indexOf(":"));

					// don't add the default vold path
					// it's already in the list.
					if (!element.equals("/mnt/sdcard"))
						vold.add(element);
				}
			}
		} catch (Exception e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void compareMountAndVold() {
		for (int i = 0; i < mounts.size(); i++) {
			String mount = mounts.get(i);
			if (!vold.contains(mount))
				mounts.remove(i--);
		}

		// don't need this anymore, clear the vold list to reduce memory
		// use and to prepare it for the next time it's needed.
		vold.clear();
	}

	private void testMounts() {
		for (int i = 0; i < mounts.size(); i++) {
			String mount = mounts.get(i);
			File root = new File(mount);
			if (!root.exists() || !root.isDirectory() || !root.canWrite())
				mounts.remove(i--);
		}
	}

	private void setProperties() {
		/*
		 * At this point all the paths in the list should be valid. Build the
		 * public properties.
		 */

		ArrayList<String> mLabels = new ArrayList<String>();

		int j = 0;
		if (mounts.size() > 0) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
				mLabels.add("Auto");
			else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				if (Environment.isExternalStorageRemovable()) {
					mLabels.add("External SD Card 1");
					j = 1;
				} else
					mLabels.add("Internal Storage");
			} else {
				if (!Environment.isExternalStorageRemovable()
						|| Environment.isExternalStorageEmulated())
					mLabels.add("Internal Storage");
				else {
					mLabels.add("External SD Card 1");
					j = 1;
				}
			}

			if (mounts.size() > 1) {
				for (int i = 1; i < mounts.size(); i++) {
					mLabels.add("External SD Card " + (i + j));
				}
			}
		}

		labels = new String[mLabels.size()];
		mLabels.toArray(labels);

		paths = new String[mounts.size()];
		mounts.toArray(paths);

		count = Math.min(labels.length, paths.length);

		// don't need this anymore, clear the mounts list to reduce memory
		// use and to prepare it for the next time it's needed.
		mounts.clear();
	}

}