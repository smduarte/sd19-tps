package tests;

public class TestFailedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TestFailedException(String msg) {
		super(msg);
	}

	@Override
	public String toString() {
		return String.format("TestFailedException: %s", this.getMessage());
	}
}