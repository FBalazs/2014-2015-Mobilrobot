package hu.berzsenyi.robotcamera;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ActivityMain extends Activity {
	Camera camera;
	ThreadMain thread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("ActivityMain.java", "onCreate()");
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
	}
	
	@Override
	protected void onStart() {
		Log.d("ActivityMain.java", "onStart()");
		super.onStart();
		this.camera = Camera.open();
		if(this.camera == null) {
			Log.e("ActivityMain.java", "Couldn't open camera!");
			Toast.makeText(this, "Couldn't open camera!", Toast.LENGTH_LONG).show();
			this.finish();
		} else {
			this.camera.lock();
			this.thread = new ThreadMain(this.camera);
			this.thread.start();
		}
	}
	
	@Override
	protected void onStop() {
		Log.d("ActivityMain.java", "onStop()");
		super.onStop();
		if(this.thread != null) {
			this.thread.isRunning = false;
			try {
				this.thread.join();
			} catch (InterruptedException e) {
				Log.e("ActivityMain.java", e.toString());
				e.printStackTrace();
			}
		}
		if(this.camera != null) {
			this.camera.unlock();
			this.camera.release();
		}
	}
	
	@Override
	protected void onDestroy() {
		Log.d("ActivityMain.java", "onDestroy()");
		super.onDestroy();
	}
}
