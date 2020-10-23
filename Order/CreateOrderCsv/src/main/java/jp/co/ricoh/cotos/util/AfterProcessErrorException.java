package jp.co.ricoh.cotos.util;

public class AfterProcessErrorException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AfterProcessErrorException() {
		super();
	}

	public AfterProcessErrorException(Throwable e) {
		super(e);
	}
}
