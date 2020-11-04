package jp.co.ricoh.cotos.util;

public class ProcessErrorException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ProcessErrorException() {
		super();
	}

	public ProcessErrorException(Throwable e) {
		super(e);
	}
}
