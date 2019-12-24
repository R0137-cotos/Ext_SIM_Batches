package jp.co.ricoh.cotos.component;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import lombok.extern.log4j.Log4j;

/**
 * 商材固有処理切替クラス
 */
@Configuration
@Log4j
public class DynamicProductConfig {

	@Autowired
	Map<String, IBatchStepComponent> batchStepComponentMap;

	/**
	 * 商材によってそれぞれのBeanを返却する
	 * @param product
	 * @return contractComponent
	 */
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public IBatchStepComponent iBatchStepComponent(String productDiv) {

		if (null == productDiv) {
			log.info("商材切替：標準");
			return batchStepComponentMap.get("BASE");
		}

		return batchStepComponentMap.get(productDiv);
	}
}