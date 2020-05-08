package jp.co.ricoh.cotos.component;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jp.co.ricoh.cotos.commonlib.dto.parameter.contract.ContractSearchParameter;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import lombok.Setter;

@Component
public class RestApiClient {

	@Setter
	@Autowired
	@Qualifier("forArrangementApi")
	RestTemplate restForArrangement;

	@Setter
	@Autowired
	@Qualifier("forContractApi")
	RestTemplate restForContract;

	@Value("${cotos.arrangement.url}")
	String COTOS_ARRANGEMENT_URL;

	@Value("${cotos.contract.url}")
	String COTOS_CONTRACT_URL;

	/**
	 * 契約一覧情報取得APIを呼び出す
	 * 
	 * @param dto
	 */
	public List<Contract> callFindTargetContract(ContractSearchParameter searchParam) {
		List<Contract> contract = Arrays.asList(restForContract.getForObject(COTOS_CONTRACT_URL + "/contract" + getParameterString(searchParam), Contract.class));
		return contract;
	}

	/**
	 * 契約情報更新APIを呼び出す
	 * 
	 * @param dto
	 */
	public void callUpdateContract(Contract contract) {
		restForContract.put(COTOS_CONTRACT_URL + "/contract", contract);
	}

	/**
	 * 契約情報更新APIを呼び出す
	 * 
	 * @param dto
	 */
	public void callCompleteArrangement(long arrangementWorkId) {
		restForArrangement.patchForObject(COTOS_ARRANGEMENT_URL + "/arrangementWork/" + arrangementWorkId + "/completeWork", null, Void.class);
	}

	/**
	 * URLリクエストパラメーター文字列取得
	 *
	 * @param Object
	 * @return
	 */
	private String getParameterString(Object Object) {
		String ret = "";
		String prefix = "?";
		for (Field field : Object.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				if (field.get(Object) != null) {
					String parameterString = "";
					if (field.getType().getEnumConstants() != null) {
						parameterString = ((Enum<?>) field.get(Object)).name();
					} else if (field.getType() == Date.class) {
						parameterString = new SimpleDateFormat("yyyy/MM/dd").format((Date) field.get(Object));
					} else {
						parameterString = field.get(Object).toString();
					}
					ret = ret + prefix + field.getName() + "=" + parameterString;
					prefix = "&";
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				continue;
			}
		}
		return ret;
	}

}