package heapManagement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import datatype.DataType;

public class Heap {
	String fileName;
	RandomAccessFile randomAccessFile;
	int writeOffset, readOffset;
	HeapHeader head;
	DataType[] datatype;
	ArrayList<Integer> hashColumns = null;

	public Heap(String fileName, DataType[] datatypes)  {
		this.fileName = fileName;
		this.writeOffset = 0;
		this.readOffset = 0;
		this.datatype = datatypes;
		this.hashColumns = getHashFiles();
	}

	public void openFile() throws FileNotFoundException {
		this.randomAccessFile = new RandomAccessFile(new File(this.fileName), "rw");
	}
		
	public int insertInHeap(String record, HeapHeader header, long offset) throws IOException {
		StringTokenizer stringTok = new StringTokenizer(record, ",");		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		randomAccessFile.seek(offset);
		
		ArrayList<Attribute> attributeList = header.getAttributeList();
		try{
			for(int i = 0; i< attributeList.size(); i++) {

				if(stringTok.hasMoreTokens()) {
					String nextTok = stringTok.nextToken();
					int attributeCode = Utilities.getIntDatatypeCode(attributeList.get(i).getType(),attributeList.get(i).getSize());
					datatype[attributeCode].write(byteArrayOutputStream, nextTok,attributeList.get(i).getSize() );
				} else {
					System.out.println("Invalid record..");
				}
			}
			randomAccessFile.write(byteArrayOutputStream.toByteArray());
		} catch (IOException e){
			return -1;
		}	
		return 0;
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

	public HeapHeader getHeapHeader() throws Exception {
		//retrieve header and advance the pointer
		/*
		 * Format of the header:
		 *   1st 4 bytes - Size of the header
		 *   2nd 4 bytes - No of Attributes
		 *   3rd 4 bytes - Size of one record
		 *   4th 8 bytes - Total number of Records
		 *   5th rest of the bytes - Schema
		 */
		randomAccessFile.seek(0);
		byte[] headSize = new byte[4];
		randomAccessFile.read(headSize);
		this.head = new HeapHeader();
		int headIntSize = Utilities.toInt(headSize);
		byte[] head = new byte[headIntSize];
		randomAccessFile.seek(0);
		randomAccessFile.read(head);


		byte[] temp = Arrays.copyOfRange(head, 0, 4);
		this.head.setHeaderSize(Utilities.toInt(temp));

		temp = Arrays.copyOfRange(head, 4, 8);
		this.head.setNoOfCol(Utilities.toInt(temp));

		temp = Arrays.copyOfRange(head, 8, 12);
		this.head.setSizeOfRecord(Utilities.toInt(temp));

		temp = Arrays.copyOfRange(head, 12, 20);
		this.head.setTotalRecords(Utilities.toLong(temp));

		temp = Arrays.copyOfRange(head, 20, head.length);
		this.head.setSchema(new String(temp));

		return this.head;
	}


	public void makeHeapHeader(String header, long totalRecords) {
		//String encodedSchema = new String();
		StringTokenizer stringTok = new StringTokenizer(header, ",");

		int recordSize = 0, attributes = 0;
		while(stringTok.hasMoreTokens()){
			attributes++;
			String nextTok = stringTok.nextToken();
			int size = Integer.parseInt(nextTok.substring(1));
			//encodedSchema = encodedSchema + encodeToSchema(nextTok.charAt(0), size);
			recordSize += size;
		}
		
		this.head = new HeapHeader();
		//head.setEncodedSchema(encodedSchema);
		head.setSchema(header);
		head.setNoOfCol(attributes);
		head.setSizeOfRecord(recordSize);
		head.setTotalRecords(totalRecords);
		head.setHeaderSize(20+head.getSchema().length());

		/*
		 * Format of the header:
		 *   1st 4 bytes - Size of the header
		 *   2nd 4 bytes - No of Attributes
		 *   3rd 4 bytes - Size of one record
		 *   4th 8 bytes - Total number of Records
		 *   5th rest of the bytes - Schema
		 */
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byteArrayOutputStream.write(Utilities.toByta(head.getHeaderSize()));
			byteArrayOutputStream.write(Utilities.toByta(head.getNoOfCol()));
			byteArrayOutputStream.write(Utilities.toByta(head.getSizeOfRecord()));
			byteArrayOutputStream.write(Utilities.toByta(head.getTotalRecords()));
			byteArrayOutputStream.write(head.getSchema().getBytes());
			randomAccessFile.seek(0);
			randomAccessFile.write(byteArrayOutputStream.toByteArray());			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public ArrayList<Integer> getHashColumns() {
		return hashColumns;
	}

	private ArrayList<Integer> getHashFiles() {
		ArrayList<Integer> existingHashFiles = new ArrayList<Integer>();
		String[] children = new File(".").list();
		for (String child : children) {
			/* Only adding index file. Overflow must exist */
			if (child.matches(this.fileName + ".[0-9]*.lht"))  {
				String indexNumber = child.replace(this.fileName + ".", "").replace(".lht", "");
				Integer index = new Integer(indexNumber);
				existingHashFiles.add(index);
			}
		}
		return existingHashFiles;
	}
	
	/* Debugging function */
	public  void retrieveRecordsFromHeap(DataType[] datatype) {
		try{
			int offset = this.head.getHeaderSize();
			RandomAccessFile randomAccessFile = new  RandomAccessFile(new File(this.fileName), "rw");
			for(int i = 0 ; i < this.head.getTotalRecords(); i++){
				randomAccessFile.seek(offset);
				byte[] buf = new byte[this.head.getSizeOfRecord()];
				randomAccessFile.read(buf);
				offset += this.head.getSizeOfRecord();	
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				int size = 0;
				

				ArrayList<Attribute> attributeList = this.head.getAttributeList();
				
				for(int j = 0; j< attributeList.size(); j++){
					int attributeCode = Utilities.getIntDatatypeCode(attributeList.get(j).getType(),attributeList.get(j).getSize());
					byte[] temp = Arrays.copyOfRange(buf, size,size+attributeList.get(j).getSize());
					size +=  attributeList.get(j).getSize();
					datatype[attributeCode].read(byteArrayOutputStream, temp);
					if(j < attributeList.size()-1) {
						byteArrayOutputStream.write(',');
					}
				}
				System.out.println("Record: " + new String(byteArrayOutputStream.toByteArray()));
			}
		} catch(IOException e){
			e.printStackTrace();	
		}
	}
}
