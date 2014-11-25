package hu.berzsenyi.robot.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import hu.berzsenyi.robot.net.packet.Packet;
import hu.berzsenyi.robot.net.packet.PacketStreamInfo;

public class PacketHandler {
	public void writePacket(Packet pkt, DataOutputStream out) throws IOException {
		pkt.write(out);
	}
	
	public Packet readPacket(int type, long id, int length, DataInputStream in) throws IOException {
		Packet pkt = null;
		switch(type) {
		case PacketStreamInfo.TYPE:
			pkt = new PacketStreamInfo(id);
			break;
		}
		if(pkt != null)
			pkt.read(in);
		return pkt;
	}
}
