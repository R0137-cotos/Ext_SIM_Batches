package jp.co.ricoh.cotos.security;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jp.co.ricoh.cotos.LoadRestTemplate;
import jp.co.ricoh.cotos.commonlib.util.HeadersProperties;
import jp.co.ricoh.cotos.config.LoadConfigulation;

@Component
public class CreateJwt {

	/**
	 * 認証ドメインによるJWTの生成
	 * @return
	 */
	public String execute() {
		HeadersProperties headersProperties = LoadConfigulation.getHeadersProperties();
		String authenticationUrl = LoadConfigulation.getAuthenticationUrl();

		RestTemplate restForAuth = LoadRestTemplate.loadRestTemplateForAuth();
		ResponseEntity<String> result = restForAuth.getForEntity(authenticationUrl, String.class);
		String resultJwt = result.getHeaders().get(headersProperties.getAuthorization()).get(0);
		return resultJwt;
	}
}
