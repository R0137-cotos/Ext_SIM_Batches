package jp.co.ricoh.cotos.batch;

import jp.co.ricoh.cotos.IExitHandler;
import jp.co.ricoh.cotos.batch.TestBase.ExitException;
import lombok.Data;

@Data
public class TestExitHandler implements IExitHandler {

    /**
	 * TestのためSystem.exitではなくExitExceptionをthrowする
	 */
    @Override
    public void exit(int status) {
    	if (1 == status || 2 == status) {
			throw new ExitException(status);
		}
    }
}
