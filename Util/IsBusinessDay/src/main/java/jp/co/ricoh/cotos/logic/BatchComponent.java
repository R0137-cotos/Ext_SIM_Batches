package jp.co.ricoh.cotos.logic;

import jp.co.ricoh.cotos.component.IBatchStepComponent;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.component.csp.BatchStepComponentCsp;
import jp.co.ricoh.cotos.component.edw.BatchStepComponentEdw;
import jp.co.ricoh.cotos.component.roc.BatchStepComponentRoc;

public class BatchComponent {

	/**
	 * バッチ処理
	 * @return
	 */
	public boolean execute(String[] args) {

		BatchStepComponent baseComponent = new BatchStepComponent();
		baseComponent.paramCheck(args);

		IBatchStepComponent component = this.getComponentInstance("BASE");
		return component.process(args[0]);
	}

	/**
	 * 商材切替
	 * @param productDiv
	 * @return IBatchStepComponent
	 */
	public IBatchStepComponent getComponentInstance(String productDiv) {
		switch (productDiv) {
		case "ROC":
			return new BatchStepComponentRoc();
		case "CSP":
			return new BatchStepComponentCsp();
		case "EDW":
			return new BatchStepComponentEdw();
		default:
			return new BatchStepComponent();
		}
	}
}
