package hu.berzsenyi.robot.net;

import java.net.InetSocketAddress;

public interface IClientConnectionListener {
	public void onClientConnected(InetSocketAddress address);
}
