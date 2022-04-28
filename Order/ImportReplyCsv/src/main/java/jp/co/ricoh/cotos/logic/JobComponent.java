package jp.co.ricoh.cotos.logic;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.BatchConstants;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.logic.message.MessageUtil;
import jp.co.ricoh.cotos.util.DeliveryExpectedDateException;
import jp.co.ricoh.cotos.util.ProcessErrorException;
import lombok.extern.log4j.Log4j;

@Component
@Log4j
public class JobComponent {

	@Autowired
	BatchComponent batchComponent;

	@Autowired
	private MessageUtil messageUtil;

	/**
	 * バッチを順次実行するジョブの実行
	 *
	 * @param args
	 *            バッチパラメーターリスト
	 */
	public void run(String[] args) {

		try {
			log.info(messageUtil.createMessageInfo("BatchProcessStartInfo", new String[] { BatchConstants.BATCH_NAME }).getMsg());
			batchComponent.execute(args);
			log.info(messageUtil.createMessageInfo("BatchProcessEndInfo", new String[] { BatchConstants.BATCH_NAME }).getMsg());

		} catch (DeliveryExpectedDateException e) {
			log.warn(e.toString());
			Arrays.asList(e.getStackTrace()).stream().forEach(s -> log.warn(s));
			log.warn("リプライCSV取込処理が一部失敗しました。");
			System.exit(2);

		} catch (ProcessErrorException e) {
			log.warn(e.toString());
			Arrays.asList(e.getStackTrace()).stream().forEach(s -> log.warn(s));
			log.warn("リプライCSV取込処理が一部失敗しました。");
			System.exit(2);

		} catch (ErrorCheckException e) {
			e.getErrorInfoList().stream().forEach(errorInfo -> log.error(errorInfo.getErrorId() + ":" + errorInfo.getErrorMessage()));
			log.error(e.toString());
			Arrays.asList(e.getStackTrace()).stream().forEach(s -> log.error(s));
			log.error(messageUtil.createMessageInfo("BatchProcessEndInfo", new String[] { BatchConstants.BATCH_NAME }).getMsg());
			System.exit(1);

		} catch (Exception e) {
			log.error(e.toString());
			Arrays.asList(e.getStackTrace()).stream().forEach(s -> log.error(s));
			log.error("リプライCSV取込処理に失敗しました。");
			System.exit(1);

		} catch (Throwable e) {
			log.fatal(e.toString());
			Arrays.asList(e.getStackTrace()).stream().forEach(s -> log.fatal(s));
			log.fatal(messageUtil.createMessageInfo("BatchCannotCompleteByUnexpectedError", new String[] { BatchConstants.BATCH_NAME }).getMsg());
			System.exit(1);

		}
	}
}
