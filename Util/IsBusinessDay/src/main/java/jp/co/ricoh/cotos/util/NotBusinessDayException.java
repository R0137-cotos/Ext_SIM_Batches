package jp.co.ricoh.cotos.util;

public class NotBusinessDayException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NotBusinessDayException() {
		super();
	}

	public NotBusinessDayException(Throwable e) {
		super(e);
	}
}
