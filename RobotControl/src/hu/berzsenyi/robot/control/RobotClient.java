package hu.berzsenyi.robot.control;

import hu.berzsenyi.robot.net.NetworkHandler;

public class RobotClient implements Runnable {
	boolean isRunning;
	NetworkHandler net;
	StringBuilder currentMsg;
	
	public void create() {
		System.out.println("create()");
		this.net = new NetworkHandler();
		this.net.connect("192.168.0.13", 8080);
		this.currentMsg = new StringBuilder();
	}
	
	public void processMsg(String msg) {
//		System.out.println("processMsg()");
		
	}
	
	public void update() {
//		System.out.println("update()");
		for(int i = 0; i < this.net.packetsReceived(); i++)
			this.net.pollPacket().handle();
	}
	
	public void render() {
//		System.out.println("render()");
		
	}
	
	public void destroy() {
		System.out.println("destroy()");
		this.net.close();
	}
	
	@Override
	public void run() {
		this.create();
		this.isRunning = true;
		while(this.isRunning && this.net.open) {
			long time = System.currentTimeMillis();
			this.update();
			this.render();
			time = 1000/100-(System.currentTimeMillis()-time);
			if(0 < time)
				try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		this.destroy();
	}
	
	public static void main(String[] args) {
		new Thread(new RobotClient(), "Thread-RobotClient").start();
	}
}
