package jp.co.ricoh.cotos.component.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.entity.EnumType.ServiceCategory;
import jp.co.ricoh.cotos.commonlib.logic.mail.CommonSendMail;
import jp.co.ricoh.cotos.component.BatchUtil;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.SearchMailTargetDto;
import lombok.extern.log4j.Log4j;

@Component("SIM")
@Log4j
public class BatchStepComponentSim extends BatchStepComponent {

	@Autowired
	DBUtil dbUtil;

	@Autowired
	CommonSendMail commonSendMail;

	@Autowired
	BatchUtil batchUtil;

	public final static String AUDIT_TRAIL_MAIL_ADDRESS = "customer_send_history@cotos.ricoh.co.jp";

	/**
	 * 処理データ取得
	 * @param searchParam 処理データ取得用パラメーター
	 * @return 処理データリスト
	 */
	@Override
	public final List<SearchMailTargetDto> getDataList(String serviceTermStart) {
		Map<String, Object> sqlParams = new HashMap<String, Object>();
		sqlParams.put("serviceTermStart", serviceTermStart);
		List<SearchMailTargetDto> serchMailTargetDtoList = dbUtil.loadFromSQLFile("sql/searchMailTargetList.sql", SearchMailTargetDto.class, sqlParams);
		return serchMailTargetDtoList;
	}

	@Override
	public void process(SearchMailTargetDto serchMailTargetDto) throws Exception {
		log.info("SIM独自処理");

		List<String> mailAddressList = new ArrayList<String>();
		mailAddressList.add(serchMailTargetDto.getMailAddress());
		List<String> mailAddressBccList = new ArrayList<String>();
		mailAddressBccList.add(AUDIT_TRAIL_MAIL_ADDRESS);
		List<String> mailTextRepalceValueList = new ArrayList<String>();
		mailTextRepalceValueList.add(batchUtil.getTargetDocUrl(serchMailTargetDto.getContractId()));
		try {
			commonSendMail.findMailTemplateMasterAndSendMail(ServiceCategory.契約, "17", serchMailTargetDto.getProductGrpMasterId(), mailAddressList, new ArrayList<String>(), mailAddressBccList, new ArrayList<String>(), mailTextRepalceValueList, null);
		} catch (MessagingException e) {
			log.fatal("メール送信処理に失敗しました。");
			throw new Exception(e);
		}
	}
}
