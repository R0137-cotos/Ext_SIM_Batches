package jp.co.ricoh.cotos.logic;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.component.IBatchStepComponent;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.CancelOrderEntity;
import jp.co.ricoh.cotos.dto.CreateOrderCsvParameter;
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
	 * 
	 * @throws Exception
	 */
	@Transactional
	public void execute(String[] args) throws Exception {
		// パラメータチェック
		CreateOrderCsvParameter param = baseComponent.paramCheck(args);

		// SIM固定 他商材についてコンポーネントを作成した場合はここで分岐すること
		IBatchStepComponent component = this.getComponentInstance("SIM");
		List<CancelOrderEntity> orderDataList = component.getCancelOrder();
		component.process(param, orderDataList);
	}

	/**
	 * 商材切替
	 * 
	 * @param productDiv　商材区分
	 * @return IBatchStepComponent
	 */
	public IBatchStepComponent getComponentInstance(String productDiv) {
		return (IBatchStepComponent) context.getBean("iBatchStepComponent", productDiv);
	}
}
