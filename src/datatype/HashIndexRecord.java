package datatype;

public class HashIndexRecord {
	
	private long recordId;
	private byte[] dataValue;
	
	public HashIndexRecord() {
	 
	}


	public byte[] getDataValue() {
		return dataValue;
	}


	public void setDataValue(byte[] dataValue) {
		this.dataValue = dataValue;
	}


	public long getRecordId() {
		return recordId;
	}

	public void setRecordId(long recordId) {
		this.recordId = recordId;
	}

}
