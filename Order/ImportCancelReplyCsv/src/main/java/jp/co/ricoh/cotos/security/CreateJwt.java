package jp.co.ricoh.cotos.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jp.co.ricoh.cotos.commonlib.util.HeadersProperties;
import lombok.Setter;

@Component
public class CreateJwt {

	@Setter
	@Autowired
	@Qualifier("forAuth")
	RestTemplate restForAuth;

	@Value("${cotos.authentication}")
	private String authenticationUrl;

	@Autowired
	HeadersProperties headersProperties;

	/**
	 * 認証ドメインによるJWTの生成
	 * @return
	 */
	public String execute() {
		ResponseEntity<String> result = restForAuth.getForEntity(authenticationUrl, String.class);
		String resultJwt = result.getHeaders().get(headersProperties.getAuthorization()).get(0);
		return resultJwt;
	}
}