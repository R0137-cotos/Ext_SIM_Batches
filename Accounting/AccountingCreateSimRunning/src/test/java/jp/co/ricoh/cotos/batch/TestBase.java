package jp.co.ricoh.cotos.batch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import jp.co.ricoh.cotos.batch.util.AccountingCreateSimRunningUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class TestBase {

	@Autowired
	AccountingCreateSimRunningUtil appUtil;

	@Value("${spring.datasource.driverClassName}")
	String dbDriver;
	@Value("${spring.datasource.url}")
	String dbURL;
	@Value("${spring.datasource.username}")
	String dbUser;
	@Value("${spring.datasource.password}")
	String dbPassword;

	@SuppressWarnings("serial")
	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class ExitException extends RuntimeException {
		private int status;

		public ExitException(int status) {
			this.status = status;
		}
	}
}
