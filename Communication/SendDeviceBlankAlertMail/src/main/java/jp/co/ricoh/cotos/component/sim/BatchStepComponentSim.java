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
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.SearchMailTargetDto;
import jp.co.ricoh.cotos.dto.SendDeviceBlankAlertMailDto;
import lombok.extern.log4j.Log4j;

@Component("SIM")
@Log4j
public class BatchStepComponentSim extends BatchStepComponent {

	@Autowired
	DBUtil dbUtil;

	@Autowired
	CommonSendMail commonSendMail;

	@Override
	public void process(SendDeviceBlankAlertMailDto dto) throws Exception {
		log.info("SIM独自処理");

		Map<String, Object> sqlParams = new HashMap<String, Object>();
		sqlParams.put("testdate", dto.getDate());
		List<SearchMailTargetDto> serchMailTargetDtoList = dbUtil.loadFromSQLFile("sql/searchMailTargetList.sql", SearchMailTargetDto.class, sqlParams);
		List<String> mailAddressList;
		try {
			for (SearchMailTargetDto serchMailTargetDto : serchMailTargetDtoList) {
				mailAddressList = new ArrayList<String>();
				mailAddressList.add(serchMailTargetDto.getMailAddress());
				commonSendMail.findMailTemplateMasterAndSendMail(ServiceCategory.契約, "17", serchMailTargetDto.getProductGrpMasterId(), mailAddressList, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), null);
			}
		} catch (MessagingException e) {
			log.fatal("メール送信処理に失敗しました。");
			throw new Exception(e);
		}

	}
}
