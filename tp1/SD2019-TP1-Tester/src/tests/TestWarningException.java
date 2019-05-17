package tests;

public class TestWarningException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TestWarningException(String msg) {
		super(msg);
	}

	@Override
	public String toString() {
		return String.format("WARNING: %s", this.getMessage());
	}
}