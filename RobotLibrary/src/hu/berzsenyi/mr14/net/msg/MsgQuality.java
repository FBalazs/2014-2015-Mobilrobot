package hu.berzsenyi.mr14.net.msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import hu.berzsenyi.mr14.net.TCPMessage;

public class MsgQuality extends TCPMessage {
	public static final int TYPE = 3;
	
	public byte quality;
	
	public MsgQuality(byte quality) {
		super(TYPE, 1);
		this.quality = quality;
	}
	
	public MsgQuality(int length) {
		super(TYPE, length);
	}
	
	@Override
	public void read(DataInputStream din) throws Exception {
		this.quality = din.readByte();
	}
	
	@Override
	public void write(DataOutputStream dout) throws Exception {
		dout.writeByte(this.quality);
	}
}
