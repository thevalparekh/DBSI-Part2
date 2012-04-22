package hashManagement;

public class HashIndexRecord {

	private long recordId;
	private byte[] dataValue;

	public HashIndexRecord() {
	}

	public HashIndexRecord(long rid, byte[] data) {
		this.recordId = rid;
		this.dataValue = data;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) 
			return true;
		if (!(o instanceof HashIndexRecord)) 
			return false;

		HashIndexRecord record = (HashIndexRecord) o;
		if (this.recordId != record.getRecordId()) 
			return false;

		return true;
	}
	
	@Override
	public int hashCode() {
		return new Long(this.recordId).hashCode();
	}
}
