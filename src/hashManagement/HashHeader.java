package hashManagement;

public class HashHeader {

	private int level;
	private int next;
	private int freeListBucketHead;
	private String indexType;
	private int headerSize;
	private int nextBucketId;
	private int initialNumberOfBuckets; //N or S
	
	public int getHeaderSize() {
		return headerSize;
	}

	public void setHeaderSize(int headerSize) {
		this.headerSize = headerSize;
	}
		
	public HashHeader(String datatype) {
		this.level = 0;
		this.initialNumberOfBuckets = 1;
		this.next = 0;
		this.freeListBucketHead = -1;
		this.nextBucketId = 0;
		this.indexType = datatype;
	}
	
	public int getNextBucketId() {
		return nextBucketId;
	}

	public void setNextBucketId(int nextBucketId) {
		this.nextBucketId = nextBucketId;
	}

	public int getInitialNumberOfBuckets() {
		return initialNumberOfBuckets;
	}
	public void setInitialNumberOfBuckets(int initialNumberOfBuckets) {
		this.initialNumberOfBuckets = initialNumberOfBuckets;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getNext() {
		return next;
	}
	public void setNext(int next) {
		
		
		if(next == (((int)Math.pow(2, level)))){
			this.next = 0;
			this.level++ ;
		}else{
			this.next = next;	
		}
		//System.out.println("--Level " + level + "--Next " + next + "  --freehead" + freeListBucketHead + "  nextBucket" + nextBucketId);
	}
	public int getFreeListBucketHead() {
		return freeListBucketHead;
	}

	public void setFreeListBucketHead(int freeListBucketHead) {
		this.freeListBucketHead = freeListBucketHead;
	}

	public String getIndexType() {
		return indexType;
	}
	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}
	
	public int getTotalBuckets(){
		return this.next + (int)Math.pow(2, level);
	}
}
