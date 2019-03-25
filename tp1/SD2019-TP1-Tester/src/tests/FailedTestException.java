package tests;

public class FailedTestException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public FailedTestException(String msg) {
		super(msg);
	}
	
	public String toString() {
		return String.format("FailedTestException: %s", this.getMessage() );
	}
}