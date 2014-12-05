package hu.berzsenyi.mr14.camera;

import java.net.InetSocketAddress;

import hu.berzsenyi.mr14.net.IClientConnectionListener;
import hu.berzsenyi.mr14.net.NetworkHandler;
import hu.berzsenyi.robotcamera.R;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ActivityMain extends Activity implements PreviewCallback, IClientConnectionListener {
//	CameraHandler cameraHandler;
	NetworkHandler net;
	CameraStreamer streamer;
	long frame = 0;
	int streamParts = 8;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(this.getClass().getName(), "onCreate()");
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
//		this.cameraHandler = new CameraHandler();
//		this.cameraHandler.lockCamera();
//		if(this.cameraHandler.camera == null) {
//			Log.e(this.getClass().getName(), "Couldn't open camera!");
//			Toast.makeText(this, "Couldn't open camera!", Toast.LENGTH_LONG).show();
//			this.finish();
//		} else {
//			this.cameraHandler.addDefaultPreviewBuffer();
//			this.cameraHandler.camera.setPreviewCallbackWithBuffer(this);
//		}
		this.net = new NetworkHandler();
		this.net.setClientConnectionListener(this);
		this.net.bind(8080);
		this.streamer = new CameraStreamer();
	}
	
	@Override
	public void onClientConnected(InetSocketAddress address) {
//		this.net.send(new PacketStreamInfo(0, this.cameraHandler.getPreviewWidth(), this.cameraHandler.getPreviewHeight(), this.streamParts), true);
		this.streamer.open(8081, address);
	}
	
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
//		Log.d(this.getClass().getName(), "onPreviewFrame()");
		
		if(!this.net.open) // reopen the connection if it had been lost
			this.net.bind(8080);
		
		if(this.net.remoteAddress != null) {
			
			
			Log.d(this.getClass().getName(), "frame="+this.frame);
			this.frame++;
		}
		
		camera.addCallbackBuffer(data);
	}
	
	@Override
	protected void onDestroy() {
		Log.d(this.getClass().getName(), "onDestroy()");
		super.onDestroy();
		
//		if(this.cameraHandler.camera != null) {
//			this.cameraHandler.camera.setPreviewCallback(null);
//			this.cameraHandler.camera.stopPreview();
//			this.cameraHandler.releaseCamera();
//		}
		this.streamer.close();
		this.net.close();
	}
}
