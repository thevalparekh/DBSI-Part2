package hashManagement;

import heapManagement.Utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import datatype.DataType;

public class HashCursor {
	HashIndex index;
	DataType[] types;

	public HashCursor(HashIndex index, DataType[] types) {
		this.index = index;
		this.types = types;
	}

	public ArrayList<Long> getAllRecords(int type, byte[] conditionValue) throws IOException {
		int bucketSize = Utilities.bucketSize;
		int overflowPointerSize = Utilities.overflowPointerSize;

		ArrayList<Long> matchingRecords = new ArrayList<Long>();
		int hashValue = types[type].getHashCode(conditionValue);
		int indexBucket = index.getIndexBucket(hashValue);
		Bucket primaryBucket = new Bucket(indexBucket, false);

		while (true) {
			byte[] data = index.getBucketById(primaryBucket);
			byte[] overFlowPointer = Arrays.copyOfRange(data, bucketSize-overflowPointerSize, bucketSize);
			matchingRecords.addAll(addEntries(type, data, conditionValue));		
			boolean haveOverflow = index.checkOverflowPointerIsSet(overFlowPointer) == 1 ? true : false;
			if (haveOverflow)
				primaryBucket = new Bucket(Utilities.toInt(overFlowPointer), true);
			else
				break;
		}
		return matchingRecords;
	}

	private ArrayList<Long> addEntries(int type, byte[] bucket, byte[] conditionValue) {
		ArrayList<Long> rids = new ArrayList<Long>();
		int currentOffset = 0;
		int sizeOfRecord = 8 + index.getIndexSize();
		
		while(currentOffset + sizeOfRecord + Utilities.overflowPointerSize <= bucket.length) { 

			byte[] byteRecord = Arrays.copyOfRange(bucket, currentOffset, currentOffset+sizeOfRecord);
			int isSpace = index.checkByteArrayIsAllZero(byteRecord);
		
			if(isSpace == -1) {
				byte[] longRid = Arrays.copyOfRange(byteRecord, 0, 8);
				byte[] data = Arrays.copyOfRange(byteRecord, 8, byteRecord.length);
				int result = types[type].compare(data, 0, conditionValue, 0, index.getIndexSize());
				if (result == 0) {
					long rid = Utilities.toLong(longRid);
					rids.add(new Long(rid));
				}
			}
			else {
				break;
			}
			currentOffset += sizeOfRecord;
		}
		return rids;
	}	
}
