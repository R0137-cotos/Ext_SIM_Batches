package jp.co.ricoh.cotos.component.roc;

import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import lombok.extern.log4j.Log4j;

@Component("ROC")
@Log4j
public class BatchStepComponentRoc extends BatchStepComponent {

	@Override
	public boolean process(Object param) {
		// TODO 自動生成されたメソッド・スタブ
		log.info("ROC独自処理");
		return true;
	}
}
