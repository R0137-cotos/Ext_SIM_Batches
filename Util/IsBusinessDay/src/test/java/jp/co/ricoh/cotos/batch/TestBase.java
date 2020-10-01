package jp.co.ricoh.cotos.batch;

import java.security.Permission;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import jp.co.ricoh.cotos.commonlib.security.CotosAuthenticationDetails;
import jp.co.ricoh.cotos.commonlib.util.BatchMomInfoProperties;
import jp.co.ricoh.cotos.commonlib.util.DatasourceProperties;
import jp.co.ricoh.cotos.config.LoadConfigulation;
import jp.co.ricoh.cotos.security.CreateJwt;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class TestBase {

	private static SecurityManager manager;

	private static String dbDriver;
	private static String dbURL;
	private static String dbUser;
	private static String dbPassword;

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
	public static void setDatasourceProperties() {
		DatasourceProperties datasourceProperties = LoadConfigulation.getDatasourceProperties();
		dbDriver = datasourceProperties.getDriverClassName();
		dbURL = datasourceProperties.getUrl();
		dbUser = datasourceProperties.getUsername();
		dbPassword = datasourceProperties.getPassword();
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

	public void auth() {
		BatchMomInfoProperties batchProperty = LoadConfigulation.getBatchMomInfoProperties();
		CreateJwt createJwt = new CreateJwt();
		String jwt = createJwt.execute();
		CotosAuthenticationDetails principal = new CotosAuthenticationDetails(batchProperty.getMomEmpId(), "sid", null, null, jwt, true, true, null);
		Authentication auth = new PreAuthenticatedAuthenticationToken(principal, null, null);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}
}
