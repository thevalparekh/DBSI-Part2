package heapManagement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

import datatype.DataType;
import datatype.HashIndexRecord;

public class HashIndex {
	private String fileName;
	private HashHeader hashHeader;
	private RandomAccessFile randomAccessFile;
	private char indexType;
	private int indexSize;
	private int attributeCode;
	DataType[] datatype;

	/*
	 * HashIndex constructor will also create the default hashHeader
	 * Input Datatype will be of form - c100 or i2 and so on...
	 */
	public HashIndex(String fileName, String datatype, DataType[] datatypes) throws FileNotFoundException {
		this.fileName = fileName;
		hashHeader = new HashHeader(datatype);
		hashHeader.setHeaderSize(20+datatype.length());
		this.datatype = datatypes;
		this.setIndexType(datatype);
		this.attributeCode = Utilities.getIntDatatypeCode(this.indexType,this.indexSize);
		this.openIndexFile();
		this.writeHashHeader(); //write the header to the hashindex file
		this.makeAndWriteFirstBucket();
	}

	private void setIndexType(String datatype){
		this.indexType = datatype.charAt(0);
		this.indexSize = Integer.parseInt(datatype.substring(1));
	}

	public void openIndexFile() throws FileNotFoundException {
		this.randomAccessFile = new RandomAccessFile(new File(this.fileName), "rw");
	}

	public int closeHeap(){
		try {
			randomAccessFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}

		return 0 ;
	}

	public boolean doesHeapFileExist() {
		File f = new File(fileName);
		return f.exists();
	}

