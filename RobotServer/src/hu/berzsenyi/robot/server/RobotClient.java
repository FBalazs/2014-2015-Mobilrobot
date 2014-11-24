package hu.berzsenyi.robot.server;

import java.net.DatagramPacket;

public class RobotClient implements Runnable {
	boolean isRunning;
	NetworkTCPClient netTCP;
	NetworkUDPClient netUDP;
	StringBuilder currentMsg;
	
	public void create() {
		System.out.println("create()");
		this.netTCP = new NetworkTCPClient();
		this.netTCP.connect("192.168.0.13", 8080);
		this.netUDP = new NetworkUDPClient();
		this.netUDP.connect("192.168.0.13", 8080);
		this.currentMsg = new StringBuilder();
	}
	
	public void processMsg(String msg) {
		System.out.println("processMsg()");
//		String cmd = msg.substring(0, msg.indexOf(' '));
		
	}
	
	public void update() {
		System.out.println("update()");
		while(0 < this.netTCP.available()) {
			int b = this.netTCP.read();
			if(b == (byte)'\n') {
				this.processMsg(this.currentMsg.toString());
				this.currentMsg = new StringBuilder();
			}
			else
				this.currentMsg.append(b);
		}
		while(0 < this.netUDP.available()) {
			DatagramPacket pkt = this.netUDP.receive();
			System.out.println("udp "+pkt.getLength());
		}
	}
	
	public void render() {
		System.out.println("render()");
		
	}
	
	public void destroy() {
		System.out.println("destroy()");
		this.netTCP.close();
		this.netUDP.close();
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
