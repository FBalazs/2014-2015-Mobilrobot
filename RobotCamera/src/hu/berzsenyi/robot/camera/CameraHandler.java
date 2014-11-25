package hu.berzsenyi.robot.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;

public class CameraHandler {
	public Camera camera = null;
	
	public void lockCamera() {
		this.camera = null;
		try {
			this.camera = Camera.open();
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		if(this.camera != null)
			try {
				this.camera.lock();
			} catch (Exception e) {
				e.printStackTrace();
				this.releaseCamera();
			}
	}
	
	public void addDefaultPreviewBuffer() {
		this.camera.addCallbackBuffer(new byte[this.camera.getParameters().getPreviewSize().width * this.camera.getParameters().getPreviewSize().height
				* ImageFormat.getBitsPerPixel(this.camera.getParameters().getPreviewFormat()) / 8]);
	}
	
	public int getPreviewWidth() {
		return this.camera.getParameters().getPreviewSize().width;
	}
	
	public int getPreviewHeight() {
		return this.camera.getParameters().getPreviewSize().height;
	}
	
	public void releaseCamera() {
		if(this.camera != null) {
			try {
				this.camera.unlock();
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.camera.release();
			this.camera = null;
		}
	}
}
