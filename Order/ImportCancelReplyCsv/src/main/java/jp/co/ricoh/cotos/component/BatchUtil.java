package jp.co.ricoh.cotos.component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.dto.ExtendsParameterDto;
import jp.co.ricoh.cotos.dto.ThrowableFunction;
import lombok.Setter;

@Component
public class BatchUtil {

	@Setter
	@Autowired
	@Qualifier("forContractApi")
	RestTemplate restForContract;

	@Setter
	@Autowired
	@Qualifier("forArrangementApi")
	RestTemplate restForArrangement;

	@Value("${cotos.contract.url}")
	String COTOS_CONTRACT_URL;

	@Value("${cotos.arrangement.url}")
	String COTOS_ARRANGEMENT_URL;

	@Autowired
	ObjectMapper om;

	/**
	 * 契約情報更新APIを呼び出す
	 * @param contract 契約情報
	 */
	public void callUpdateContract(Contract contract) {
		restForContract.put(COTOS_CONTRACT_URL + "/contract", contract);
	}

	/**
	 * 手配情報業務完了APIを呼び出す
	 * @param arrangementWorkId 手配業務ID
	 */
	public void callCompleteArrangement(long arrangementWorkId) {
		restForArrangement.patchForObject(COTOS_ARRANGEMENT_URL + "/arrangementWork/" + arrangementWorkId + "/completeWork", null, Void.class);
	}

	/**
	 * 文字列を日付に変換する
	 * @param yyyyMMdd 文字列の日付
	 * @return Date型の日付
	 */
	public Date changeDate(String yyyyMMdd) {
		try {
			return new SimpleDateFormat("yyyyMMdd").parse(yyyyMMdd);
		} catch (ParseException e1) {
			return null;
		}
	}

	/**
	 * 拡張項目繰返文字列をオブジェクトに変換する
	 * @param extendsParameterIterance 拡張項目繰返
	 * @return 拡張項目繰返オブジェクト
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public List<ExtendsParameterDto> readJson(String extendsParameterIterance) throws JsonParseException, JsonMappingException, IOException {
		HashMap<String, HashMap<String, Object>> basicContentsJsonMap = om.readValue(extendsParameterIterance, new TypeReference<Object>() {
		});

		String extendsJson = om.writeValueAsString(basicContentsJsonMap.get("extendsParameterList"));
		List<ExtendsParameterDto> extendsParameterList = om.readValue(extendsJson, new TypeReference<List<ExtendsParameterDto>>() {
		});

		return extendsParameterList;
	}

	/**
	 * 例外Throw可能な関数型インターフェース
	 */
	public <T, R> Function<T, R> Try(ThrowableFunction<T, R> onTry, BiFunction<Exception, T, R> onCatch) {
		return x -> {
			try {
				return onTry.apply(x);
			} catch (Exception e) {
				e.printStackTrace();
				return onCatch.apply(e, x);
			}
		};
	}

}
