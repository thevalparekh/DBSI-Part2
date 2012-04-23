package datatype;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import heapManagement.Utilities;

public class Real8Byte extends DataType {

	@Override
	public int compare(byte[] record1, int offset1, byte[] record2, int offset2, int length) {
		byte[] field1 = Arrays.copyOfRange(record1, offset1, offset1+length);
		byte[] field2 = Arrays.copyOfRange(record2, offset2, offset2+length);
		double real1 = Utilities.toDouble(field1);
		double real2 = Utilities.toDouble(field2);
		return real1 == real2 ? 0 : (real1 > real2 ? 1 : -1);
	}

	@Override
	public void read(ByteArrayOutputStream byteArrayOutputStream, byte[] data) throws IOException {
		double attr = Utilities.toDouble(data);
		String attribute = java.lang.Double.toString(attr);
		byteArrayOutputStream.write(attribute.getBytes());
	}

	@Override
	public void write(ByteArrayOutputStream byteArrayOutputStream, String attribute, int length) throws IOException {
		double attr = java.lang.Double.parseDouble(attribute);
		byte[] byteAttr = Utilities.toByta(attr);
		byteArrayOutputStream.write(byteAttr);
	}

	@Override
	public int getHashCode(byte[] data) {
		Double dataAsDouble = new Double(Utilities.toDouble(data));
		return Math.abs(dataAsDouble.hashCode());
	}
	
}
