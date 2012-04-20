package datatype;

import heapManagement.Compare;
import heapManagement.Read;
import heapManagement.Write;

public abstract class DataType implements Compare, Read, Write {
	public static final byte PAD = '0';

	public abstract int getHashCode(byte[] data);
}
