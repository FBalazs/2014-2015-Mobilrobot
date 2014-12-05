package hu.berzsenyi.mr14.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class TCPMessage {
	public int type, length;
	
	public TCPMessage(int type, int length) {
		this.type = type;
		this.length = length;
	}
	
	public void read(DataInputStream din) throws Exception {
		
	}
	
	public void write(DataOutputStream dout) throws Exception {
		
	}
}
