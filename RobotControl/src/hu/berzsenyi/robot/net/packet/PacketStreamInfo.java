package hu.berzsenyi.robot.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketStreamInfo extends Packet {
	public static final int TYPE = 1;
	
	public int width, height;
	
	public PacketStreamInfo(long id) {
		super(TYPE, id, 4+4);
	}
	
	public PacketStreamInfo(long id, int width, int height) {
		super(TYPE, id, 4+4);
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void read(DataInputStream in) throws IOException {
		this.width = in.readInt();
		this.height = in.readInt();
	}
	
	@Override
	public void write(DataOutputStream out) throws IOException {
		super.write(out);
		out.writeInt(this.width);
		out.writeInt(this.height);
	}
	
	@Override
	public void handle() {
		System.out.println("Stream resolution is "+this.width+"x"+this.height);
	}
}
