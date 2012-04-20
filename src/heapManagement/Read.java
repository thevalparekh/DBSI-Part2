package heapManagement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface Read {
	public void read(ByteArrayOutputStream byteArrayOutputStream, byte[] data) throws IOException;
}
