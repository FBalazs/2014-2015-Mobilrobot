package hu.berzsenyi.mr14.control;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import hu.berzsenyi.mr14.control.vision.RobotVision;
import hu.berzsenyi.mr14.net.IConnection;
import hu.berzsenyi.mr14.net.IConnectionListener;
import hu.berzsenyi.mr14.net.TCPConnection;
import hu.berzsenyi.mr14.net.TCPMessage;
import hu.berzsenyi.mr14.net.UDPConnection;
import hu.berzsenyi.mr14.net.msg.MsgConnect;
import hu.berzsenyi.mr14.net.msg.MsgDisconnect;
import hu.berzsenyi.mr14.net.msg.MsgStatus;
import hu.berzsenyi.mr14.net.msg.MsgSwitchPos;

public class RobotControl implements Runnable, IConnectionListener {
	public static final int PORT = 8080, TIMEOUT = 3000;
	
	public boolean isRunning;
	public RobotDisplay display;
	public TCPConnection tcp = new TCPConnection();
	public UDPConnection udp = new UDPConnection();
	public RobotVision vision;
	
	public byte[] netBuffer = new byte[60000];
	public int pps = 0;
	public long lastPPS = 0;
	public BufferedImage imgCamera = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
	public long lastStatus = 0, lastVideo = 0;
	
	public void create() {
		System.out.println("create()");
		this.display = new RobotDisplay(this);
		this.display.setTitle("Robot Control");
		
		this.tcp.setListener(this);
		this.tcp.connect(JOptionPane.showInputDialog(this.display, "Enter the IP address!", "192.168.43.2"), PORT);
		
		this.vision = new RobotVision();
	}
	
	@Override
	public void onConnected(IConnection connection, InetSocketAddress remoteAddr) {
		System.out.println("onConnected()");
		
		if(connection == this.tcp) {
			System.out.println("tcp");
			this.udp.setListener(this);
			this.udp.connect(this.tcp.localPort, remoteAddr);
			this.tcp.sendMsg(new MsgConnect());
		} else {
			System.out.println("udp");
			this.lastVideo = System.currentTimeMillis();
		}
	}

	@Override
	public void onDisconnected(IConnection connection) {
		System.out.println("onDisconnected()");
		if(!this.display.shouldClose && connection == this.tcp)
			JOptionPane.showMessageDialog(this.display, "Disconnected!");
	}
	
	public void handleMessage(TCPMessage msg) {
		System.out.println("msg.type="+msg.type+" msg.length="+msg.length);
		
		if(msg instanceof MsgSwitchPos) {
			synchronized (this.imgCamera) {
				this.vision.process2(this.imgCamera);
				// TODO send information to pick up the target
			}
		} else {
			System.err.println("Didn't handle tcp message!");
		}
	}
	
	public void update() {
//		System.out.println("update()");
		
		if(1000 <= System.currentTimeMillis()-this.lastPPS) {
			this.display.setTitle("Robot Control pps="+this.pps);
			this.pps = 0;
			this.lastPPS = System.currentTimeMillis();
		}
		
		if(this.tcp.open && TIMEOUT/10 <= System.currentTimeMillis()-this.lastStatus) {
			this.lastStatus = System.currentTimeMillis();
			this.tcp.sendMsg(new MsgStatus());
		}
		
		while(this.tcp.open && 8 <= this.tcp.available()) {
			TCPMessage msg = this.tcp.readMsg();
			if(msg != null)
				this.handleMessage(msg);
			else
				System.err.println("msg=null");
		}
		
		if(this.udp.open) {
			DatagramPacket pkt = this.udp.receive(this.netBuffer, 100);
			if(pkt != null) {
				this.lastVideo = System.currentTimeMillis();
				this.pps++;
				try {
					ByteArrayInputStream bin = new ByteArrayInputStream(pkt.getData());
					synchronized (this.imgCamera) {
						this.imgCamera = ImageIO.read(bin);
					}
					bin.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			} else if(TIMEOUT < System.currentTimeMillis()-this.lastVideo) {
				System.err.println("Timed out!");
				this.isRunning = false;
			}
		}
	}
	
	public void render() {
//		System.out.println("render()");
		
		if(this.imgCamera != null)
			this.display.update(this.imgCamera);
	}
	
	public void destroy() {
		System.out.println("destroy()");
		
		if(this.tcp.open)
			this.tcp.sendMsg(new MsgDisconnect());
		this.tcp.close();
		this.udp.close();
	}
	
	@Override
	public void run() {
		this.create();
		this.isRunning = true;
		long time = System.currentTimeMillis();
		while(this.tcp.connecting && System.currentTimeMillis()-time < 3000)
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
		System.exit(0);
	}
	
	public static void main(String[] args) {
		new Thread(new RobotControl(), "Thread-RobotClient").start();
	}
}
