package hu.berzsenyi.mr14.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

public class UDPConnection implements IConnection {
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
	
	public DatagramSocket socket = null;
	
	public boolean connecting = false, open = false;
	
	public IConnectionListener listener = null;
	
	@Override
	public void setListener(IConnectionListener listener) {
		this.listener = listener;
	}
	
	public void connect(int localPort, InetSocketAddress remoteAddr) {
		this.connecting = true;
		try {
			this.socket = new DatagramSocket(localPort);
			this.socket.connect(remoteAddr);
			if(this.listener != null)
				this.listener.onConnected(this, remoteAddr);
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
		}
		this.open = true;
		this.connecting = false;
	}
	
	public DatagramPacket receive(byte[] buffer, int timeout) {
		try {
			DatagramPacket pkt = new DatagramPacket(buffer, buffer.length);
			this.socket.setSoTimeout(timeout);
			this.socket.receive(pkt);
			return pkt;
		} catch(Exception e) {
			if(!(e instanceof SocketTimeoutException))
				e.printStackTrace();
			return null;
		}
	}
	
	public void send(byte[] buffer, int offset, int length) {
		new UDPSendThread(this, buffer, offset, length).start();
	}
	
	@Override
	public void close() {
		if(!this.open && !this.connecting)
			return;
		this.open = false;
		this.connecting = false;
		
		try {
			this.socket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(this.listener != null)
			this.listener.onDisconnected();
	}
}
