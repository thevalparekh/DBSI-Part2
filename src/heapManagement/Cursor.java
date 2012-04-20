package heapManagement;

import heapManagement.Condition.Operations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

import datatype.*;

public class Cursor {
	Heap heapFile;
	Header header;
	long nextRecordOffset;
	long nextRecord;
	DataType[] types;
	ArrayList<Condition> selectionList;
	ArrayList<Integer> projectionList;
	ArrayList<Attribute> schema;

	public static final String SELECT = "-s[0-9]*";
	public static final int COLUMN_POSITION = 2;
	public static final String PROJECT = "-p[0-9]*";

	public Cursor(Heap file, String[] arguments, DataType[] types) throws Exception {
		this.heapFile = file;
		this.types = types;
		this.selectionList = new ArrayList<Condition>();
		this.projectionList = new ArrayList<Integer>();
		getSelectionsAndProjections(arguments);
		this.nextRecordOffset = (long) heapFile.head.getHeaderSize();
		this.nextRecord = 1;
		this.header = heapFile.head;
		this.schema = heapFile.head.getAttributeList();
	}

	private void getSelectionsAndProjections(String[] args) throws Exception {
		for (int i = 0; i < args.length;) {
			if (args[i].matches(SELECT)) {
				int column = Integer.parseInt(args[i].substring(COLUMN_POSITION));
				String operation = args[i+1];
				String value = args[i+2];
				this.selectionList.add(new Condition(column, operation, value));
				i+=3;				
			} else if (args[i].matches(PROJECT)) {
				int column = Integer.parseInt(args[i].substring(COLUMN_POSITION));
				this.projectionList.add(new Integer(column));
				i++;
			} else {
				throw new Exception();
			}			
		}
	}

	public String getNextRecord() throws IOException {
		RandomAccessFile file = heapFile.randomAccessFile;
		long numberOfRecords= this.header.getTotalRecords();
		int sizeOfRecord = this.header.getSizeOfRecord();
		byte[] buf = null;
		
	outer:
		while(this.nextRecord <= numberOfRecords) {
			file.seek(nextRecordOffset);
			buf = new byte[sizeOfRecord];
			file.read(buf);
			this.nextRecordOffset += sizeOfRecord;
			this.nextRecord++;
			int offset = 0;
			int attributeNumber = 1;
			
			for (Attribute attribute: schema) {
				int size = attribute.getSize();
				char type = attribute.getType();
				int attributeCode = Utilities.getIntDatatypeCode(type, size);

				for (Condition condition : selectionList) {
					if (condition.column != attributeNumber) 
						continue;
					byte[] byteFormat = getAsByteArray(attribute.getType(), condition, size);
					int result = types[attributeCode].compare(buf, offset, 
							byteFormat, 
							0, size);
					
					if (!satisfyCondition(result, condition.operation)){
						continue outer;
					}
						
				}
				offset += size;
				attributeNumber++;
			}
			/* 
			 * If you got here, it means you went through all conditions on 
			 * all attributes and they all passed. You have a valid record. 
			 * Break the while loop.
			 */
			break;
		}

		if (this.nextRecord > numberOfRecords)
			return null;

		if (this.projectionList.isEmpty())
			return project(buf, null);
		else 
			return project(buf, this.projectionList);
	}

	private byte[] getAsByteArray(char type, Condition cond, int size) {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		int attributeNumber = Utilities.getIntDatatypeCode(type, size);
		try {
			types[attributeNumber].write(byteArray, cond.value, size);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteArray.toByteArray();
	}

	private String project(byte[] record, ArrayList<Integer> projectionList) throws IOException {
		int offset = 0;
		/* To remove commas in last column */
		int lastColumn = schema.size()-1;
		if (projectionList != null)
			lastColumn = findMax(projectionList)-1;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		for(int j = 0; j< schema.size(); j++){
			int attributeCode = Utilities.getIntDatatypeCode(schema.get(j).getType(),schema.get(j).getSize());
			byte[] temp = Arrays.copyOfRange(record, offset, offset+schema.get(j).getSize());
			offset +=  schema.get(j).getSize();
			if (projectionList != null && !projectionList.contains(new Integer(j+1)))
				continue;
			types[attributeCode].read(byteArrayOutputStream, temp);
			if(j < lastColumn) {
				byteArrayOutputStream.write(',');
			}
		}
		return new String(byteArrayOutputStream.toByteArray());
	}

	private int findMax(ArrayList<Integer> projectionList) {
		int max = 0;
		for (Integer i : projectionList)
			if (i.intValue() > max)
				max = i;
		return max;
	}

	private boolean satisfyCondition(int result, Operations operation) {
		boolean satisfiesCondition = false;
		switch (operation) {
		case LT:
			satisfiesCondition = result == -1 ? true : false;
			break;
		case GT:
			satisfiesCondition = result == 1 ? true : false;
			break;
		case LTE:
			satisfiesCondition = result == 1 ? false : true;
			break;
		case GTE:
			satisfiesCondition = result == -1 ? false : true;
			break;
		case EQ:
			satisfiesCondition = result == 0 ? true : false;
			break;
		case NEQ:
			satisfiesCondition = result != 0 ? true : false;
			break;
		case INV:
			break;			
		}
		return satisfiesCondition;
	}

	public String getHeader() {
		String result = header.getSchema();
		if (projectionList.isEmpty())
			return result;
		String[] attr = result.split(",");
		result = "";
		for (int i = 0; i < attr.length; i++)
			if (projectionList.contains(new Integer(i+1)))
				result = result + attr[i] + ",";
		result = result.substring(0, result.length()-1);
		return result;
	}

}
