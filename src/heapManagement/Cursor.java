package heapManagement;

import hashManagement.HashCursor;
import hashManagement.HashIndex;
import heapManagement.Condition.Operations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


import datatype.*;

public class Cursor {
	Heap heapFile;
	HeapHeader header;
	long nextRecordOffset;
	long nextRecord;
	DataType[] types;
	ArrayList<Condition> selectionList;
	ArrayList<Integer> projectionList;
	ArrayList<Attribute> schema;
	HashMap<Integer, HashIndex> indices;
	ArrayList<Long> results;

	public static final String SELECT = "-s[0-9]*";
	public static final int COLUMN_POSITION = 2;
	public static final String PROJECT = "-p[0-9]*";

	public Cursor(Heap file, String[] arguments, DataType[] types) throws Exception {
		this.heapFile = file;
		this.types = types;
		this.selectionList = new ArrayList<Condition>();
		this.projectionList = new ArrayList<Integer>();
		this.indices = file.getIndices();
		getSelectionsAndProjections(arguments);

		this.nextRecordOffset = (long) heapFile.head.getHeaderSize();
		this.nextRecord = 1;
		this.header = heapFile.head;
		this.schema = heapFile.head.getAttributeList();
		
		/* Populates results using hashes if any */
		this.results = getUsingHashIfAny();
	}

	private ArrayList<Long> getUsingHashIfAny() throws IOException {
		boolean gotUsingHash = false;
		ArrayList<Long> r = new ArrayList<Long>();
		for (Condition c : this.selectionList) {
			if (c.isHashCondition) {
				gotUsingHash = true;
				HashIndex index = this.indices.get(new Integer(c.column));
				HashCursor hashCursor = new HashCursor(index, types);
				Attribute attribute = schema.get(c.column-1);
				byte[] conditionValue = getAsByteArray(attribute.getType(), c, attribute.getSize());
				int attributeCode = Utilities.getIntDatatypeCode(attribute.getType(), attribute.getSize());
				
				ArrayList<Long> matchingRids = hashCursor.getAllRecords(attributeCode, conditionValue);
				/* Optimization: If returned result is empty. Entire selection is empty */
				if (matchingRids.isEmpty()) {
					r.clear();
					break;
				}
				/* If r was empty, nothing was put in yet. Set first results to r */
				if (r.isEmpty())
					r = matchingRids;
				r.retainAll(matchingRids);
			}
		}
		/* Null means we did not have any hash conditions */
		if (gotUsingHash)
			return r;
		else
			return null;
	}

	private void getSelectionsAndProjections(String[] args) throws Exception {
		for (int i = 0; i < args.length;) {
			if (args[i].matches(SELECT)) {
				int column = Integer.parseInt(args[i].substring(COLUMN_POSITION));
				String operation = args[i+1];
				String value = args[i+2];
				boolean haveHash = this.indices.containsKey(new Integer(column)) ? true : false;
				this.selectionList.add(new Condition(column, operation, value, haveHash));
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
		if (this.results == null)
			return getNextRecordByHeapScan();
		else
			return getNextRecordFromHash();	
	}


	private String getNextRecordFromHash() throws IOException {
		if (this.results.isEmpty())
			return null;
		
		RandomAccessFile randomAccessFile = heapFile.randomAccessFile;
		byte[] buff = null;
		outer:
			while (!this.results.isEmpty()) {
				Long top = this.results.remove(0);
				randomAccessFile.seek(top);
				buff = new byte[this.header.getSizeOfRecord()];
				randomAccessFile.read(buff);
				
				int offset = 0;
				int attributeNumber = 1;
				for (Attribute attribute: schema) {
					int size = attribute.getSize();
					char type = attribute.getType();
					int attributeCode = Utilities.getIntDatatypeCode(type, size);

					for (Condition condition : selectionList) {
						if (condition.column != attributeNumber || condition.isHashCondition) 
							continue;
						byte[] byteFormat = getAsByteArray(type, condition, size);
						int result = types[attributeCode].compare(buff, offset, byteFormat, 0, size);

						if (!satisfyCondition(result, condition.operation)) {
							continue outer;
						}
					}
					offset += size;
					attributeNumber++;
				}
				/* We have a valid record. Return it */
				if (this.projectionList.isEmpty())
					return project(buff, null);
				else 
					return project(buff, this.projectionList);
			}
		return null;
	}

	private String getNextRecordByHeapScan() throws IOException {
		RandomAccessFile file = heapFile.randomAccessFile;
		long numberOfRecords= this.header.getTotalRecords();
		int sizeOfRecord = this.header.getSizeOfRecord();
		byte[] buf = null;
		boolean haveRecord = false;

		if (this.nextRecord > numberOfRecords)
			return null;
		
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
						byte[] byteFormat = getAsByteArray(type, condition, size);
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
				haveRecord = true;
				break;
			}
		
		if (!haveRecord)
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
