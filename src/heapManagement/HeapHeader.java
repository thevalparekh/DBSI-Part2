package heapManagement;

import java.util.ArrayList;

public class HeapHeader {
	private int noOfCol;
	private int sizeOfRecord;
	private long totalRecords;
	private int headerSize;
	private String schema;
	private ArrayList<Attribute> attributeList;

	public HeapHeader(){
		this.attributeList = new ArrayList<Attribute>();
	}

	public int getNoOfCol() {
		return noOfCol;
	}
	public void setNoOfCol(int noOfCol) {
		this.noOfCol = noOfCol;
	}
	public int getSizeOfRecord() {
		return sizeOfRecord;
	}
	public void setSizeOfRecord(int sizeOfRecord) {
		this.sizeOfRecord = sizeOfRecord;
	}
	public long getTotalRecords() {
		return totalRecords;
	}
	public void setTotalRecords(long totalRecords) {
		this.totalRecords = totalRecords;
	}

	public int getHeaderSize() {
		return headerSize;
	}

	public void setHeaderSize(int headerSize) {
		this.headerSize = headerSize;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String encodedSchema) {
		this.schema = encodedSchema;
		setAttributeList();
	}

	public void setAttributeList(){
		this.attributeList = Utilities.makeAttributeList(this.schema);
	}

	public ArrayList<Attribute> getAttributeList(){
		return this.attributeList;
	}
}
