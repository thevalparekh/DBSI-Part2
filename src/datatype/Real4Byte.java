package datatype;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import heapManagement.Utilities;

public class Real4Byte extends DataType {
	@Override
	public int compare(byte[] record1, int offset1, byte[] record2, int offset2, int length) {
		byte[] field1 = Arrays.copyOfRange(record1, offset1, offset1+length);
		byte[] field2 = Arrays.copyOfRange(record2, offset2, offset2+length);
		float real1 = Utilities.toFloat(field1);
		float real2 = Utilities.toFloat(field2);
		return real1 == real2 ? 0 : (real1 > real2 ? 1 : -1);
	}

	@Override	
	public void read(ByteArrayOutputStream byteArrayOutputStream, byte[] data) throws IOException {
		float attr = Utilities.toFloat(data);
		//float attr = java.lang.Float.parseFloat(new String(data));
		String attribute = java.lang.Float.toString(attr);
		byteArrayOutputStream.write(attribute.getBytes());
	}

	@Override
	public void write(ByteArrayOutputStream byteArrayOutputStream, String attribute, int length) throws IOException {
		float attr = java.lang.Float.parseFloat(attribute);
		//float attr = Utils.toFloat(attribute.getBytes());
		byte[] byteAttr = Utilities.toByta(attr);
		byteArrayOutputStream.write(byteAttr);
	}

	@Override
	public int getHashCode(byte[] data) {
		Float dataAsFloat = new Float(Utilities.toFloat(data));
		return Math.abs(dataAsFloat.hashCode());
	}


}
