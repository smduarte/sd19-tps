package tests;

public class TestWarningException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TestWarningException(String msg) {
		super(msg);
	}
	
	public String toString() {
		return String.format("FailedTestException: %s", this.getMessage() );
	}
}