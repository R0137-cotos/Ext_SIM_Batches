package jp.co.ricoh.cotos.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import jp.co.ricoh.cotos.commonlib.util.BatchMomInfoProperties;
import jp.co.ricoh.cotos.commonlib.util.DatasourceProperties;
import jp.co.ricoh.cotos.commonlib.util.HeadersProperties;
import jp.co.ricoh.jmo.util.StringUtil;

/**
 *
 * application.ymlから設定情報を取得する
 *
 */
@SuppressWarnings("unchecked")
public class LoadConfigulation {

	private static LinkedHashMap<String, Object> configOrg;
	private static LinkedHashMap<String, Object> configActive;

	static {

		String ymlName = "config/application.yml";
		try (InputStreamReader in = new InputStreamReader(LoadConfigulation.class.getClassLoader().getResourceAsStream(ymlName))) {

			Yaml yaml = new Yaml();
			configOrg = (LinkedHashMap<String, Object>) yaml.load(in);
		} catch (IOException e) {

		}

		// 環境変数から読む込むymlファイルを指定する
		Map<String, String> env;
		env = System.getenv();
		String activeProfile = env.get("SPRING_PROFILES_ACTIVE");

		if (!StringUtil.isEmpty(activeProfile)) {
			String activeYmlName = "config/application-" + activeProfile + ".yml";
			try (InputStreamReader in = new InputStreamReader(LoadConfigulation.class.getClassLoader().getResourceAsStream(activeYmlName))) {
				Yaml yaml = new Yaml();
				configActive = (LinkedHashMap<String, Object>) yaml.load(in);
			} catch (IOException e) {

			}
		}
	}

	/**
	 * cotos.mom.system情報を取得
	 * @return cotos.mom.system情報
	 */
	public static BatchMomInfoProperties getBatchMomInfoProperties() {
		BatchMomInfoProperties properties = new BatchMomInfoProperties();

		// application.ymlの値を設定
		LinkedHashMap<String, Object> cotosOrg = (LinkedHashMap<String, Object>) configOrg.get("cotos");
		LinkedHashMap<String, Object> momOrg = (LinkedHashMap<String, Object>) cotosOrg.get("mom");
		LinkedHashMap<String, String> systemOrg = (LinkedHashMap<String, String>) momOrg.get("system");
		properties.setMomEmpId(systemOrg.get("momEmpId"));
		properties.setOperatorName(systemOrg.get("operatorName"));
		properties.setOperatorOrgName(systemOrg.get("operatorOrgName"));

		// アクティブymlのノード取得に失敗した場合は、すべてaplication.ymlの値を返す
		LinkedHashMap<String, String> system = null;
		try {
			LinkedHashMap<String, Object> cotos = (LinkedHashMap<String, Object>) configActive.get("cotos");
			LinkedHashMap<String, Object> mom = (LinkedHashMap<String, Object>) cotos.get("mom");
			system = (LinkedHashMap<String, String>) mom.get("system");
		} catch (Exception e) {
			return properties;
		}

		// 各値がアクティブymlに存在する場合は上書き
		try {
			properties.setMomEmpId(system.get("momEmpId"));
		} catch (Exception ignored) {
		}
		try {
			properties.setOperatorName(system.get("operatorName"));
		} catch (Exception ignored) {
		}
		try {
			properties.setOperatorOrgName(system.get("operatorOrgName"));
		} catch (Exception ignored) {
		}

		return properties;
	}

