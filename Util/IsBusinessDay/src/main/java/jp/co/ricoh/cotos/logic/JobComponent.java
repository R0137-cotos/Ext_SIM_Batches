package jp.co.ricoh.cotos.logic;

import java.util.Arrays;

import jp.co.ricoh.cotos.BatchConstants;
import jp.co.ricoh.cotos.UtilProvider;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.logic.message.MessageUtil;
import jp.co.ricoh.cotos.util.NotBusinessDayException;
import lombok.extern.log4j.Log4j;

@Log4j
public class JobComponent {

	/**
	 * 営業日判定ジョブの実行
	 * @param args バッチパラメーターリスト
	 */
	public void run(String[] args) {

		BatchComponent batchComponent = new BatchComponent();
		MessageUtil messageUtil = UtilProvider.getMessageUtil();

		try {
			log.info(messageUtil.createMessageInfo("BatchProcessStartInfo", new String[] { BatchConstants.BATCH_NAME }).getMsg());
			boolean isBusinessDay = batchComponent.execute(args);
			log.info(messageUtil.createMessageInfo("BatchProcessEndInfo", new String[] { BatchConstants.BATCH_NAME }).getMsg());
			if (!isBusinessDay) {
				throw new NotBusinessDayException();
			}

		} catch (ErrorCheckException e) {
			e.getErrorInfoList().stream().forEach(errorInfo -> log.error(errorInfo.getErrorId() + ":" + errorInfo.getErrorMessage()));
			log.error(e.toString());
			Arrays.asList(e.getStackTrace()).stream().forEach(s -> log.error(s));
			log.error(messageUtil.createMessageInfo("BatchProcessEndInfo", new String[] { BatchConstants.BATCH_NAME }).getMsg());
			System.exit(1);

		} catch (NotBusinessDayException e) {
			System.exit(2);

		} catch (Throwable e) {
			log.error(e.toString());
			Arrays.asList(e.getStackTrace()).stream().forEach(s -> log.error(s));
			log.fatal(messageUtil.createMessageInfo("BatchCannotCompleteByUnexpectedError", new String[] { BatchConstants.BATCH_NAME }).getMsg());
			System.exit(1);

		}
	}
}
