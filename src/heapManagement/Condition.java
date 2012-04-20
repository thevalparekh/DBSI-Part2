package heapManagement;

public class Condition {
	public enum Operations {
		LT,
		GT,
		EQ,
		NEQ,
		LTE,
		GTE,
		INV
	}
	
	Operations operation;
	int column;
	String value;
	
	public Condition(int column, String operation, String value) {
		this.column = column;
		this.value = value;
		this.operation = getOperation(operation);
	}
	
	private Operations getOperation(String op) {
		if (op.equals("<"))
			return Operations.LT;
		else if (op.equals(">"))
			return Operations.GT;
		else if (op.equals("="))
			return Operations.EQ;
		else if (op.equals("<>"))
			return Operations.NEQ;
		else if (op.equals("<="))
			return Operations.LTE;
		else if (op.equals(">="))
			return Operations.GTE;
		else
			return Operations.INV;		
	}

}
