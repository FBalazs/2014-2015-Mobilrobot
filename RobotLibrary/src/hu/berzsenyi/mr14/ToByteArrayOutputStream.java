package hu.berzsenyi.mr14;

import java.io.IOException;
import java.io.OutputStream;

public class ToByteArrayOutputStream extends OutputStream {
	public byte[] buffer;
	public int pos;
	
	public ToByteArrayOutputStream(byte[] buffer) {
		this.buffer = buffer;
		this.pos = 0;
	}
	
	public int size() {
		return this.pos;
	}

	@Override
	public void write(int b) throws IOException {
		this.buffer[this.pos++] = (byte)b;
	}
}
