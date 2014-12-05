package hu.berzsenyi.mr14.camera;

import java.net.InetSocketAddress;

import hu.berzsenyi.mr14.ToByteArrayOutputStream;
import hu.berzsenyi.mr14.net.IConnectionListener;
import hu.berzsenyi.mr14.net.TCPConnection;
import hu.berzsenyi.mr14.net.TCPMessage;
import hu.berzsenyi.mr14.net.UDPConnection;
import hu.berzsenyi.robotcamera.R;
import android.app.Activity;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ActivityMain extends Activity implements PreviewCallback, IConnectionListener {
	public CameraHandler camera = new CameraHandler();
	public TCPConnection tcp = new TCPConnection();
	public UDPConnection udp = new UDPConnection();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(this.getClass().getName(), "onCreate()");
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
		this.camera.lockCamera();
		if(this.camera.camera == null) {
			Log.e(this.getClass().getName(), "Couldn't open camera!");
			Toast.makeText(this, "Couldn't open camera!", Toast.LENGTH_LONG).show();
			this.finish();
		} else {
			this.camera.addDefaultPreviewBuffer();
			this.camera.camera.setPreviewCallbackWithBuffer(this);
			this.camera.camera.startPreview();
		}
		
		this.tcp.setListener(this);
		this.tcp.listen(8080);
	}
	
	@Override
	public void onConnected(InetSocketAddress remoteAddr) {
		Log.d(this.getClass().getName(), "onConnected()");
		
		this.udp.connect(8080, remoteAddr);
	}

	@Override
	public void onDisconnected() {
		Log.d(this.getClass().getName(), "onDisconnected()");
		
		this.tcp.close();
		this.udp.close();
		this.tcp.listen(8080);
	}
	
	public byte[] netBuffer = new byte[60000];
	
//	public static class UpdateThread extends Thread {
//		public ActivityMain activity;
//		public byte[] data;
//		public Camera camera;
//		
//		public UpdateThread(ActivityMain activity, byte[] data, Camera camera) {
//			super("Thread-Update");
//			this.activity = activity;
//			this.data = data;
//			this.camera = camera;
//		}
//		
//		@Override
//		public void run() {
////			if(!this.activity.tcp.open && !this.activity.tcp.connecting)
////				this.activity.tcp.listen(8080);
//			
//			if(this.activity.udp.open) {
//				this.activity.udp.send(this.activity.netBuffer, 0, 1024);
//			}
//			
//			while(this.activity.tcp.open && 0 < this.activity.tcp.available()) {
//				TCPMessage msg = this.activity.tcp.readMsg();
//				if(msg != null)
//					Log.d(this.activity.getClass().getName(), "msg.type="+msg.type+" msg.length="+msg.length);
//				else
//					Log.w(this.activity.getClass().getName(), "msg=null");
//			}
//			
//			this.camera.addCallbackBuffer(this.data);
//		}
//	}
	
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
//		Log.d(this.getClass().getName(), "onPreviewFrame()");
		
		if(this.udp.open) {
			try {
				YuvImage img = new YuvImage(data, this.camera.getParams().getPreviewFormat(), this.camera.getPreviewWidth(), this.camera.getPreviewHeight(), null);
				ToByteArrayOutputStream bout = new ToByteArrayOutputStream(this.netBuffer);
				img.compressToJpeg(new Rect(0, 0, this.camera.getPreviewWidth(), this.camera.getPreviewHeight()), 75, bout);
				if(60000 < bout.size())
					Log.e(this.getClass().getName(), "Image size is too big! ("+bout.size()+" bytes)");
				else
					this.udp.send(this.netBuffer, 0, bout.size());
				bout.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
//		while(this.tcp.open && 0 < this.tcp.available()) {
//			TCPMessage msg = this.tcp.readMsg();
//			if(msg != null)
//				Log.d(this.getClass().getName(), "msg.type="+msg.type+" msg.length="+msg.length);
//			else
//				Log.w(this.getClass().getName(), "msg=null");
//		}
		
		camera.addCallbackBuffer(data);
		
//		new UpdateThread(this, data, camera).start();
	}
	
	@Override
	protected void onDestroy() {
		Log.d(this.getClass().getName(), "onDestroy()");
		super.onDestroy();
		
		this.tcp.close();
		this.udp.close();
		
		if(this.camera.camera != null) {
			this.camera.camera.setPreviewCallbackWithBuffer(null);
			this.camera.camera.stopPreview();
			this.camera.releaseCamera();
		}
	}
}
