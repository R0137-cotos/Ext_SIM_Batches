package jp.co.ricoh.cotos.logic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.component.IBatchStepComponent;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BatchComponent {

	private final ApplicationContext context;

	@Autowired
	@Qualifier("BASE")
	BatchStepComponent baseComponent;

	/**
	 * バッチ処理
	 * @return
	 * @throws Exception 
	 */
	public void execute(String[] args) throws Exception {

	}

	/**
	 * 商材切替
	 * @param productDiv
	 * @return IBatchStepComponent
	 */
	public IBatchStepComponent getComponentInstance(String productDiv) {
		return (IBatchStepComponent) context.getBean("iBatchStepComponent", productDiv);
	}
}
