package datatype;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import heapManagement.Utilities;

public class Integer8Byte extends DataType {

	@Override
	public int compare(byte[] record1, int offset1, byte[] record2, int offset2, int length) {
		byte[] field1 = Arrays.copyOfRange(record1, offset1, offset1+length);
		byte[] field2 = Arrays.copyOfRange(record2, offset2, offset2+length);
		long integer1 = Utilities.toLong(field1);
		long integer2 = Utilities.toLong(field2);
		return integer1 == integer2 ? 0 : (integer1 > integer2 ? 1 : -1);
	}

		
	@Override
	public void read(ByteArrayOutputStream byteArrayOutputStream, byte[] data) throws IOException {
		long attr = Utilities.toLong(data);
		String attribute = java.lang.Long.toString(attr);
		byteArrayOutputStream.write(attribute.getBytes());
	}
	
	@Override
	public void write(ByteArrayOutputStream byteArrayOutputStream, String attribute, int length) throws IOException {
		long attr = java.lang.Long.parseLong(attribute);
		byte[] byteAttr = Utilities.toByta(attr);
		byteArrayOutputStream.write(byteAttr);
	}


	@Override
	public int getHashCode(byte[] data) {
		Long dataAsLong = new Long(Utilities.toLong(data));
		return Math.abs(dataAsLong.hashCode());
	}

	
}
