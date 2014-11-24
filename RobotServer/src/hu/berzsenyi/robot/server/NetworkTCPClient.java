package hu.berzsenyi.robot.server;

import java.net.InetSocketAddress;
import java.net.Socket;

public class NetworkTCPClient {
	public Socket socket;
	
	public void connect(String host, int port) {
		try {
			this.socket = new Socket();
			this.socket.setSoTimeout(0);
			this.socket.connect(new InetSocketAddress(host, port));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void send(byte[] data) {
		try {
			this.socket.getOutputStream().write(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void send(int oneByte) {
		try {
			this.socket.getOutputStream().write(oneByte);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int read() {
		try {
			return this.socket.getInputStream().read();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public int available() {
		try {
			return this.socket.getInputStream().available();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public void close() {
		if(this.socket != null)
			try {
				this.socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
