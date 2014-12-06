package hu.berzsenyi.mr14.net;

public interface IConnection {
	public void setListener(IConnectionListener listener);
	public void close();
}
