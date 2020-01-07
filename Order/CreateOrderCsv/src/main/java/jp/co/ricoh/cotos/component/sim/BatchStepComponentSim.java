package jp.co.ricoh.cotos.component.sim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.commonlib.logic.mail.CommonSendMail;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import lombok.extern.log4j.Log4j;

@Component("SIM")
@Log4j
public class BatchStepComponentSim extends BatchStepComponent {

	@Autowired
	CommonSendMail commonSendMail;

	@Override
	public void process(Object param) {
		log.info("SIM独自処理");
	}
}
