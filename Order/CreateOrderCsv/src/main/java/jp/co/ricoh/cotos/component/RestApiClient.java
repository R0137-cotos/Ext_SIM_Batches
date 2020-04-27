package jp.co.ricoh.cotos.component;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jp.co.ricoh.cotos.commonlib.entity.arrangement.ArrangementWork;
import lombok.Setter;

@Component
public class RestApiClient {

	@Setter
	@Autowired
	@Qualifier("forArrangementApi")
	RestTemplate restForArrangement;

	@Value("${cotos.arrangement.url}")
	String COTOS_ARRANGEMENT_URL;

	@Value("${cotos.mom.superUserId}")
	String COTOS_MOM_SUPERUSERID;

	/**
	 * 契約情報更新APIを呼び出す
	 * 
	 * @param dto
	 */
	public void callCompleteArrangement(long arrangementWorkId) {
		restForArrangement.patchForObject(COTOS_ARRANGEMENT_URL + "/arrangementWork/" + arrangementWorkId + "/completeWork", null, Void.class);
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

}
