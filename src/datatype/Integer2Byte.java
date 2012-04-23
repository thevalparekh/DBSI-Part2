package datatype;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import heapManagement.Utilities;

public class Integer2Byte extends DataType {

	@Override
	public int compare(byte[] record1, int offset1, byte[] record2, int offset2, int length) {
		byte[] field1 = Arrays.copyOfRange(record1, offset1, offset1+length);
		byte[] field2 = Arrays.copyOfRange(record2, offset2, offset2+length);
		short integer1 = Utilities.toShort(field1);
		short integer2 = Utilities.toShort(field2);
		return integer1 == integer2 ? 0 : (integer1 > integer2 ? 1 : -1);
	}

	@Override
	public void read(ByteArrayOutputStream byteArrayOutputStream, byte[] data) throws IOException {
		short attr = Utilities.toShort(data);
		//short attr = java.lang.Short.parseShort(new String(data));
		String attribute = java.lang.Short.toString(attr);
		byteArrayOutputStream.write(attribute.getBytes());
	}

	@Override
	public void write(ByteArrayOutputStream byteArrayOutputStream, String attribute, int length) throws IOException {
		short attr = java.lang.Short.parseShort(attribute);
		//short attr = Utils.toShort(attribute.getBytes());
		byte[] byteAttr = Utilities.toByta(attr);
		byteArrayOutputStream.write(byteAttr);
	}

	@Override
	public int getHashCode(byte[] data) {
		Short dataAsShort = new Short(Utilities.toShort(data));
		return Math.abs(dataAsShort.hashCode());
	}

}
