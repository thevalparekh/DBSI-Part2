package heapManagement;

public interface Compare {
	/* 
	 * record1 is the actual record from the heap file. record2 is input constant value, also
	 * stored as a byte array. offset2 in this application is always 0. This function however
	 * is generic and can used to compare records. This is why we pass in offset2 even though
	 * it is always zero in this Project 2.1
	 */
	public int compare(byte[] record1, int offset1, byte[] record2, int offset2, int length);
}
