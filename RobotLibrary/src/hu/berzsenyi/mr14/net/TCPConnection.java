package hu.berzsenyi.mr14.net;

import hu.berzsenyi.mr14.net.msg.MsgConnect;
import hu.berzsenyi.mr14.net.msg.MsgDisconnect;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPConnection implements IConnection {
	public static class TCPListenThread extends Thread {
		public TCPConnection connection;
		
		public TCPListenThread(TCPConnection connection) {
			super("Thread-TCPListen");
			this.connection = connection;
		}
		
		@Override
		public void run() {
			try {
				this.connection.serverSocket = new ServerSocket(this.connection.localPort);
				this.connection.socket = this.connection.serverSocket.accept();
				this.connection.remoteAddr = new InetSocketAddress(this.connection.socket.getInetAddress(), this.connection.socket.getPort());
				this.connection.onConnection();
			} catch(Exception e) {
				e.printStackTrace();
				this.connection.close();
			}
		}
	}
	
	public static class TCPConnectThread extends Thread {
		public TCPConnection connection;
		
		public TCPConnectThread(TCPConnection connection) {
			super("Thread-TCPConnect");
			this.connection = connection;
		}
		
		@Override
		public void run() {
			try {
				this.connection.socket = new Socket();
				this.connection.socket.bind(new InetSocketAddress(this.connection.localPort));
				this.connection.socket.connect(this.connection.remoteAddr);
				this.connection.onConnection();
			} catch(Exception e) {
				e.printStackTrace();
				this.connection.close();
			}
		}
	}
	
	public ServerSocket serverSocket;
	public Socket socket;
	
	public boolean connecting, open;
	public int localPort;
	public InetSocketAddress remoteAddr;
	public DataInputStream din;
	public DataOutputStream dout;
	
	public IConnectionListener listener;
	
	@Override
	public void setListener(IConnectionListener listener) {
		this.listener = listener;
	}
	
	public void listen(int port) {
		if(this.open)
			this.close();
		this.connecting = true;
		try {
			this.localPort = port;
			new TCPListenThread(this).start();
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
		}
	}
	
	public void connect(int localPort, String host, int port) {
		if(this.open)
			this.close();
		this.connecting = true;
		try {
			this.localPort = localPort;
			this.remoteAddr = new InetSocketAddress(host, port);
			new TCPConnectThread(this).start();
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
		}
	}
	
	public void onConnection() {
		this.open = true;
		this.connecting = false;
		try {
			this.din = new DataInputStream(this.socket.getInputStream());
			this.dout = new DataOutputStream(this.socket.getOutputStream());
			if(this.listener != null)
				this.listener.onConnected(this, this.remoteAddr);
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
		}
	}
	
	public int available() {
		try {
			return this.din.available();
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
			return -1;
		}
	}
	
	public TCPMessage readMsg() {
		try {
			if(this.din.available() < 8)
				return null;
			int type = this.din.readInt();
			int length = this.din.readInt();
			TCPMessage msg = null;
			switch(type) {
			case MsgConnect.TYPE:
				msg = new MsgConnect(length);
				break;
			case MsgDisconnect.TYPE:
				msg = new MsgDisconnect(length);
				break;
			}
			if(msg != null) {
				while(this.din.available() < length)
					Thread.sleep(10);
				msg.read(this.din);
			}
			return msg;
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
			return null;
		}
	}
	
	public void sendMsg(TCPMessage msg) {
		try {
			this.dout.writeInt(msg.type);
			this.dout.writeInt(msg.length);
			msg.write(this.dout);
			this.dout.flush();
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
		}
	}
	
	@Override
	public void close() {
		if(!this.open && !this.connecting)
			return;
		this.open = false;
		this.connecting = false;
		
		try {
			this.din.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			this.dout.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			this.socket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			if(this.serverSocket != null)
				this.serverSocket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(this.listener != null)
			this.listener.onDisconnected();
	}
}
