package shared;

public class ResponseProtocol {
	public String operation;
	public String id;
	public String result;
	public String info; // error message등
	
	public ResponseProtocol() {
		this("", "", "", "");
	}

	public ResponseProtocol(String operation, String id, String result) {
		this(operation, id, result, "");
	}
	
	public ResponseProtocol(String operation, String id, String result, String info) {
		this.operation = operation;
		this.id = id;
		this.result = result;
		this.info = info;
	}

	public String toString() {
		return "operation: " + operation + " / id: " + id + " / result: " + result + " / info: [" + info + "]";
	}
}
