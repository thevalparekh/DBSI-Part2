package heapManagement;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import datatype.*;

public class Main {
	
	public static final DataType[] datatype = {
		new Integer1Byte(), 
		new Integer2Byte(), 
		new Integer4Byte(), 
		new Integer8Byte(),
		new Real4Byte(),
		new Real8Byte(),
		new CharacterXByte()
	};

	public static void main(String args[]) {
		if (args.length < 1 ) {
			System.out.println("Usage: heapfile -i with CSV file in standard input or " +
			"heapfile -sn op constant | -pn for any n columns in heap file");
			System.exit(1);
		}
	
		String heapFile = args[0];
		Heap heap = new Heap(heapFile, datatype);
		

		if (args.length == 2 && args[1].equals("-i")) {
			insertRecordsFromCSV(heap);
			System.out.println("Successfully entered records into heapfile");
			System.exit(0);
		}		
		else {
			String[] conditions = new String[args.length-1];
			for (int i = 1; i < args.length; i++)
				conditions[i-1] = args[i];
			queryRecordsInHeap(heap, conditions);
			System.exit(0);
		}
	}
	
	
	private static void queryRecordsInHeap(Heap heap, String[] conditions) {
		if (heap.doesHeapFileExist()) {
			try {
				heap.openFile();
				heap.getHeapHeader();
			} catch (FileNotFoundException e) {
				System.out.println ("Directory, not a file");
			} catch (Exception e) {
				System.out.println("Invalid heap file format");
			}
		}
		else {
			System.out.println("Heapfile " + heap.fileName + " not found. ");
			System.exit(1);
		}

		try {
			Cursor cursor = new Cursor(heap, conditions, datatype);
			String record;
			/* Header */
			System.out.println(cursor.getHeader());
			while ((record = cursor.getNextRecord()) != null)
				System.out.println(record);
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Invalid argument in list of selections and projections");
			System.exit(1);
		}

	}

	/*
	 * This function will extract the records from the CSV file and insert it into the heap file
	 * If heap file does not exist it will create one, otherwise just append at the end of existing file
	 */
	private static void insertRecordsFromCSV(Heap heapFile) {

		CSVFileReader cfr = new CSVFileReader(System.in);
		cfr.ReadFile();
		ArrayList<String> records = cfr.getStoreValues();
		System.out.println("Extracted records from the CSV file");
		try{
			String originalCSVHeader = records.get(0);

			ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
			attributeList = Utilities.makeAttributeList(originalCSVHeader);
			String encodedCSVHeader = Utilities.formatHeaderSchema(attributeList);
			//1st check if the heap file already exists
			if(!heapFile.doesHeapFileExist()){
				heapFile.openFile();
				int count = 0;
				long offset = 0;
				for(String record : records){
					if(count++ == 0) {
						heapFile.makeHeapHeader(record,records.size()-1); // make the header and insert it
						offset = heapFile.head.getHeaderSize();
					} else {
						heapFile.insertInHeap(record,heapFile.head,offset);	
						offset += heapFile.head.getSizeOfRecord();
					}	
				}
				heapFile.closeHeap();
			} else { //append to existing file
				heapFile.openFile();
				//compare heap header for validation
				Header heapHeader = heapFile.getHeapHeader();
				if(encodedCSVHeader.compareToIgnoreCase(Utilities.formatHeaderSchema(heapHeader.getAttributeList())) == 0){
					//schema matched, append the records
					int  count = 0;
					long offset = heapHeader.getTotalRecords()*heapHeader.getSizeOfRecord()+heapHeader.getHeaderSize();
					for(String record : records){
						if (count++ == 0) {
							long totalRecords = heapHeader.getTotalRecords() + records.size()-1;
							heapFile.makeHeapHeader(record, totalRecords); 

						} else {
							heapFile.insertInHeap(record, heapFile. head, offset);
							offset += heapFile.head.getSizeOfRecord();	
						}
					}
				}
				else {
					//throw exception - or display error message
					System.out.println("The CSV schema does not match with heap header schema");
					System.exit(0);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}