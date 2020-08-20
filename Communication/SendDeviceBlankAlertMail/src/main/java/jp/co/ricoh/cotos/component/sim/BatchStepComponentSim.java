package jp.co.ricoh.cotos.component.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.entity.EnumType.ServiceCategory;
import jp.co.ricoh.cotos.commonlib.entity.common.MailSendHistory;
import jp.co.ricoh.cotos.commonlib.entity.common.MailSendHistory.MailSendType;
import jp.co.ricoh.cotos.commonlib.logic.mail.CommonSendMail;
import jp.co.ricoh.cotos.commonlib.repository.common.MailSendHistoryRepository;
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
	MailSendHistoryRepository mailSendHistoryRepository;

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

		// メール送信対象のデータを送信履歴テーブルへ登録する
		List<Long> transactionIdList = resultIdList.stream().map(e -> e.longValue()).collect(Collectors.toList()); // 何故かBigDecimal型で返るので型変換
		batchUtil.saveMailSendHistory(transactionIdList, mailControlMaster, MailSendType.未送信);

		sendMailAndSaveHistory(serchMailTargetDto);

	}

	/**
	 * メイン処理（メール送信 ⇒ 履歴登録）
	 * この処理内で発生するエラーはすべてキャッチされログ等に出力されず次のループへ流れます。
	 * @param mailControlMaster
	 * @param entity
	 * @param transactionId
	 */
	private void sendMailAndSaveHistory(SearchMailTargetDto serchMailTargetDto) {
		List<String> mailAddressList = new ArrayList<String>();
		mailAddressList.add(serchMailTargetDto.getMailAddress());
		List<String> mailAddressBccList = new ArrayList<String>();
		mailAddressBccList.add(AUDIT_TRAIL_MAIL_ADDRESS);
		List<String> mailTextRepalceValueList = new ArrayList<String>();
		mailTextRepalceValueList.add(batchUtil.getTargetDocUrl(serchMailTargetDto.getContractId()));
		try {
			commonSendMail.findMailTemplateMasterAndSendMail(ServiceCategory.契約, "17", serchMailTargetDto.getProductGrpMasterId(), mailAddressList, new ArrayList<String>(), mailAddressBccList, new ArrayList<String>(), mailTextRepalceValueList, null);

			// 送信履歴を更新
			MailSendHistory mailSendHistory = mailSendHistoryRepository.findByTargetDataIdAndMailControlMasterAndMailSendType(transactionId, mailControlMaster, MailSendType.未送信);
			batchUtil.updateMailSendHistory(mailSendHistory, MailSendType.完了, mailAddressList, new ArrayList<String>(), mailAddressBccList);
		} catch (MessagingException e) {
			log.fatal("メール送信処理に失敗しました。");
			throw new Exception(e);
		}

	}
}
