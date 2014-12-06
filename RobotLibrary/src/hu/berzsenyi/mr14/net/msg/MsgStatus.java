package hu.berzsenyi.mr14.net.msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import hu.berzsenyi.mr14.net.TCPMessage;

public class MsgStatus extends TCPMessage {
	public static final int TYPE = 4;
	
	public MsgStatus() {
		super(TYPE, 0);
	}
	
	public MsgStatus(int length) {
		super(TYPE, length);
	}
	
	@Override
	public void read(DataInputStream din) throws Exception {
		
	}
	
	@Override
	public void write(DataOutputStream dout) throws Exception {
		
	}
}
