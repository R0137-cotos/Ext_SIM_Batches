package jp.co.ricoh.cotos.component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.ricoh.cotos.commonlib.entity.arrangement.ArrangementWork;
import jp.co.ricoh.cotos.commonlib.security.CotosAuthenticationDetails;
import lombok.Setter;

@Component
public class BatchUtil {

	@Setter
	@Autowired
	@Qualifier("forArrangementApi")
	RestTemplate restForArrangement;

	@Value("${cotos.arrangement.url}")
	String COTOS_ARRANGEMENT_URL;

	@Autowired
	ObjectMapper om;

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
		CotosAuthenticationDetails userInfo = (CotosAuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		restForArrangement.postForObject(COTOS_ARRANGEMENT_URL + "/arrangementWork/assignWorker?workerMomEmpId=" + userInfo.getMomEmployeeId(), arrangementWorkIdList, Void.class);
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
	 * 文字列を日付に変換する
	 *
	 * @param yyyyMMdd
	 *            文字列の日付
	 * @return Date型の日付
	 */
	public Date changeDate(String yyyyMMdd) {
		try {
			return new SimpleDateFormat("yyyyMMdd").parse(yyyyMMdd);
		} catch (ParseException e1) {
			return null;
		}
	}

	public int getOrderCsvCreationStatus(String extendsParameter) throws JsonProcessingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(extendsParameter);
		return node.get("orderCsvCreationStatus").asInt();
	}
}