	/**
	 * cotos.auth.headers情報を取得
	 * @return cotos.auth.headers情報
	 */
	public static HeadersProperties getHeadersProperties() {
		HeadersProperties properties = new HeadersProperties();

		// application.ymlの値を設定
		LinkedHashMap<String, Object> cotosOrg = (LinkedHashMap<String, Object>) configOrg.get("cotos");
		LinkedHashMap<String, Object> authOrg = (LinkedHashMap<String, Object>) cotosOrg.get("auth");
		LinkedHashMap<String, String> headersOrg = (LinkedHashMap<String, String>) authOrg.get("headers");
		properties.setAuthorization(headersOrg.get("authorization"));
		properties.setRequireDispAuthorize(headersOrg.get("requireDispAuthorize"));
		properties.setDispAuthorization(headersOrg.get("dispAuthorization"));
		properties.setContentType(headersOrg.get("contentType"));
		properties.setFilename(headersOrg.get("filename"));

		// アクティブymlのノード取得に失敗した場合は、すべてaplication.ymlの値を返す
		LinkedHashMap<String, String> headers = null;
		try {
			LinkedHashMap<String, Object> cotos = (LinkedHashMap<String, Object>) configActive.get("cotos");
			LinkedHashMap<String, Object> auth = (LinkedHashMap<String, Object>) cotos.get("auth");
			headers = (LinkedHashMap<String, String>) auth.get("headers");
		} catch (Exception e) {
			return properties;
		}

		// 各値がアクティブymlに存在する場合は上書き
		try {
			properties.setAuthorization(headers.get("authorization"));
		} catch (Exception ignored) {
		}
		try {
			properties.setRequireDispAuthorize(headers.get("requireDispAuthorize"));
		} catch (Exception ignored) {
		}
		try {
			properties.setDispAuthorization(headers.get("dispAuthorization"));
		} catch (Exception ignored) {
		}
		try {
			properties.setContentType(headers.get("contentType"));
		} catch (Exception ignored) {
		}
		try {
			properties.setFilename(headers.get("filename"));
		} catch (Exception ignored) {
		}

		return properties;
	}

	/**
	 * cotos.authenticationを取得
	 * @return cotos.authentication
	 */
	public static String getAuthenticationUrl() {

		String authenticationUrl = "";
		try {
			LinkedHashMap<String, Object> cotos = (LinkedHashMap<String, Object>) configActive.get("cotos");
			authenticationUrl = (String) cotos.get("authentication");
		} catch (Exception e) {
		}
		if (StringUtil.isEmpty(authenticationUrl)) {
			LinkedHashMap<String, Object> cotos = (LinkedHashMap<String, Object>) configOrg.get("cotos");
			authenticationUrl = (String) cotos.get("authentication");
		}

		return authenticationUrl;
	}

	/**
	 * spring.messages情報を取得
	 * @return spring.messages情報
	 */
	public static Map<String, String> getMessageProperties() {
		Map<String, String> messageProperties = new HashMap<>();

		// application.ymlの値を設定
		LinkedHashMap<String, Object> springOrg = (LinkedHashMap<String, Object>) configOrg.get("spring");
		LinkedHashMap<String, String> messagesOrg = (LinkedHashMap<String, String>) springOrg.get("messages");
		messageProperties.put("basename", messagesOrg.get("basename"));
		messageProperties.put("encoding", messagesOrg.get("encoding"));

		// アクティブymlのノード取得に失敗した場合は、すべてaplication.ymlの値を返す
		LinkedHashMap<String, String> messages = null;
		try {
			LinkedHashMap<String, Object> spring = (LinkedHashMap<String, Object>) configActive.get("spring");
			messages = (LinkedHashMap<String, String>) spring.get("messages");
		} catch (Exception e) {
			return messageProperties;
		}

		// 各値がアクティブymlに存在する場合は上書き
		try {
			messageProperties.put("basename", messages.get("basename"));
		} catch (Exception ignored) {
		}
		try {
			messageProperties.put("encoding", messages.get("encoding"));
		} catch (Exception ignored) {
		}

		return messageProperties;
	}

