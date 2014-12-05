package hu.berzsenyi.mr14.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketStreamData extends Packet {
	public static final int TYPE = 2;
	
	public int x, y, w, h;
	public byte[] data;
	
	public PacketStreamData(long id, int length) {
		super(TYPE, id, length);
	}
	
	public PacketStreamData(long id, int x, int y, int w, int h, byte[] data) {
		super(TYPE, id, 16+data.length);
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.data = data;
	}
	
	@Override
	public void read(DataInputStream in) throws IOException {
		this.x = in.readInt();
		this.y = in.readInt();
		this.w = in.readInt();
		this.h = in.readInt();
		this.data = new byte[this.length-16];
		in.read(this.data);
	}
	
	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(this.x);
		out.writeInt(this.y);
		out.writeInt(this.w);
		out.writeInt(this.h);
		out.write(this.data);
	}
}
