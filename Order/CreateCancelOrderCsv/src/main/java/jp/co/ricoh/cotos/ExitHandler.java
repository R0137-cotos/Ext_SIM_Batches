package jp.co.ricoh.cotos;

public class ExitHandler implements IExitHandler {

	/**
	 * System.exit(n)を実装
	 */
	@Override
	public void exit(int status) {
		System.exit(status);
	}
}
