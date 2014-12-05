package hu.berzsenyi.mr14.net;

import java.net.InetSocketAddress;

public interface IConnectionListener {
	public void onConnected(InetSocketAddress remoteAddr);
	public void onDisconnected();
}
