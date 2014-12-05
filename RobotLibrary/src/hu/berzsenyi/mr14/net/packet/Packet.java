package hu.berzsenyi.mr14.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet {
	public int type, length;
	public long id;
	
	public Packet(int type, long id, int length) {
		this.type = type;
		this.id = id;
		this.length = length;
	}
	
	public void read(DataInputStream in) throws IOException {
		
	}
	
	public void write(DataOutputStream out) throws IOException {
		
	}
}
