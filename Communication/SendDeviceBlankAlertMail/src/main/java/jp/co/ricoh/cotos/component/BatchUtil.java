package jp.co.ricoh.cotos.component;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.commonlib.entity.master.AppMaster;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.commonlib.repository.master.AppMasterRepository;

@Component
public class BatchUtil {

	@Autowired
	AppMasterRepository appMasterRepository;

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
		AppMaster appMaster = appMasterRepository.findOne(applicationId);
		if (appMaster == null) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "EntityCheckNotNullError", new String[] { "アプリマスタ" }));
		}
		return appMaster.getSystemMaster().getSystemId();
	}
}
