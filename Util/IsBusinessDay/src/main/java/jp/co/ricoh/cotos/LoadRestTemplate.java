package jp.co.ricoh.cotos;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import jp.co.ricoh.cotos.commonlib.security.CotosAuthenticationDetails;
import jp.co.ricoh.cotos.commonlib.util.BatchMomInfoProperties;
import jp.co.ricoh.cotos.commonlib.util.HeadersProperties;
import jp.co.ricoh.cotos.config.LoadConfigulation;

/**
 *
 * RestTemplateを返却する
 *
 */
public class LoadRestTemplate {

	public static RestTemplate loadRestTemplate() {
		HeadersProperties headersProperties = LoadConfigulation.getHeadersProperties();
		RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
		RestTemplate rest = restTemplateBuilder.build();
		rest.setInterceptors(Stream.concat(rest.getInterceptors().stream(), Arrays.asList(new ClientHttpRequestInterceptor() {
			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
				CotosAuthenticationDetails userInfo = (CotosAuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				request.getHeaders().add(headersProperties.getAuthorization(), userInfo.getJwt());
				return execution.execute(request, body);
			}
		}).stream()).collect(Collectors.toList()));

		return rest;
	}

	/**
	 * RestTemplateの生成（認証ドメイン）
	 * @return
	 */
	public static RestTemplate loadRestTemplateForAuth() {
		BatchMomInfoProperties batchProperty = LoadConfigulation.getBatchMomInfoProperties();
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
