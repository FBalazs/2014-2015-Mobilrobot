package hu.berzsenyi.robot.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.LinkedList;

public class NetworkUDPClient {
	public DatagramSocket socket = null;
	public LinkedList<DatagramPacket> packetsReceived;
	
	public void connect(String host, int port) {
		try {
			this.packetsReceived = new LinkedList<DatagramPacket>();
			this.socket = new DatagramSocket(new InetSocketAddress(host, port));
			new Thread("Thread-UDPReceive") {
				@Override
				public void run() {
					try {
						while(true) {
							DatagramPacket packet = new DatagramPacket(new byte[32767], 32767);
							socket.receive(packet);
							synchronized (packetsReceived) {
								packetsReceived.add(packet);
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int available() {
		synchronized (this.packetsReceived) {
			return this.packetsReceived.size();
		}
	}
	
	public DatagramPacket receive() {
		synchronized (this.packetsReceived) {
			return this.packetsReceived.poll();
		}
	}
	
	public void send(byte[] data) {
		try {
			this.socket.send(new DatagramPacket(data, data.length, this.socket.getRemoteSocketAddress()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			this.socket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
