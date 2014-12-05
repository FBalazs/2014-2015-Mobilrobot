package hu.berzsenyi.mr14.net.msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import hu.berzsenyi.mr14.net.TCPMessage;

public class MsgConnect extends TCPMessage {
	public static final int TYPE = 1;
	
	public MsgConnect() {
		super(TYPE, 0);
	}
	
	public MsgConnect(int length) {
		super(TYPE, length);
	}
	
	@Override
	public void read(DataInputStream din) throws Exception {
		
	}
	
	@Override
	public void write(DataOutputStream dout) throws Exception {
		
	}
}
