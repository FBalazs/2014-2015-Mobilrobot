package hu.berzsenyi.mr14.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketStreamInfo extends Packet {
	public static final int TYPE = 1;
	
	public int width, height, parts;
	
	public PacketStreamInfo(long id, int length) {
		super(TYPE, id, length);
	}
	
	public PacketStreamInfo(long id, int width, int height, int parts) {
		super(TYPE, id, 12);
		this.width = width;
		this.height = height;
		this.parts = parts;
	}
	
	@Override
	public void read(DataInputStream in) throws IOException {
		this.width = in.readInt();
		this.height = in.readInt();
		this.parts = in.readInt();
	}
	
	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(this.width);
		out.writeInt(this.height);
		out.writeInt(this.parts);
	}
}
