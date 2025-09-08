package jp.co.ricoh.cotos.component;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jp.co.ricoh.cotos.commonlib.entity.arrangement.ArrangementWork;
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

	@Value("${cotos.mom.superUserId}")
	String COTOS_MOM_SUPERUSERID;

	@Value("${cotos.contract.url}")
	String COTOS_CONTRACT_URL;

	/**
	 * 手配情報担当作業者設定APIを呼び出す
	 *
	 * @param arrangementWorkIdList
	 *            手配業務IDリスト
	 */
	public void callAssignWorker(List<Long> arrangementWorkIdList) {
		if (CollectionUtils.isEmpty(arrangementWorkIdList))
			return;
		restForArrangement.postForObject(COTOS_ARRANGEMENT_URL + "/arrangementWork/assignWorker?workerMomEmpId=" + COTOS_MOM_SUPERUSERID, arrangementWorkIdList, Void.class);
	}

	/**
	 * 手配情報業務受付APIを呼び出す
	 *
	 * @param arrangementWorkIdList
	 *            手配業務IDリスト
	 */
	public void callAcceptWorkApi(List<Long> arrangementWorkIdList) {
		if (CollectionUtils.isEmpty(arrangementWorkIdList))
			return;
		restForArrangement.patchForObject(COTOS_ARRANGEMENT_URL + "/arrangementWork/acceptWork", arrangementWorkIdList, Void.class);
	}

	/**
	 * 手配情報取得APIを呼び出す
	 */
	public ArrangementWork callFindOneArrangement(long arrangementWorkId) {
		String parameterStr = COTOS_ARRANGEMENT_URL + "/arrangementWork/" + arrangementWorkId;
		ResponseEntity<ArrangementWork> result = restForArrangement.getForEntity(parameterStr, ArrangementWork.class);
		return result == null ? null : result.getBody();
	}

	/**
	 * 契約情報明細取得APIを呼び出す
	 *
	 * @param conttactId
	 *            契約ID
	 * @return 
	 */
	public Contract callFindOneContractApi(long contractId) {
		ResponseEntity<Contract> result = restForContract.getForEntity(COTOS_CONTRACT_URL + "/contract/" + contractId, Contract.class);
		return result == null ? null : result.getBody();
	}

	/**
	 * 契約情報更新APIを呼び出す
	 *
	 * @param conttact
	 *            契約情報
	 */
	public void callContractApi(Contract contract) {
		restForContract.put(COTOS_CONTRACT_URL + "/contract", contract, Void.class);
	}

}
