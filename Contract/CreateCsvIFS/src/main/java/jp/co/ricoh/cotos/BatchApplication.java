package jp.co.ricoh.cotos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import jp.co.ricoh.cotos.batch.exec.CsvFileGenerator;
import jp.co.ricoh.cotos.commonlib.security.CotosAuthenticationDetails;
import jp.co.ricoh.cotos.commonlib.util.BatchMomInfoProperties;

@SpringBootApplication
public class BatchApplication {

	private static BatchMomInfoProperties batchProperty;

	@Autowired
	public void setBatchMomInfoProperties(BatchMomInfoProperties batchProperty) {
		BatchApplication.batchProperty = batchProperty;
	}

	public static void main(String[] args) {

		ConfigurableApplicationContext context = SpringApplication.run(BatchApplication.class, args);
		CotosAuthenticationDetails principal = new CotosAuthenticationDetails(batchProperty.getMomEmpId(), "sid", null, null, null, true, true, null);
		Authentication auth = new PreAuthenticatedAuthenticationToken(principal, null, null);
		SecurityContextHolder.getContext().setAuthentication(auth);
		context.getBean(CsvFileGenerator.class).execute(args);

	}
}
