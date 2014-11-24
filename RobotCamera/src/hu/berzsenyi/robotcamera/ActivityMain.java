package hu.berzsenyi.robotcamera;

import java.net.SocketAddress;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ActivityMain extends Activity implements PreviewCallback, NetworkTCPServer.IConnectionListener {
	CameraHandler cameraHandler;
	NetworkTCPServer netTCP;
	NetworkUDPServer netUDP;
	byte[] netBuffer = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(this.getClass().getName(), "onCreate()");
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
		this.cameraHandler = new CameraHandler();
		this.cameraHandler.lockCamera();
		if(this.cameraHandler.camera == null) {
			Log.e(this.getClass().getName(), "Couldn't open camera!");
			Toast.makeText(this, "Couldn't open camera!", Toast.LENGTH_LONG).show();
			this.finish();
		} else {
			this.cameraHandler.addDefaultPreviewBuffer();
			this.cameraHandler.camera.setPreviewCallbackWithBuffer(this);
			this.cameraHandler.camera.startPreview();
		}
		this.netTCP = new NetworkTCPServer();
		this.netTCP.open(8080);
		this.netUDP = new NetworkUDPServer();
		this.netUDP.open(8080);
	}
	
	@Override
	public void onConnection(SocketAddress address) {
		this.netUDP.clientAddress = address;
	}
	
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Log.d(this.getClass().getName(), "onPreviewFrame()");
		
		if(this.netTCP.connected()) {
			if(this.netBuffer == null)
				this.netBuffer = new byte[this.cameraHandler.getPreviewWidth()*this.cameraHandler.getPreviewHeight()];
			for(int i = 0; i < this.netBuffer.length; i++)
				if(i%2 == 0)
					this.netBuffer[i] = data[i*3/2];
				else
					this.netBuffer[i] = (byte) (data[i*3/2] >> 4);
			//this.netUDP.send(this.netBuffer);
			this.netUDP.send(new byte[]{0, 1, 2, 3});
		}
		
		camera.addCallbackBuffer(data);
	}
	
	@Override
	protected void onDestroy() {
		Log.d(this.getClass().getName(), "onDestroy()");
		super.onDestroy();
		
		if(this.cameraHandler.camera != null) {
			this.cameraHandler.camera.setPreviewCallback(null);
			this.cameraHandler.camera.stopPreview();
			this.cameraHandler.releaseCamera();
		}
		this.netTCP.close();
		this.netUDP.close();
	}
}
