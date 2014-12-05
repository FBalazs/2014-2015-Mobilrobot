package hu.berzsenyi.mr14.camera;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.ParcelFileDescriptor;

public class CameraStreamer {
	public DatagramSocket socket;
	public Camera camera = null;
	public MediaRecorder rec = null;
	public boolean open;
	
	public void open(int localPort, InetSocketAddress remoteAddress) {
		try {
			this.open = true;
			this.socket = new DatagramSocket(localPort);
			this.socket.connect(remoteAddress);
			this.camera = Camera.open();
			this.camera.unlock();
			this.rec = new MediaRecorder();
			this.rec.setCamera(this.camera);
			this.rec.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
			this.rec.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
			this.rec.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
			this.rec.setOutputFile(ParcelFileDescriptor.fromDatagramSocket(socket).getFileDescriptor());
			this.rec.prepare();
			this.rec.start();
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
		}
	}
	
	public void close() {
		this.open = false;
		try {
			this.rec.stop();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			this.rec.release();
		} catch(Exception e) {
			e.printStackTrace();
		}
		this.rec = null;
		try {
			this.camera.release();
		} catch(Exception e) {
			e.printStackTrace();
		}
		this.camera = null;
		try {
			this.socket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
