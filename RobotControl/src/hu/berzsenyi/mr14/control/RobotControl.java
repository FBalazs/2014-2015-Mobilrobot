package hu.berzsenyi.mr14.control;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import javax.imageio.ImageIO;

import hu.berzsenyi.mr14.net.IConnectionListener;
import hu.berzsenyi.mr14.net.TCPConnection;
import hu.berzsenyi.mr14.net.UDPConnection;
import hu.berzsenyi.mr14.net.msg.MsgConnect;

public class RobotControl implements Runnable, IConnectionListener {
	public boolean isRunning;
	public RobotDisplay display;
	public TCPConnection tcp = new TCPConnection();
	public UDPConnection udp = new UDPConnection();
	
	public void create() {
		System.out.println("create()");
		
		this.display = new RobotDisplay();
		
		this.tcp.setListener(this);
		this.tcp.connect(8080, "192.168.0.13", 8080);
	}
	
	@Override
	public void onConnected(InetSocketAddress remoteAddr) {
		System.out.println("onConnected()");
		
		this.udp.connect(8080, remoteAddr);
		this.tcp.sendMsg(new MsgConnect());
	}

	@Override
	public void onDisconnected() {
		System.out.println("onDisconnected()");
		
		this.tcp.close();
		this.udp.close();
	}
	
	byte[] netBuffer = new byte[60000];
	int pps = 0;
	long lastPPS = 0;
	BufferedImage imgCamera;
	
	public void update() {
//		System.out.println("update()");
		
		if(1000 <= System.currentTimeMillis()-this.lastPPS) {
			System.out.println("pps="+this.pps+" time="+(System.currentTimeMillis()-this.lastPPS));
			this.pps = 0;
			this.lastPPS = System.currentTimeMillis();
		}
		
		if(this.udp.open) {
			DatagramPacket pkt = this.udp.receive(this.netBuffer);
			this.pps++;
			try {
				ByteArrayInputStream bin = new ByteArrayInputStream(pkt.getData());
				this.imgCamera = ImageIO.read(bin);
				bin.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
//			System.out.println(pkt.getLength()+" "+pkt.getData().length);
		}
	}
	
	public void render() {
//		System.out.println("render()");
		
		if(this.imgCamera != null)
			this.display.update(this.imgCamera);
	}
	
	public void destroy() {
		System.out.println("destroy()");
		
		this.tcp.close();
		this.udp.close();
		
		System.exit(0);
	}
	
	@Override
	public void run() {
		this.create();
		this.isRunning = true;
		long time = System.currentTimeMillis();
		while(this.tcp.connecting && System.currentTimeMillis()-time < 1000)
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		while(this.isRunning && !this.display.shouldClose && this.tcp.open) {
			time = System.currentTimeMillis();
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
