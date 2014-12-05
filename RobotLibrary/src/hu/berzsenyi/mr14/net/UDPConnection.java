package hu.berzsenyi.mr14.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class UDPConnection {
	public static class UDPSendThread extends Thread {
		public UDPConnection connection;
		public byte[] buffer;
		public int offset, length;
		
		public UDPSendThread(UDPConnection connection, byte[] buffer, int offset, int length) {
			super("Thread-UDPSend");
			this.connection = connection;
			this.buffer = buffer;
			this.offset = offset;
			this.length = length;
		}
		
		@Override
		public void run() {
			try {
				this.connection.socket.send(new DatagramPacket(this.buffer,  this.offset, this.length));
			} catch(Exception e) {
				e.printStackTrace();
				this.connection.close();
			}
		}
	}
	
	public DatagramSocket socket;
	
	public boolean open;
	
	public IConnectionListener listener;
	
	public void setListener(IConnectionListener listener) {
		this.listener = listener;
	}
	
	public void connect(int localPort, InetSocketAddress remoteAddr) {
		try {
			this.socket = new DatagramSocket(localPort);
			this.socket.connect(remoteAddr);
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
		}
		this.open = true;
	}
	
	public DatagramPacket receive(byte[] buffer) {
		try {
			DatagramPacket pkt = new DatagramPacket(buffer, buffer.length);
			this.socket.receive(pkt);
			return pkt;
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
			return null;
		}
	}
	
	public void send(byte[] buffer, int offset, int length) {
		new UDPSendThread(this, buffer, offset, length).start();
	}
	
	public void close() {
		if(!this.open)
			return;
		try {
			this.socket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		this.open = false;
		if(this.listener != null)
			this.listener.onDisconnected();
	}
}