	/*
	 * This function can be used to write the header for the first time 
	 * and also to update the header for the  Level , Next and FreeList
	 * 
	 * Format of the header:
	 *   1st 4 bytes - Size of the header
	 *   2nd 4 bytes - Level
	 *   3rd 4 bytes - Next Split Bucket
	 *   4th 4 bytes - Free List Bucket Head
	 *   5th rest of the bytes - Index Type
	 */
	public void writeHashHeader() {

		try{
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byteArrayOutputStream.write(Utilities.toByta(this.hashHeader.getHeaderSize()));
			byteArrayOutputStream.write(Utilities.toByta(this.hashHeader.getLevel()));
			byteArrayOutputStream.write(Utilities.toByta(this.hashHeader.getNext()));
			byteArrayOutputStream.write(Utilities.toByta(this.hashHeader.getFreeListBucketHead()));
			byteArrayOutputStream.write(this.hashHeader.getIndexType().getBytes());
			randomAccessFile.seek(0);
			randomAccessFile.write(byteArrayOutputStream.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void makeAndWriteFirstBucket() {
	
		try { 
		    byte[] bucket = new byte[Utilities.bucketSize]; 
		    int overFlowPointerSize = Utilities.overflowPointerSize;
		    Arrays.fill(bucket, 0, bucket.length-overFlowPointerSize, new Integer(0).byteValue());
			Arrays.fill(bucket, bucket.length-overFlowPointerSize, bucket.length, new Integer(-1).byteValue());
			randomAccessFile.seek(Utilities.bucketSize);
			randomAccessFile.write(bucket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int insertInhashIndex(HashIndexRecord record) {

		// get the corresponding bucket of the record
		
		int hashCode = datatype[attributeCode].getHashCode(record.getDataValue().getBytes());
		int bucketId = getIndexBucket(hashCode);

		try {
			
			int bucketSize = Utilities.bucketSize; 
			int overflowPointerSize = Utilities.overflowPointerSize;
			//get bucket for given bucketId from index file
			byte[] indexBucket = getBucketById(bucketId);
			
			boolean recordInsertedInBucket = insertBucketInHeap(indexBucket, record, bucketId);
			
		     if(!recordInsertedInBucket){
				
		    	byte[] overFlowPointer = Arrays.copyOfRange(indexBucket, bucketSize-overflowPointerSize, overflowPointerSize);
				int isOverFlowSet = checkOverflowPointerIsSet(overFlowPointer);
				
				if(isOverFlowSet == 1){
					
					while(!recordInsertedInBucket && isOverFlowSet == 1 ){
						int overFlowBucketId = Utilities.toInt(overFlowPointer);
						byte[] overFlowBucket = getBucketById(overFlowBucketId);
						recordInsertedInBucket = insertBucketInHeap(indexBucket, record, bucketId);
						if(!recordInsertedInBucket){
							
					    	overFlowPointer = Arrays.copyOfRange(indexBucket, bucketSize-overflowPointerSize, overflowPointerSize);
							isOverFlowSet = checkOverflowPointerIsSet(overFlowPointer);
						}
					}
					
					/*there can be 2 case to exit the while loop
					 *  -either there was no 
					 */
					
					
				} else { 
					//split 
				}
				//call for split
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}
	
	public boolean insertBucketInHeap(byte[]bucket, HashIndexRecord record, int bucketId) throws IOException {
		
		//check for space in the bucket for the given record
		boolean recordInsertedInBucket = false;
		int currentOffset = 0;
		int sizeOfRecord = 8 + indexSize;//8 -> RID 
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		while(currentOffset < Utilities.bucketSize-Utilities.overflowPointerSize){ //-8 for the OverFlowPointer
			
			byte[] byteRecord = Arrays.copyOfRange(bucket, currentOffset,currentOffset+sizeOfRecord);
			int isSpace = checkByteArrayIsAllZero(byteRecord); //drawback - if overflow pointer is 0.
		
			if(!recordInsertedInBucket && isSpace == 0){ //if RID == 0, this is the vacant space
				
				byte[] longRid = Utilities.toByta(record.getRecordId());
				byteArrayOutputStream.write(longRid);
				datatype[attributeCode].write(byteArrayOutputStream, record.getDataValue() , this.indexSize );
				// TODO insert overflow pointer
				recordInsertedInBucket = true;
			}
			else{
				byteArrayOutputStream.write(byteRecord);
			}
			currentOffset += sizeOfRecord;
		}
		//get the overflow pointer from the bucket
		byte[] overFlowPointer = Arrays.copyOfRange(bucket, Utilities.bucketSize-Utilities.overflowPointerSize, Utilities.overflowPointerSize);
		byteArrayOutputStream.write(overFlowPointer);//write overflow pointer to the bufferstream
		
		if(recordInsertedInBucket) {
				
			randomAccessFile.seek(Utilities.bucketSize + bucketId*Utilities.bucketSize);
			randomAccessFile.write(byteArrayOutputStream.toByteArray());
		}
		
		return recordInsertedInBucket;
	}
		
	
	public int checkOverflowPointerIsSet(byte[] overFlowPointer){
		
		for(int z = 0 ; z < overFlowPointer.length; z++){
			
			if(overFlowPointer[z] != -1){
				return 1;
			}
		}
		return 0;
		
	}
	
	public int checkByteArrayIsAllZero(byte[] bucket){
		
		for(int z = 0 ; z < bucket.length; z++){
			if(bucket[z] != 0){
				return -1;
			}
		}
		return 0;
	}
	
	public byte[] getBucketById(int bucketId) throws IOException {
		
		int bucketSize = Utilities.bucketSize;
		int bufferOffset = bucketSize + bucketId*bucketSize;
		byte[] bucket = new byte[bucketSize];
		randomAccessFile.seek(bufferOffset);
		randomAccessFile.read(bucket);
		return bucket;
	}

	public int getIndexBucket(int hashCode){

		int bucketId  = 0;
		int s = 2^hashHeader.getLevel();
		int totalbuckets = hashHeader.getNext() + s;
		bucketId = hashCode % (2*s);
		if (bucketId > totalbuckets - 1) {
			bucketId = bucketId - s;
		}
		return bucketId;
	}



}
