package hu.berzsenyi.robot.control;

import hu.berzsenyi.robot.net.NetworkHandler;

import java.net.DatagramPacket;

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
		while(0 < this.net.availableTcp()) {
			int b = this.net.readTcp();
			if(b == (byte)'\n') {
				this.processMsg(this.currentMsg.toString());
				this.currentMsg = new StringBuilder();
			}
			else
				this.currentMsg.append(b);
		}
		while(0 < this.net.availableUdp()) {
			DatagramPacket pkt = this.net.readUdp();
			
//			System.out.println("udp "+pkt.getLength());
		}
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
		while(this.isRunning) {
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
