package jp.co.ricoh.cotos.batch;

import org.springframework.beans.factory.annotation.Value;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class TestBase {

	@Value("${spring.datasource.driverClassName}")
	String dbDriver;
	@Value("${spring.datasource.url}")
	String dbURL;
	@Value("${spring.datasource.username}")
	String dbUser;
	@Value("${spring.datasource.password}")
	String dbPassword;

	// 出力ファイルパス
	protected static final String filePath = "src/test/resources/csv";
	// 出力ファイル名
	protected static final String fileName = "reply.csv";

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
