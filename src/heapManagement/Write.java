package heapManagement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public interface Write {
	
	public void write(ByteArrayOutputStream byteArrayOutputStream,String attribute, int length) throws IOException;
}
