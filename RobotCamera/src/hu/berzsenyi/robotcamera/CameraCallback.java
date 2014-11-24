package hu.berzsenyi.robotcamera;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;

public class CameraCallback implements PreviewCallback {
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Parameters params = camera.getParameters();
		Log.d("CameraCallback.java", "width="+params.getPreviewSize().width+" height="+params.getPreviewSize().height);
	}
}
