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
}
