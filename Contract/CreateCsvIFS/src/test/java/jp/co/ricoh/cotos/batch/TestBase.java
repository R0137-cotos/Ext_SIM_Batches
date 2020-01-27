package jp.co.ricoh.cotos.batch;

import java.security.Permission;

import org.junit.AfterClass;
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
				if (1 == status) {
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
