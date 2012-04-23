package datatype;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class CharacterXByte extends DataType {

	@Override
	public int compare(byte[] record1, int offset1, byte[] record2, int offset2, int length) {
		/* 
		 * Compares the byte arrays position by position, including pad characters 
		 * Returns 0 if the two fields being compared are equal. 1 if record1 is 
		 * lexicographically greater than record2. -1 otherwise.
		 */
		int i = 0;
		while (record1[offset1+i] == record2[offset2+i]) {
			i++;
			/* Reached end and record1 field was equal to record2 field */
			if (i >= length)
				return 0;
		}
		return record1[offset1+i] > record2[offset2+i] ? 1 : -1;
	}

	@Override
	public void read(ByteArrayOutputStream byteArrayOutputStream, byte[] data) throws IOException {
		for(int i = 0; i < data.length; i++) {
			if(data[i] == PAD) {
				break;
			}
			byteArrayOutputStream.write(data[i]);
		}
	}

	@Override
	public void write(ByteArrayOutputStream byteArrayOutputStream, String attribute, int length) throws IOException {
		byte[] byteAttr = new byte[length];
		Arrays.fill(byteAttr, PAD);
		
		byte[] recordByte = attribute.getBytes();
		for(int j = 0; j < recordByte.length; j++) {
			byteAttr[j] = recordByte[j];
		}
		byteArrayOutputStream.write(byteAttr);
	}
	
	@Override
	public int getHashCode(byte[] data) {
		String dataAsString = new String(data);
		return Math.abs(dataAsString.hashCode());
	}
}
