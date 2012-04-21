package heapManagement;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import datatype.HashIndexRecord;

public class Bucket {
	
	private int bucketId;
	private long overflowPointer;
	private ArrayList<HashIndexRecord> recordList;
	private int bucketSize;
	
	public Bucket(int _bucketId) {
		recordList = new ArrayList<HashIndexRecord>();
		this.bucketId = _bucketId;
		this.overflowPointer = -1; //by default it will point to the first bucket in the overflow file
	    this.bucketSize = Utilities.bucketSize;
	}
	
	public int getBucketId() {
		return bucketId;
	}

	public void setBucketId(int bucketId) {
		this.bucketId = bucketId;
	}

	public long getOverflowPointer() {
		return overflowPointer;
	}

	public void setOverflowPointer(long overflowPointer) {
		this.overflowPointer = overflowPointer;
	}

	public ArrayList<HashIndexRecord> getRecordList() {
		return recordList;
	}

	public void setRecordList(ArrayList<HashIndexRecord> recordList) {
		this.recordList = recordList;
	}

	public int getBucketSize() {
		return bucketSize;
	}

	public void setBucketSize(int bucketSize) {
		this.bucketSize = bucketSize;
	}

	
		
}
