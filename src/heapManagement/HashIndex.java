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
	private String indexFileName;
	private String overFlowFileName;
	private HashHeader hashHeader;
	private RandomAccessFile indexRandomAccessFile;
	private RandomAccessFile overFlowRandomAccessFile;
	private char indexType;
	private int indexSize;
	private int attributeCode;
	DataType[] datatype;

	/*
	 * HashIndex constructor will also create the default hashHeader
	 * Input Datatype will be of form - c100 or i2 and so on...
	 */
	public HashIndex(String indexFileName, String overFlowFileName, String datatype, DataType[] datatypes) throws IOException {
		this.indexFileName = indexFileName;
		this.overFlowFileName = overFlowFileName;
		hashHeader = new HashHeader(datatype);
		hashHeader.setHeaderSize(20+datatype.length());
		this.datatype = datatypes;
		this.setIndexType(datatype);
		this.attributeCode = Utilities.getIntDatatypeCode(this.indexType,this.indexSize);
		this.openIndexFile();
		this.openOverFlowFile();
		byte[] firstBucket = this.initializeBucket();
		indexRandomAccessFile.seek(0);
		indexRandomAccessFile.write(firstBucket);
	}

	private void setIndexType(String datatype){
		this.indexType = datatype.charAt(0);
		this.indexSize = Integer.parseInt(datatype.substring(1));
	}
	
	public void openOverFlowFile() throws FileNotFoundException {
		this.overFlowRandomAccessFile = new RandomAccessFile(new File(this.overFlowFileName), "rw");
		this.writeHashHeader(); //write the header to the hashindex file
	}

	public void openIndexFile() throws FileNotFoundException {
		this.indexRandomAccessFile = new RandomAccessFile(new File(this.indexFileName), "rw");
	}

	public int closeHeap(){
		try {
			indexRandomAccessFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}

		return 0 ;
	}

	public boolean doesHeapFileExist() {
		File f = new File(indexFileName);
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
	 *   5th 4 bytes - Next Bucket Id
	 *   5th rest of the bytes - Index Type
	 */
	public void writeHashHeader() {

		try{
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byteArrayOutputStream.write(Utilities.toByta(this.hashHeader.getHeaderSize()));
			byteArrayOutputStream.write(Utilities.toByta(this.hashHeader.getLevel()));
			byteArrayOutputStream.write(Utilities.toByta(this.hashHeader.getNext()));
			byteArrayOutputStream.write(Utilities.toByta(this.hashHeader.getFreeListBucketHead()));
			byteArrayOutputStream.write(Utilities.toByta(this.hashHeader.getNextBucketId()));
			byteArrayOutputStream.write(this.hashHeader.getIndexType().getBytes());
			indexRandomAccessFile.seek(0);
			indexRandomAccessFile.write(byteArrayOutputStream.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public byte[] initializeBucket() {

		    byte[] bucket = new byte[Utilities.bucketSize]; 
		    int overFlowPointerSize = Utilities.overflowPointerSize;
		    Arrays.fill(bucket, 0, bucket.length-overFlowPointerSize, new Integer(0).byteValue());
			Arrays.fill(bucket, bucket.length-overFlowPointerSize, bucket.length, new Integer(-1).byteValue());
			return bucket;		
	}

	public int insertInhashIndex(HashIndexRecord record) {

		// get the corresponding bucket of the record
		
		int hashCode = datatype[attributeCode].getHashCode(record.getDataValue());
		int bucketId = getIndexBucket(hashCode);

		try {
			
			int bucketSize = Utilities.bucketSize; 
			int overflowPointerSize = Utilities.overflowPointerSize;
			//get bucket for given bucketId from index file
			byte[] indexBucket = getBucketById(bucketId);
			Bucket indexFileBucket = new Bucket(bucketId, false);
			boolean recordInsertedInBucket = insertBucketInFile(indexBucket, record,indexFileBucket, false); //last parameter is free bucket insert
			
		     if(!recordInsertedInBucket){
				
		    	byte[] overFlowPointer = Arrays.copyOfRange(indexBucket, bucketSize-overflowPointerSize, overflowPointerSize);
				int isOverFlowSet = checkOverflowPointerIsSet(overFlowPointer);
				
				if(isOverFlowSet == 1){
					
					while(!recordInsertedInBucket && isOverFlowSet == 1 ){
						int overFlowBucketId = Utilities.toInt(overFlowPointer);
						byte[] overFlowBucket = getBucketById(overFlowBucketId);
						Bucket overFlowFileBucket = new Bucket(overFlowBucketId, true);
						recordInsertedInBucket = insertBucketInFile(indexBucket, record, overFlowFileBucket, false); //last parameter is free bucket insert
						
					    overFlowPointer = Arrays.copyOfRange(indexBucket, bucketSize-overflowPointerSize, overflowPointerSize);
						isOverFlowSet = checkOverflowPointerIsSet(overFlowPointer);
					}
					
					/*there can be 2 case to exit the while loop
					 *  -either insert was done
					 *  - or no overflow avialable
					 */
					if(recordInsertedInBucket){
						return 1; //record inserted in overflow bucket
					}
					
					//the overflow pointer is not set... so its split
										
					
					
				} else { 
					//if there are no overflow bucket
					
					//split 
					
				}
				//call for split
			} else {
				//record inserted in the index file
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	public int insertInFreeBucket(HashIndexRecord record) throws IOException{
		
		int freeBucketHead  = this.hashHeader.getFreeListBucketHead();
		
		if(freeBucketHead != -1) {
			
			byte[] indexBucket = getBucketById(freeBucketHead);
			Bucket overFlowFileBucket = new Bucket(freeBucketHead, true);
			boolean recordInsertedInBucket = insertBucketInFile(indexBucket, record, overFlowFileBucket,true);
			
			//for debugging
			if(!recordInsertedInBucket) {
				System.out.println( "There is some problem in insertion to free bucket in overflow file");
			}
			
		} else {
			//create a new bucket
			initializeBucket();
		}
		
		return 0;
	}
	
	
	
	public boolean insertBucketInFile(byte[]bucket, HashIndexRecord record, Bucket inputBucket, boolean freeBucket) throws IOException {
		
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
				byteArrayOutputStream.write(record.getDataValue());
				//datatype[attributeCode].write(byteArrayOutputStream, record.getDataValue() , this.indexSize );
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
		
		if(freeBucket) {
			
			// if its a free bucket make the free list head to next  overflow pointer
			// then make the  next overflow pointer to -1 
			this.hashHeader.setFreeListBucketHead(Utilities.toInt(overFlowPointer));
			byte[] overFlowTemp = new byte[Utilities.overflowPointerSize]; 
			Arrays.fill(overFlowTemp, new Integer(-1).byteValue());
			byteArrayOutputStream.write(overFlowPointer);
		} else {
			byteArrayOutputStream.write(overFlowPointer);//write overflow pointer to the bufferstream
		}
		
		
		if(recordInsertedInBucket) {
			
			if(inputBucket.isOverFlowBucket) {
				
				indexRandomAccessFile.seek(Utilities.bucketSize + inputBucket.bucketId*Utilities.bucketSize); //header size  + offset
				indexRandomAccessFile.write(byteArrayOutputStream.toByteArray());
				
			} else  {
				indexRandomAccessFile.seek(inputBucket.bucketId*Utilities.bucketSize);
				indexRandomAccessFile.write(byteArrayOutputStream.toByteArray());
			}
			
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
		indexRandomAccessFile.seek(bufferOffset);
		indexRandomAccessFile.read(bucket);
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
