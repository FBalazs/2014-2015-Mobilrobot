package hu.berzsenyi.robotcamera;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class NetworkTCPServer {
	public static interface IConnectionListener {
		public void onConnection(SocketAddress address);
	}
	
	public IConnectionListener listenerConnection = null;
	public ServerSocket serverSocket = null;
	public Socket socket = null;
	
	public void setConnectionListener(IConnectionListener listener) {
		this.listenerConnection = listener;
	}
	
	public void open(int port) {
		try {
			this.serverSocket = new ServerSocket(port);
			this.serverSocket.setSoTimeout(0);
			new Thread("Thread-TCPAccept") {
				@Override
				public void run() {
					try {
						socket = serverSocket.accept();
						if(listenerConnection != null)
							listenerConnection.onConnection(socket.getRemoteSocketAddress());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		} catch (Exception e) {
			e.printStackTrace();
			this.close();
			return;
		}
	}
	
	public boolean connected() {
		return this.socket != null && this.socket.isConnected();
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
		try {
			this.socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			this.serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
