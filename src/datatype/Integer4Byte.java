package datatype;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import heapManagement.Utilities;

public class Integer4Byte extends DataType{
	@Override
	public int compare(byte[] record1, int offset1, byte[] record2,	int offset2, int length) {
		byte[] field1 = Arrays.copyOfRange(record1, offset1, offset1+length);
		byte[] field2 = Arrays.copyOfRange(record2, offset2, offset2+length);
		int integer1 = Utilities.toInt(field1);
		int integer2 = Utilities.toInt(field2);
		return integer1 == integer2 ? 0 : (integer1 > integer2 ? 1 : -1);
	}

	@Override
	public void read(ByteArrayOutputStream byteArrayOutputStream, byte[] data) throws IOException {
		int attr = Utilities.toInt(data);
		String attribute = java.lang.Integer.toString(attr);
		byteArrayOutputStream.write(attribute.getBytes());
	}

	@Override
	public void write(ByteArrayOutputStream byteArrayOutputStream, String attribute, int length) throws IOException {
		int attr = java.lang.Integer.parseInt(attribute);
		byte[] byteAttr = Utilities.toByta(attr);
		byteArrayOutputStream.write(byteAttr);
	}

	@Override
	public int getHashCode(byte[] data) {
		Integer dataAsInt = new Integer(Utilities.toInt(data));
		return Math.abs(dataAsInt.hashCode());
	}
}
