package jp.co.ricoh.cotos.logic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.BatchConstants;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.logic.message.MessageUtil;
import jp.co.ricoh.cotos.util.ProcessErrorException;
import jp.co.ricoh.cotos.util.OperationDateException;
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
	 * バッチ終了ステータスは以下のようになっている
	 * 1：オーダーCSV作成処理全体が失敗→後続処理としてメール送信されない
	 * 2：容量変更の処理対象日でない→後続処理としてメール送信されない
	 * 3：オーダーCSV作成処理は成功したが、事後処理等に一部失敗が存在→正常終了したデータのみメール送信される
	 * 
	 * @param args
	 *            バッチパラメーターリスト
	 */
	public void run(String[] args) {

		try {
			log.info(messageUtil.createMessageInfo("BatchProcessStartInfo", new String[] { BatchConstants.BATCH_NAME }).getMsg());
			batchComponent.execute(args);
			log.info(messageUtil.createMessageInfo("BatchProcessEndInfo", new String[] { BatchConstants.BATCH_NAME }).getMsg());

		} catch (OperationDateException e) {
			// 処理日が規定の実行日でない場合、戻り値「2」で処理を終了する
			log.info(messageUtil.createMessageInfo("BatchProcessEndInfo", new String[] { BatchConstants.BATCH_NAME }).getMsg());
			System.exit(2);

		} catch (ProcessErrorException e) {
			// CSV出力後の後処理で失敗したデータが存在する場合、戻り値「3」で処理を終了する
			log.info(messageUtil.createMessageInfo("BatchProcessEndInfo", new String[] { BatchConstants.BATCH_NAME }).getMsg());
			System.exit(3);

		} catch (ErrorCheckException e) {
			e.getErrorInfoList().stream().forEach(errorInfo -> log.error(errorInfo.getErrorId() + ":" + errorInfo.getErrorMessage()));
			log.error(messageUtil.createMessageInfo("BatchProcessEndInfo", new String[] { BatchConstants.BATCH_NAME }).getMsg());
			System.exit(1);

		} catch (Exception e) {
			log.fatal("オーダーCSV作成処理に失敗しました。", e);
			System.exit(1);

		} catch (Throwable e) {
			log.fatal(messageUtil.createMessageInfo("BatchCannotCompleteByUnexpectedError", new String[] { BatchConstants.BATCH_NAME }).getMsg(), e);
			System.exit(1);

		}
	}
}
