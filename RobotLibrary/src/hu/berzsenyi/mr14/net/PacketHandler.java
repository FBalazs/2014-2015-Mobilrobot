package hu.berzsenyi.mr14.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import hu.berzsenyi.mr14.net.packet.Packet;
import hu.berzsenyi.mr14.net.packet.PacketStreamData;
import hu.berzsenyi.mr14.net.packet.PacketStreamInfo;

public class PacketHandler {
	public void writePacket(Packet pkt, DataOutputStream out) throws IOException {
		out.writeInt(pkt.type);
		out.writeLong(pkt.id);
		out.writeInt(pkt.length);
		pkt.write(out);
	}
	
	public Packet readPacket(int type, long id, int length, DataInputStream in) throws IOException {
		Packet pkt = null;
		switch(type) {
		case PacketStreamInfo.TYPE:
			pkt = new PacketStreamInfo(id, length);
			break;
		case PacketStreamData.TYPE:
			pkt = new PacketStreamData(id, length);
			break;
		}
		if(pkt != null)
			pkt.read(in);
		return pkt;
	}
}
