package heapManagement;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import datatype.HashIndexRecord;

public class Bucket {
	
	public int bucketId;
	private int overflowPointer;
	private ArrayList<HashIndexRecord> recordList;
	private int bucketSize;
	public boolean isOverFlowBucket;
	public int nextOffset; // this is used during a split, which gives the offset to write the split record in this bucket
	public byte[] bucketData;
	
	public Bucket(int _bucketId, boolean isOverFlow) {
		recordList = new ArrayList<HashIndexRecord>();
		this.bucketId = _bucketId;
		this.nextOffset = 0;
		this.overflowPointer = -1; //by default it will point to the first bucket in the overflow file
	    this.bucketSize = Utilities.bucketSize;
	    this.isOverFlowBucket = isOverFlow;
	}
	
	public boolean isOverFlowBucket() {
		return isOverFlowBucket;
	}

	public void setOverFlowBucket(boolean isOverFlowBucket) {
		this.isOverFlowBucket = isOverFlowBucket;
	}

	public int getBucketId() {
		return bucketId;
	}

	public void setBucketId(int bucketId) {
		this.bucketId = bucketId;
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

	public int getOverflowPointer() {
		return overflowPointer;
	}

	public void setOverflowPointer(int overflowPointer) {
		this.overflowPointer = overflowPointer;
	}

	

	
		
}
