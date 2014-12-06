package hu.berzsenyi.mr14.camera;

import java.net.InetSocketAddress;

import hu.berzsenyi.mr14.ToByteArrayOutputStream;
import hu.berzsenyi.mr14.net.IConnection;
import hu.berzsenyi.mr14.net.IConnectionListener;
import hu.berzsenyi.mr14.net.TCPConnection;
import hu.berzsenyi.mr14.net.TCPMessage;
import hu.berzsenyi.mr14.net.UDPConnection;
import hu.berzsenyi.mr14.net.msg.MsgDisconnect;
import hu.berzsenyi.mr14.net.msg.MsgQuality;
import hu.berzsenyi.mr14.net.msg.MsgSwitchPos;
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
	public static final int PORT = 8080;
	
	public CameraHandler camera = new CameraHandler();
	public TCPConnection tcp = new TCPConnection();
	public UDPConnection udp = new UDPConnection();
	public long lastReceiveTime;
	
	public int streamQuality = 50;
	
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
		this.tcp.listen(PORT);
	}
	
	@Override
	public void onConnected(IConnection connection, InetSocketAddress remoteAddr) {
		Log.d(this.getClass().getName(), "onConnected()");
		
		if(connection == this.tcp) {
			this.lastReceiveTime = System.currentTimeMillis();
			this.udp.setListener(this);
			this.udp.connect(PORT, new InetSocketAddress(remoteAddr.getAddress(), PORT));
		}
	}

	@Override
	public void onDisconnected(IConnection connection) {
		Log.d(this.getClass().getName(), "onDisconnected()");
		
		this.tcp.close();
		this.udp.close();
		this.tcp.listen(PORT);
	}
	
	public byte[] netBuffer = new byte[60000];
	
	public static class UpdateThread extends Thread {
		public ActivityMain activity;
		public byte[] data;
		public Camera camera;
		
		public UpdateThread(ActivityMain activity, byte[] data, Camera camera) {
			super("Thread-Update");
			this.activity = activity;
			this.data = data;
			this.camera = camera;
		}
		
		@Override
		public void run() {
			this.activity.update(this.data, this.camera);
		}
	}
	
	public void handleMessage(TCPMessage msg) {
//		Log.d(this.getClass().getName(), "msg.type="+msg.type+" msg.length="+msg.length);
		this.lastReceiveTime = System.currentTimeMillis();
		
		if(msg instanceof MsgQuality) {
			this.streamQuality = (int)((MsgQuality)msg).quality;
			Log.d(this.getClass().getName(), "streamQuality="+this.streamQuality);
		} else if(msg instanceof MsgDisconnect) {
			this.tcp.close();
		} else if(msg instanceof MsgSwitchPos) {
			// TODO start to switch position
		} else {
			Log.w(this.getClass().getName(), "Didn't handle tcp message!");
		}
	}
	
	public void update(byte[] data, Camera camera) {
		if(this.udp.open) {
			try {
				YuvImage img = new YuvImage(data, this.camera.getParams().getPreviewFormat(), this.camera.getPreviewWidth(), this.camera.getPreviewHeight(), null);
				ToByteArrayOutputStream bout = new ToByteArrayOutputStream(this.netBuffer);
				img.compressToJpeg(new Rect(0, 0, this.camera.getPreviewWidth(), this.camera.getPreviewHeight()), this.streamQuality, bout);
				if(60000 < bout.size()) {
					Log.e(this.getClass().getName(), "Image size is too big! ("+bout.size()+" bytes)");
					this.streamQuality--;
				} else
					this.udp.send(this.netBuffer, 0, bout.size());
				bout.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		while(this.tcp.open && 8 <= this.tcp.available()) {
			TCPMessage msg = this.tcp.readMsg();
			if(msg != null)
				this.handleMessage(msg);
			else
				Log.w(this.getClass().getName(), "msg=null");
		}
		
		if(this.tcp.open && 1000 < System.currentTimeMillis()-this.lastReceiveTime) {
			Log.w(this.getClass().getName(), "Client timed out!");
			this.tcp.close();
		}
		
		camera.addCallbackBuffer(data);
	}
	
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
//		Log.d(this.getClass().getName(), "onPreviewFrame()");
		new UpdateThread(this, data, camera).start();
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
