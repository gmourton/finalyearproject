package com.example.services;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class FCameraService extends Service {

	// Camera variables
	// a surface holder
	private SurfaceHolder sHolder;
	// a variable to control the camera
	private Camera mCamera;
	// the camera parameters 
	private Parameters parameters;
	private Context context = this;
	private String filePath = "/sdcard/FrontImage_"
			+ System.currentTimeMillis() + ".jpg";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		Log.d("No of cameras", Camera.getNumberOfCameras() + "");

		mCamera = openFrontCamera();
		SurfaceView sv = new SurfaceView(getApplicationContext());

		try {
			mCamera.setPreviewDisplay(sv.getHolder());
			parameters = mCamera.getParameters();

			// set camera parameters
			mCamera.setParameters(parameters);
			mCamera.startPreview();
			mCamera.takePicture(null, null, mCall);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Get a surface
		sHolder = sv.getHolder();
		// tells Android that this surface will have its data constantly
		// replaced
		sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	Camera.PictureCallback mCall = new Camera.PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {
			// decode the data obtained by the camera into a Bitmap

			FileOutputStream outStream = null;
			try {
				Log.d("FrontCamera", "bout to save");
				outStream = new FileOutputStream(filePath);
				outStream.write(data);
				outStream.close();
			} catch (FileNotFoundException e) {
				Log.d("CAMERA", e.getMessage());
			} catch (IOException e) {
				Log.d("CAMERA", e.getMessage());
			}
			mCamera.release();
			Intent intentt = new Intent(context, FImageUploadService.class);
			intentt.putExtra("FILEPATH", filePath);
			startService(intentt);
			stopSelf();
		}
	};

	private Camera openFrontCamera() {
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					cam = Camera.open(camIdx);
				} catch (RuntimeException e) {
					Log.e("FrontActivity",
							"Camera failed to open: " + e.getLocalizedMessage());
				}
			}
		}

		return cam;
	}

}
