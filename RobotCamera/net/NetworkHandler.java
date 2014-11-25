package hu.berzsenyi.robot.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class NetworkHandler {
	public InetSocketAddress remoteAddress;
	public Socket socketTcp;
	public ServerSocket socketTcpServer;
	public DatagramSocket socketUdp;
	
	public boolean open;
	public LinkedList<DatagramPacket> packetsReceived;
	
	public void connect(String host, int port) {
		try {
			this.open = true;
			this.remoteAddress = new InetSocketAddress(host, port);
			this.socketTcp = new Socket();
			this.socketTcp.connect(this.remoteAddress);
			this.socketUdp = new DatagramSocket(port);
			this.packetsReceived = new LinkedList<DatagramPacket>();
			new Thread("Thread-UDPReceive"){
				@Override
				public void run() {
					while(open) {
						try {
							DatagramPacket pkt = new DatagramPacket(new byte[32768], 32768);
							socketUdp.receive(pkt);
							if(pkt != null && pkt.getLength() != 0)
								synchronized (packetsReceived) {
									packetsReceived.add(pkt);
								}
						} catch(Exception e) {
							e.printStackTrace();
							if(open)
								close();
						}
					}
				}
			}.start();
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
		}
	}
	
	public void bind(int port) {
		try {
			this.open = true;
			this.socketTcpServer = new ServerSocket(port);
			this.socketTcpServer.setSoTimeout(0);
			this.socketTcp = null;
			new Thread("Thread-TCPListen") {
				@Override
				public void run() {
					try {
						while(open)
							if(socketTcp == null || !socketTcp.isConnected()) {
								socketTcp = socketTcpServer.accept();
								remoteAddress = new InetSocketAddress(socketTcp.getInetAddress(), socketTcp.getPort());
							} else
								Thread.sleep(1000);
					} catch(Exception e) {
						e.printStackTrace();
						if(open)
							close();
					}
				}
			}.start();
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
		}
	}
	
	public void send(byte[] data, boolean tcp) {
		try {
			if(tcp)
				this.socketTcp.getOutputStream().write(data);
			else
				this.socketUdp.send(new DatagramPacket(data.clone(), data.length, this.remoteAddress));
		} catch(Exception e) {
			e.printStackTrace();
			if(this.open)
				this.close();
		}
	}
	
	public void send(String str, boolean tcp) {
		this.send(str.getBytes(), tcp);
	}
	
	public int availableTcp() {
		try {
			return this.socketTcp.getInputStream().available();
		} catch(Exception e) {
			e.printStackTrace();
			if(this.open)
				this.close();
			return -1;
		}
	}
	
	public int availableUdp() {
		try {
			synchronized (this.packetsReceived) {
				return this.packetsReceived.size();
			}
		} catch(Exception e) {
			e.printStackTrace();
			if(this.open)
				this.close();
			return -1;
		}
	}
	
	public int readTcp() {
		try {
			return this.socketTcp.getInputStream().read();
		} catch(Exception e) {
			e.printStackTrace();
			if(this.open)
				this.close();
			return -1;
		}
	}
	
	public DatagramPacket readUdp() {
		try {
			return this.packetsReceived.poll();
		} catch(Exception e) {
			e.printStackTrace();
			if(this.open)
				this.close();
			return null;
		}
	}
	
	public void close() {
		this.open = false;
		this.remoteAddress = null;
		try {
			this.socketTcp.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			this.socketTcpServer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			this.socketUdp.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