	/**
	 * spring.datasource情報を取得
	 * @return spring.datasource情報
	 */
	public static DatasourceProperties getDatasourceProperties() {
		DatasourceProperties properties = new DatasourceProperties();

		// application.ymlの値を設定
		LinkedHashMap<String, Object> springOrg = (LinkedHashMap<String, Object>) configOrg.get("spring");
		LinkedHashMap<String, String> datasourceOrg = (LinkedHashMap<String, String>) springOrg.get("datasource");
		properties.setUrl(datasourceOrg.get("url"));
		properties.setUsername(datasourceOrg.get("username"));
		properties.setPassword(datasourceOrg.get("password"));
		properties.setDriverClassName(datasourceOrg.get("driverClassName"));

		// アクティブymlのノード取得に失敗した場合は、すべてaplication.ymlの値を返す
		LinkedHashMap<String, String> datasource = null;
		try {
			LinkedHashMap<String, Object> spring = (LinkedHashMap<String, Object>) configActive.get("spring");
			datasource = (LinkedHashMap<String, String>) spring.get("datasource");
		} catch (Exception e) {
			return properties;
		}

		// 各値がアクティブymlに存在する場合は上書き
		try {
			properties.setUrl(datasource.get("url"));
		} catch (Exception ignored) {
		}
		try {
			properties.setUsername(datasource.get("username"));
		} catch (Exception ignored) {
		}
		try {
			properties.setPassword(datasource.get("password"));
		} catch (Exception ignored) {
		}
		try {
			properties.setDriverClassName(datasource.get("driverClassName"));
		} catch (Exception ignored) {
		}

		return properties;
	}

	/**
	 * cotos.master.urlを取得
	 * @return cotos.master.url
	 */
	public static String getMasterUrl() {

		String masterUrl = "";
		try {
			LinkedHashMap<String, Object> cotos = (LinkedHashMap<String, Object>) configActive.get("cotos");
			LinkedHashMap<String, String> master = (LinkedHashMap<String, String>) cotos.get("master");
			masterUrl = (String) master.get("url");
		} catch (Exception e) {
		}
		if (StringUtil.isEmpty(masterUrl)) {
			LinkedHashMap<String, Object> cotos = (LinkedHashMap<String, Object>) configOrg.get("cotos");
			LinkedHashMap<String, String> master = (LinkedHashMap<String, String>) cotos.get("master");
			masterUrl = (String) master.get("url");
		}

		return masterUrl;
	}

	/**
	 * spring.jpa.properties.hibernate情報を取得
	 * @return spring.jpa.properties.hibernate情報
	 */
	public static Map<String, String> getHibernateProperties() {
		Map<String, String> hibernateProperties = new HashMap<>();

		// application.ymlの値を設定
		LinkedHashMap<String, Object> springOrg = (LinkedHashMap<String, Object>) configOrg.get("spring");
		LinkedHashMap<String, Object> jpaOrg = (LinkedHashMap<String, Object>) springOrg.get("jpa");
		LinkedHashMap<String, Object> propertiesOrg = (LinkedHashMap<String, Object>) jpaOrg.get("properties");
		LinkedHashMap<String, Object> hibernateOrg = (LinkedHashMap<String, Object>) propertiesOrg.get("hibernate");
		hibernateProperties.put("show_sql", hibernateOrg.get("show_sql").toString());
		hibernateProperties.put("use_sql_comments", hibernateOrg.get("use_sql_comments").toString());
		hibernateProperties.put("format_sql", hibernateOrg.get("format_sql").toString());
		hibernateProperties.put("default_schema", hibernateOrg.get("default_schema").toString());

		// アクティブymlのノード取得に失敗した場合は、すべてaplication.ymlの値を返す
		LinkedHashMap<String, Object> hibernate = null;
		try {
			LinkedHashMap<String, Object> spring = (LinkedHashMap<String, Object>) configActive.get("spring");
			LinkedHashMap<String, Object> jpa = (LinkedHashMap<String, Object>) spring.get("jpa");
			LinkedHashMap<String, Object> properties = (LinkedHashMap<String, Object>) jpa.get("properties");
			hibernate = (LinkedHashMap<String, Object>) properties.get("hibernate");
		} catch (Exception e) {
			return hibernateProperties;
		}

		// 各値がアクティブymlに存在する場合は上書き
		try {
			hibernateProperties.put("show_sql", hibernate.get("show_sql").toString());
		} catch (Exception ignored) {
		}
		try {
			hibernateProperties.put("use_sql_comments", hibernate.get("use_sql_comments").toString());
		} catch (Exception ignored) {
		}
		try {
			hibernateProperties.put("format_sql", hibernate.get("format_sql").toString());
		} catch (Exception ignored) {
		}
		try {
			hibernateProperties.put("default_schema", hibernate.get("default_schema").toString());
		} catch (Exception ignored) {
		}
		return hibernateProperties;
	}
}
