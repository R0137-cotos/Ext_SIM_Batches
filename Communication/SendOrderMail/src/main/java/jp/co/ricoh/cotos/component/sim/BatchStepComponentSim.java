package jp.co.ricoh.cotos.component.sim;

import java.util.ArrayList;

import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.commonlib.dto.parameter.communication.BounceMailHeaderDto;
import jp.co.ricoh.cotos.commonlib.entity.EnumType.ServiceCategory;
import jp.co.ricoh.cotos.commonlib.logic.mail.CommonSendMail;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.SendOrderMailDto;
import lombok.extern.log4j.Log4j;

@Component("SIM")
@Log4j
public class BatchStepComponentSim extends BatchStepComponent {

	@Autowired
	CommonSendMail commonSendMail;

	@Override
	public void process(SendOrderMailDto dto) throws Exception {
		log.info("SIM独自処理");

		try {
			commonSendMail.findMailTemplateMasterAndSendMail(ServiceCategory.手配, "0", dto.getProductGrpMasterId(), dto.getMailAddressList(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), dto.getCsvFile(), new BounceMailHeaderDto());
		} catch (MessagingException e) {
			log.fatal("メール送信処理に失敗しました。");
			throw new Exception(e);
		}

	}
}
