package hu.berzsenyi.robot.camera;

import java.net.InetSocketAddress;

import hu.berzsenyi.robot.net.IClientConnectionListener;
import hu.berzsenyi.robot.net.NetworkHandler;
import hu.berzsenyi.robot.net.packet.PacketStreamInfo;
import hu.berzsenyi.robotcamera.R;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ActivityMain extends Activity implements PreviewCallback, IClientConnectionListener {
	CameraHandler cameraHandler;
	NetworkHandler net;
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
		this.net = new NetworkHandler();
		this.net.setClientConnectionListener(this);
		this.net.bind(8080);
	}
	
	@Override
	public void onClientConnected(InetSocketAddress address) {
		this.net.send(new PacketStreamInfo(0, this.cameraHandler.getPreviewWidth(), this.cameraHandler.getPreviewHeight()), true);
	}
	
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
//		Log.d(this.getClass().getName(), "onPreviewFrame()");
		
		if(!this.net.open) // reopen the connection if it had been lost
			this.net.bind(8080);
		
		if(this.net.remoteAddress != null) {
			if(this.netBuffer == null)
				this.netBuffer = new byte[this.cameraHandler.getPreviewWidth()*this.cameraHandler.getPreviewHeight()];
			for(int i = 0; i < this.netBuffer.length; i++)
				if(i%2 == 0)
					this.netBuffer[i] = data[i*3/2];
				else
					this.netBuffer[i] = (byte) (data[i*3/2] >> 4);
//			this.net.send(new PacketStreamInfo(0, 5, 6), false);
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
		this.net.close();
	}
}
