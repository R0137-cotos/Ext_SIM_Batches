package jp.co.ricoh.cotos;

import java.util.Map;

import org.springframework.context.ApplicationContext;

import jp.co.ricoh.cotos.commonlib.logic.businessday.BusinessDayUtil;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.commonlib.logic.message.MessageUtil;
import jp.co.ricoh.cotos.commonlib.repository.master.BusinessCalendarRepository;
import jp.co.ricoh.cotos.commonlib.repository.master.NonBusinessDayCalendarMasterRepository;
import jp.co.ricoh.cotos.config.LoadConfigulation;

/**
 *
 * Utilクラスのインスタンスを生成、返却する
 *
 */
public class UtilProvider {

	private static MessageUtil messageUtil;

	private static CheckUtil checkUtil;

	private static BusinessDayUtil businessDayUtil;

	private UtilProvider() {
	}

	/**
	 * MessageUtilを取得
	 * @return MessageUtil
	 */
	public static MessageUtil getMessageUtil() {
		if (messageUtil == null) {
			messageUtil = createMessageUtil();
		}
		return messageUtil;
	}

	/**
	 * CheckUtilを取得
	 * @return CheckUtil
	 */
	public static CheckUtil getCheckUtil() {
		if (checkUtil == null) {
			checkUtil = createCheckUtil();
		}
		return checkUtil;
	}

	public static BusinessDayUtil getBusinessDayUtil() {
		if (businessDayUtil == null) {
			businessDayUtil = createBusinessDayUtil();
		}
		return businessDayUtil;
	}

	/**
	 * MessageUtilを生成
	 * @return MessageUtil
	 */
	private static MessageUtil createMessageUtil() {

		MessageUtil messageUtil = new MessageUtil();
		Map<String, String> messageProperties = LoadConfigulation.getMessageProperties();
		messageUtil.setMessageSource(messageProperties.get("basename"), messageProperties.get("encoding"));

		return messageUtil;
	}

	/**
	 * CheckUtilを生成
	 * @return CheckUtil
	 */
	private static CheckUtil createCheckUtil() {

		CheckUtil checkUtil = new CheckUtil();
		Map<String, String> messageProperties = LoadConfigulation.getMessageProperties();
		checkUtil.setMessageUtil(messageProperties.get("basename"), messageProperties.get("encoding"));

		return checkUtil;
	}

	/**
	 * BusinessDayUtilを生成
	 * @return BusinessDayUtil
	 */
	private static BusinessDayUtil createBusinessDayUtil() {
		BusinessDayUtil businessDayUtil = new BusinessDayUtil();
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		// Repository取得
		NonBusinessDayCalendarMasterRepository nonBusinessDayCalendarMasterRepository = context.getBean(NonBusinessDayCalendarMasterRepository.class);
		businessDayUtil.setNonBusinessDayCalendarMasterRepository(nonBusinessDayCalendarMasterRepository);
		BusinessCalendarRepository businessCalendarRepository = context.getBean(BusinessCalendarRepository.class);
		businessDayUtil.setBusinessCalendarRepository(businessCalendarRepository);

		return businessDayUtil;
	}
}
