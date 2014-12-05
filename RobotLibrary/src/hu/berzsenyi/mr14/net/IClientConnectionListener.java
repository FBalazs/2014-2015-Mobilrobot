package hu.berzsenyi.mr14.net;

import java.net.InetSocketAddress;

public interface IClientConnectionListener {
	public void onClientConnected(InetSocketAddress address);
}
