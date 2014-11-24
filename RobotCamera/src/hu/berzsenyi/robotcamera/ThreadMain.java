package hu.berzsenyi.robotcamera;

import android.hardware.Camera;
import android.util.Log;

public class ThreadMain extends Thread {
	Camera camera;
	public boolean isRunning;
	long currentTime, lastTickTime;
	
	public ThreadMain(Camera camera) {
		this.camera = camera;
	}
	
	@Override
	public void run() {
		this.isRunning = true;
		while(this.isRunning) {
			this.lastTickTime = this.currentTime;
			this.currentTime = System.currentTimeMillis();
			
			Log.d("ThreadMain.java", "hali!");
			
			long sleep = 1000/1-(this.currentTime-this.lastTickTime);
			if(0 < sleep)
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					Log.e("ThreadMain.java", e.toString());
					e.printStackTrace();
				}
		}
	}
}
