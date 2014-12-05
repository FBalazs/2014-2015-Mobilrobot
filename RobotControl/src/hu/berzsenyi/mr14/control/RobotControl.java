package hu.berzsenyi.mr14.control;

import hu.berzsenyi.mr14.control.display.RobotDisplay;
import hu.berzsenyi.mr14.net.NetworkHandler;
import hu.berzsenyi.mr14.net.packet.Packet;

public class RobotControl implements Runnable {
	boolean isRunning;
	NetworkHandler net;
	RobotDisplay display;
	int streamParts = 0;
	
	public void create() {
		System.out.println("create()");
		this.net = new NetworkHandler();
		this.net.connect("192.168.0.13", 8080);
	}
	
	public void handlePacket(Packet pkt) {
		System.out.println("type="+pkt.type+" id="+pkt.id+" length="+pkt.length);
	}
	
	public void update() {
//		System.out.println("update()");
		while(0 < this.net.packetsReceived())
			this.handlePacket(this.net.pollPacket());
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
		new Thread(new RobotControl(), "Thread-RobotClient").start();
	}
}
