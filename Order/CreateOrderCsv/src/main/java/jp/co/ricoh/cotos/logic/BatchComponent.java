package jp.co.ricoh.cotos.logic;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.component.IBatchStepComponent;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.CreateOrderCsvDataDto;
import jp.co.ricoh.cotos.dto.CreateOrderCsvDto;
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
	 * @return
	 * @throws Exception
	 */
	public void execute(String[] args) throws Exception {
		// パラメータチェック
		CreateOrderCsvDto dto = baseComponent.paramCheck(args);

		IBatchStepComponent component = this.getComponentInstance("SIM");
		String contractType = null;
		if ("1".equals(dto.getType())) {
			contractType = "'$.extendsParameterList?(@.contractType == \"新規\")'";
		} else if ("2".equals(dto.getType())) {
			contractType = "'$.extendsParameterList?(@.contractType == \"容量変更\")'";
		} else if ("3".equals(dto.getType())) {
			contractType = "'$.extendsParameterList?(@.contractType == \"有償交換\")'";
		}
		List<CreateOrderCsvDataDto> orderDataList = component.getDataList(contractType);
		component.process(dto, orderDataList);
	}

	/**
	 * 商材切替
	 * 
	 * @param productDiv
	 * @return IBatchStepComponent
	 */
	public IBatchStepComponent getComponentInstance(String productDiv) {
		return (IBatchStepComponent) context.getBean("iBatchStepComponent", productDiv);
	}
}
