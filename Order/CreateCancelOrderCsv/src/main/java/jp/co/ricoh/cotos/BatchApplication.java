package jp.co.ricoh.cotos;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.client.RestTemplate;

import jp.co.ricoh.cotos.commonlib.security.CotosAuthenticationDetails;
import jp.co.ricoh.cotos.commonlib.util.BatchMomInfoProperties;
import jp.co.ricoh.cotos.commonlib.util.HeadersProperties;
import jp.co.ricoh.cotos.logic.JobComponent;
import jp.co.ricoh.cotos.security.CreateJwt;

@SpringBootApplication
public class BatchApplication {

	private static BatchMomInfoProperties batchProperty;

	@Autowired
	RestTemplateBuilder restTemplateBuilder;

	@Autowired
	HeadersProperties headersProperties;

	@Autowired
	public void setBatchMomInfoProperties(BatchMomInfoProperties batchProperty) {
		BatchApplication.batchProperty = batchProperty;
	}

	private static CreateJwt createJwt;

	@Autowired
	@Lazy
	public void setCreateJwt(CreateJwt createJwt) {
		BatchApplication.createJwt = createJwt;
	}

	@Bean(name = "forAuth")
	public RestTemplate loadRestTemplateForAuthentication() {
		return loadRestTemplateForAuth();
	}

	/**
	 * メイン処理
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// 認証処理
		ConfigurableApplicationContext context = SpringApplication.run(BatchApplication.class, args);
		String jwt = createJwt.execute();
		CotosAuthenticationDetails principal = new CotosAuthenticationDetails(batchProperty.getMomEmpId(), "sid", null, null, jwt, true, true, null);
		Authentication auth = new PreAuthenticatedAuthenticationToken(principal, null, null);
		SecurityContextHolder.getContext().setAuthentication(auth);

		// ジョブの実行
		context.getBean(JobComponent.class).run(args);
	}

	/**
	 * RestTemplateの生成（認証ドメイン）
	 * 
	 * @return
	 */
	private RestTemplate loadRestTemplateForAuth() {
		RestTemplate rest = new RestTemplate();
		rest.setInterceptors(Stream.concat(rest.getInterceptors().stream(), Arrays.asList(new ClientHttpRequestInterceptor() {
			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
				request.getHeaders().add("X-Cotos-Mom-Emp-Id", batchProperty.getMomEmpId());
				request.getHeaders().add("X-Cotos-Single-User-Id", "sid");
				request.getHeaders().add("X-Cotos-Application-Id", "cotos_batch");
				request.getHeaders().add("X-Cotos-Pass", "cotosmightyoubehappy");

				return execution.execute(request, body);
			}
		}).stream()).collect(Collectors.toList()));

		return rest;
	}
}
