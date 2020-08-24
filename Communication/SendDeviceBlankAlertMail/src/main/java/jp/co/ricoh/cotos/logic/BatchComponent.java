package jp.co.ricoh.cotos.logic;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.component.IBatchStepComponent;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.SearchMailTargetDto;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BatchComponent {

	private final ApplicationContext context;

	@Autowired
	@Qualifier("BASE")
	BatchStepComponent baseComponent;

	private final long CONTROL_ID = 3100;

	/**
	 * バッチ処理
	 * @return
	 * @throws Exception 
	 */
	public void execute(String[] args) throws Exception {
		// パラメータチェック
		String serviceTermStart = baseComponent.paramCheck(args);

		IBatchStepComponent component = this.getComponentInstance("SIM");

		List<SearchMailTargetDto> serchMailTargetDtoList = component.getDataList(serviceTermStart);

		try {
			component.process(serchMailTargetDtoList, CONTROL_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
