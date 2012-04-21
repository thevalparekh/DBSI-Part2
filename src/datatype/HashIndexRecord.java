package datatype;

public class HashIndexRecord {
	
	private long recordId;
	private String dataValue;
	
	public HashIndexRecord() {
	 
	}

	public String getDataValue() {
		return dataValue;
	}

	public void setDataValue(String dataValue) {
		this.dataValue = dataValue;
	}

	public long getRecordId() {
		return recordId;
	}

	public void setRecordId(long recordId) {
		this.recordId = recordId;
	}

}
