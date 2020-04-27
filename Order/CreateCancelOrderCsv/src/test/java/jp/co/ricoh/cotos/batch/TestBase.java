package jp.co.ricoh.cotos.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Permission;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Value;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class TestBase {

	private static SecurityManager manager;
	@Value("${spring.datasource.driverClassName}")
	String dbDriver;
	@Value("${spring.datasource.url}")
	String dbURL;
	@Value("${spring.datasource.username}")
	String dbUser;
	@Value("${spring.datasource.password}")
	String dbPassword;

	// 出力ファイルパス
	protected static final String filePath = "output";
	// 出力ファイル名
	protected static final String fileName = "test.csv";
	// 一時ファイル名
	protected static final String tmpFileName = "temp.csv";
	// 出力ファイル
	protected static final File csvFile = Paths.get(filePath, fileName).toFile();
	// 一時ファイル
	protected static final File tmpFile = Paths.get(filePath, tmpFileName).toFile();

	@Before
	public void InitializeDirectory() throws IOException {
		// 出力ファイルが存在する場合は削除する
		Files.deleteIfExists(csvFile.toPath());

		//　一時ファイルが存在する場合は削除する
		Files.deleteIfExists(tmpFile.toPath());

		// フォルダが存在しない場合は作成する
		if (csvFile.getParentFile().exists()) {
			csvFile.getParentFile().mkdir();
		}
	}

	@SuppressWarnings("serial")
	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class ExitException extends RuntimeException {
		private int status;

		public ExitException(int status) {
			this.status = status;
		}
	}

	@BeforeClass
	public static void rewriteSystemExit() {
		manager = System.getSecurityManager();
		System.setSecurityManager(new SecurityManager() {
			@Override
			public void checkExit(int status) {
				if (1 == status || 2 == status) {
					throw new ExitException(status);
				}
			}

			@Override
			public void checkPermission(Permission permission) {
			}
		});
	}

	@AfterClass
	public static void resetSystemExit() {
		System.setSecurityManager(manager);
	}

}
