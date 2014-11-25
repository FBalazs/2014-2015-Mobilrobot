package hu.berzsenyi.robot.net;

import hu.berzsenyi.robot.net.packet.Packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class NetworkHandler {
	public static class TCPListenThread extends Thread {
		public NetworkHandler net;
		
		public TCPListenThread(NetworkHandler net) {
			super("Thread-TCPListen");
			this.net = net;
		}
		
		@Override
		public void run() {
			try {
				while(this.net.open)
					if(this.net.socketTcp == null || !this.net.socketTcp.isConnected()) {
						this.net.socketTcp = this.net.socketTcpServer.accept();
						if(this.net.remoteAddress == null && this.net.socketTcp != null)
							this.net.remoteAddress = new InetSocketAddress(this.net.socketTcp.getInetAddress(), this.net.socketTcp.getPort());
						if(this.net.socketTcp != null) {
							this.net.tcpIn = new DataInputStream(this.net.socketTcp.getInputStream());
							this.net.tcpOut = new DataOutputStream(this.net.socketTcp.getOutputStream());
							new TCPReceiveThread(this.net).start();
							if(this.net.listenerClientConnection != null)
								this.net.listenerClientConnection.onClientConnected(this.net.remoteAddress);
						}
					}
					else
						Thread.sleep(1000);
			} catch(Exception e) {
				e.printStackTrace();
				if(this.net.open)
					this.net.close();
			}
		}
	}
	
	public static class TCPReceiveThread extends Thread {
		public NetworkHandler net;
		
		public TCPReceiveThread(NetworkHandler net) {
			super("Thread-TCPReceive");
			this.net = net;
		}
		
		@Override
		public void run() {
			while(this.net.open) {
				try {
					while(this.net.socketTcp.getInputStream().available() < 4+8+4);
					int type = this.net.tcpIn.readInt();
					long id = this.net.tcpIn.readLong();
					int length = this.net.tcpIn.readInt();
					while(this.net.socketTcp.getInputStream().available() < length);
//					byte[] data = new byte[length];
//					this.net.tcpIn.read(data);
					synchronized (this.net.packetsReceived) {
						this.net.packetsReceived.add(this.net.packetHandler.readPacket(type, id, length, this.net.tcpIn));
					}
				} catch(Exception e) {
					e.printStackTrace();
					if(this.net.open)
						this.net.close();
				}
			}
		}
	}
	
	public static class UDPReceiveThread extends Thread {
		public NetworkHandler net;
		
		public UDPReceiveThread(NetworkHandler net) {
			super("Thread-UDPReceive");
			this.net = net;
		}
		
		@Override
		public void run() {
			while(this.net.open) {
				try {
					DatagramPacket pkt = new DatagramPacket(new byte[32768], 32768);
					this.net.socketUdp.receive(pkt);
					if(pkt != null && pkt.getLength() != 0) {
						if(this.net.remoteAddress == null) {
							this.net.remoteAddress = new InetSocketAddress(pkt.getAddress(), pkt.getPort());
							if(this.net.listenerClientConnection != null)
								this.net.listenerClientConnection.onClientConnected(this.net.remoteAddress);
						}
						ByteArrayInputStream bin = new ByteArrayInputStream(pkt.getData());
						DataInputStream din = new DataInputStream(bin);
						synchronized (this.net.packetsReceived) {
							this.net.packetsReceived.add(this.net.packetHandler.readPacket(din.readInt(), din.readLong(), din.readInt(), din));
						}
						din.close();
						bin.close();
					}
				} catch(Exception e) {
					e.printStackTrace();
					if(this.net.open)
						this.net.close();
				}
			}
		}
	}
	
	public static class UDPSendThread extends Thread {
		public NetworkHandler net;
		public Packet pkt;
		
		public UDPSendThread(NetworkHandler net, Packet pkt) {
			super("Thread-UDPSend");
			this.net = net;
			this.pkt = pkt;
		}
		
		@Override
		public void run() {
			try {
				ByteArrayOutputStream bout = new ByteArrayOutputStream(4+8+4+this.pkt.length);
				DataOutputStream dout = new DataOutputStream(bout);
				this.net.packetHandler.writePacket(this.pkt, dout);
				byte[] data = bout.toByteArray();
				dout.close();
				bout.close();
				this.net.socketUdp.send(new DatagramPacket(data, data.length, this.net.remoteAddress));
			} catch(Exception e) {
				e.printStackTrace();
				if(this.net.open)
					this.net.close();
			}
		}
	}
	
	public InetSocketAddress remoteAddress;
	public Socket socketTcp;
	public DataInputStream tcpIn;
	public DataOutputStream tcpOut;
	public ServerSocket socketTcpServer;
	public DatagramSocket socketUdp;
	
	public boolean open;
	public LinkedList<Packet> packetsReceived;
	public PacketHandler packetHandler = new PacketHandler();
	
	public IClientConnectionListener listenerClientConnection;
	
	public void setClientConnectionListener(IClientConnectionListener listener) {
		this.listenerClientConnection = listener;
	}
	
	public void connect(String host, int port) {
		try {
			this.open = true;
			this.remoteAddress = new InetSocketAddress(host, port);
			this.socketTcp = new Socket();
			this.socketTcp.bind(new InetSocketAddress(port));
			this.socketTcp.connect(this.remoteAddress);
			this.tcpIn = new DataInputStream(this.socketTcp.getInputStream());
			this.tcpOut = new DataOutputStream(this.socketTcp.getOutputStream());
			this.socketUdp = new DatagramSocket(port);
			this.packetsReceived = new LinkedList<Packet>();
			new TCPReceiveThread(this).start();
			new UDPReceiveThread(this).start();
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
			this.remoteAddress = null;
			new TCPListenThread(this).start();
			this.socketUdp = new DatagramSocket(port);
			this.packetsReceived = new LinkedList<Packet>();
			new UDPReceiveThread(this).start();
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
		}
	}
	
	public void send(Packet pkt, boolean tcp) {
		try {
			if(tcp)
				this.packetHandler.writePacket(pkt, this.tcpOut);
			else
				new UDPSendThread(this, pkt).start();
		} catch(Exception e) {
			e.printStackTrace();
			if(this.open)
				this.close();
		}
	}
	
	public int packetsReceived() {
		synchronized (this.packetsReceived) {
			return this.packetsReceived.size();
		}
	}
	
	public Packet pollPacket() {
		synchronized (this.packetsReceived) {
			return this.packetsReceived.poll();
		}
	}
	
	public void close() {
		this.open = false;
		this.remoteAddress = null;
		try {
			this.tcpIn.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			this.tcpOut.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
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
