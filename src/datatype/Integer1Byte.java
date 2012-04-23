package datatype;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Integer1Byte extends DataType {
	
	@Override
	public int compare(byte[] record1, int offset1, byte[] record2, int offset2, int length) {
		/* Byte[] to Integer 1 byte is not provided in the sample code so we do it manually */
		byte integer1 = record1[offset1];
		byte integer2 = record2[offset2];
		return integer1 == integer2 ? 0 : (integer1 > integer2 ? 1 : -1);
	}

	@Override
	public void read(ByteArrayOutputStream byteArrayOutputStream, byte[] data) throws IOException {
		byte attr = data[0];
		byteArrayOutputStream.write(attr);
	}

	@Override
	public void write(ByteArrayOutputStream byteArrayOutputStream, String attribute, int length) throws IOException {
		byte[] byteAttr = attribute.getBytes();
		/*
		if(byteAttr.length > 1){
			System.out.println("Invalid Data Format");
		}
		*/
		byteArrayOutputStream.write(byteAttr[0]);
	}

	@Override
	public int getHashCode(byte[] data) {
		Byte dataAsByte = new Byte(data[0]);
		return Math.abs(dataAsByte.hashCode());
	}

}
