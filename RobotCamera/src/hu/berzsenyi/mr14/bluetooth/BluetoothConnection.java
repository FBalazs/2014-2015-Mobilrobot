package hu.berzsenyi.mr14.bluetooth;

import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothConnection {
	public BluetoothAdapter adapter = null;
	public BluetoothDevice device = null;
	public BluetoothSocket socket = null;
	
	public boolean connecting = false, open = false;
	
	public BluetoothConnection() {
		this.adapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	public BluetoothDevice getDeviceByName(String name) {
		for(BluetoothDevice device : this.adapter.getBondedDevices())
			if(device.getName().equals(name))
				return device;
		return null;
	}
	
	public void connect(String name) {
		this.connecting = true;
		try {
			if(!this.adapter.isEnabled())
				this.adapter.enable();
			this.device = this.getDeviceByName(name);
			this.socket = this.device.createInsecureRfcommSocketToServiceRecord(UUID.randomUUID());
			this.open = true;
		} catch(Exception e) {
			e.printStackTrace();
			this.close();
		}
	}
	
	public void close() {
		if(!this.open && !this.connecting)
			return;
		this.open = false;
		this.connecting = false;
		
		try {
			this.socket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
