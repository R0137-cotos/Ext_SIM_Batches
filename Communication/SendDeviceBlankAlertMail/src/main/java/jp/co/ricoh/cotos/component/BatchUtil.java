package jp.co.ricoh.cotos.component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.commonlib.entity.common.MailSendHistory;
import jp.co.ricoh.cotos.commonlib.entity.common.MailSendHistory.MailSendType;
import jp.co.ricoh.cotos.commonlib.entity.master.AppMaster;
import jp.co.ricoh.cotos.commonlib.entity.master.MailControlMaster;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.commonlib.repository.common.MailSendHistoryRepository;
import jp.co.ricoh.cotos.commonlib.repository.master.AppMasterRepository;

@Component
public class BatchUtil {

	@Autowired
	AppMasterRepository appMasterRepository;

	@Autowired
	MailSendHistoryRepository mailSendHistoryRepository;

	@Autowired
	CheckUtil checkUtil;

	@Value("${cotos.disp.urls.domain}")
	private String dispDomain;

	@Value("${cotos.disp.urls.screenName}")
	private String dispScreenName;

	@Value("${cotos.mom.system.applicationId}")
	private String applicationId;

	/**
	 * 対象文書画面URL取得
	 *
	 * @param contractId 契約ID
	 * @return String 対象文書画面URL
	 */
	public String getTargetDocUrl(long contractId) {
		String systemId = getSystemId();
		if (systemId == null) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "EntityCheckNotNullError", new String[] { "システムID" }));
		}
		if ("cotos".equals(systemId)) {
			return this.dispDomain + this.dispScreenName + "/" + String.valueOf(contractId);
		} else {
			return this.dispDomain + systemId + "/" + this.dispScreenName + "/" + String.valueOf(contractId);
		}
	}

	/**
	 * ログインユーザシステムID取得
	 *
	 * @return システムID
	 */
	public String getSystemId() {
		AppMaster appMaster = appMasterRepository.findById(applicationId).orElse(null);
		if (appMaster == null) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "EntityCheckNotNullError", new String[] { "アプリマスタ" }));
		}
		return appMaster.getSystemMaster().getSystemId();
	}

	/**
	 * メール履歴テーブルを複数作成or更新します。
	 * この処理単位で1トランザクションです。
	 * @param transactionIdList 作成又は更新対象のトランザクションID
	 * @param mailControlMaster 履歴に紐づけるメール制御マスタオブジェクト
	 * @param mailSendType 未送信又はエラーを設定。（未送信 ⇒ 新規履歴作成、エラー ⇒ 未送信データをエラーに更新）
	 */
	@Transactional
	public void saveMailSendHistory(List<Long> transactionIdList, MailControlMaster mailControlMaster, MailSendType mailSendType) {

		if (MailSendType.未送信 == mailSendType) {
			transactionIdList.stream().forEach(tranId -> {
				createMailSendHistory(tranId, mailControlMaster);
			});
			return;
		}
		if (MailSendType.エラー == mailSendType) {
			List<MailSendHistory> mailSendHistory = mailSendHistoryRepository.findByMailControlMasterAndMailSendType(mailControlMaster, MailSendType.未送信);
			mailSendHistory.stream().forEach(m -> {
				updateMailSendHistoryError(m);
			});
		}
	}

	@Transactional
	public void saveMailSendHistory(Long transactionId, MailControlMaster mailControlMaster, MailSendType mailSendType) {

		if (MailSendType.未送信 == mailSendType) {
			createMailSendHistory(transactionId, mailControlMaster);
			return;
		}
		if (MailSendType.エラー == mailSendType) {
			List<MailSendHistory> mailSendHistoryList = mailSendHistoryRepository.findByTargetDataIdAndMailControlMasterAndMailSendType(transactionId, mailControlMaster, MailSendType.未送信);
			for (MailSendHistory mailSendHistory : mailSendHistoryList) {
				updateMailSendHistoryError(mailSendHistory);
		    }
		}
	}

	/**
	 * メール履歴テーブルを1件更新します。（1件毎の更新はSSとメール送信成功時のみ実施される想定です）
	 * この処理単位で1トランザクションです。
	 */
	@Transactional
	public void updateMailSendHistory(MailSendHistory mailSendHistory, MailSendType mailSendType, List<String> emailToList, List<String> emailCcList, List<String> emailBccList) {
		mailSendHistory.setMailSendType(mailSendType);
		mailSendHistory.setContactMailTo(emailToList.toString().replace("[", "").replace("]", ""));
		mailSendHistory.setContactMailCc(emailCcList.toString().replace("[", "").replace("]", ""));
		mailSendHistory.setContactMailBcc(emailBccList.toString().replace("[", "").replace("]", ""));
		mailSendHistory.setSendedAt(new Date());
		mailSendHistoryRepository.save(mailSendHistory);
	}

	/**
	 * メール履歴テーブル作成
	 * @param transactionId
	 * @param mailControlMaster
	 */
	private void createMailSendHistory(Long transactionId, MailControlMaster mailControlMaster) {
		MailSendHistory mailSendHistory = new MailSendHistory();
		mailSendHistory.setTargetDataId(transactionId);
		mailSendHistory.setMailControlMaster(mailControlMaster);
		mailSendHistory.setMailSendType(MailSendType.未送信);
		mailSendHistoryRepository.save(mailSendHistory);
	}

	/**
	 * メール履歴テーブル更新(エラー)
	 * @param mailSendHistory
	 */
	private void updateMailSendHistoryError(MailSendHistory mailSendHistory) {
		mailSendHistory.setMailSendType(MailSendType.エラー);
		mailSendHistoryRepository.save(mailSendHistory);
	}
}
