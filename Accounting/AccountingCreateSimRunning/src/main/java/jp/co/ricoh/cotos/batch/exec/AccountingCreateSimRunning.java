package jp.co.ricoh.cotos.batch.exec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.batch.exec.step.AccountingExecution;
import jp.co.ricoh.cotos.batch.exec.step.AccountingExecutionParameterCreate;
import jp.co.ricoh.cotos.batch.exec.step.JobStepResult;
import jp.co.ricoh.cotos.batch.exec.step.WorkDataGet;
import jp.co.ricoh.cotos.batch.util.AccountingCreateSimRunningUtil;
import jp.co.ricoh.cotos.commonlib.dto.result.SalesCalcResultWorkForCspRunning;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import lombok.extern.log4j.Log4j;

/**
 *
 * 計上データ作成（SIMランニング分）のビジネスロジック実行クラス
 *
 */
@Component
@Log4j
public class AccountingCreateSimRunning {

	@Autowired
	AccountingCreateSimRunningUtil appUtil;

	@Autowired
	CheckUtil checkUtil;

	/** 処理対象データ取得 */
	@Autowired
	WorkDataGet workData;

	/** 計上処理のパラメータ作成 */
	@Autowired
	AccountingExecutionParameterCreate accountingExecutionParameterCreate;

	/** トランザクション処理（ 計上処理→対象契約データ更新→対象契約データ更新） */
	@Autowired
	AccountingExecution accountingExecution;

	/** 処理対象件数 */
	private long processTargetCnt = 0L;

	/** 処理完了件数 */
	private long processCompleteCnt = 0L;
	
	/** 処理エラー件数 */
	private long processErrorCnt = 0L;

	/**
	 * 計上データ作成（SIMランニング分）のビジネスロジック実行
	 * 
	 * @param args
	 *            バッチ起動パラメータ
	 *
	 *            ①処理開始日（yyyyMMdd）
	 */
	public void execute(String args[]) {

		log.info("!!!! 計上データ作成（SIMランニング分）を開始します。 !!!!");

		// パラメータチェック
		if (false == appUtil.existsDate(args[0])) {
			List<ErrorInfo> error = checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "SearchParameterError", new String[] { "起動パラメータ", "存在する日付（yyyyMMdd）" });
			error.stream().forEach(errorInfo -> log.fatal(errorInfo.getErrorId() + ":" + errorInfo.getErrorMessage()));
			System.exit(1);
		}

		// 対象データ取得
		workData.setBaseDate(args[0]);
		if (JobStepResult.SUCCESS != workData.execute()) {
			log.info("「1 対象データ取得」に失敗しました。");
			System.exit(1);
		}

		if (0 == workData.getSalesCalcResultWorkForSimRunningList().size()) {
			log.info("処理対象データが0件でした。処理を終了します。");
			return;
		}

		this.processTargetCnt = workData.getSalesCalcResultWorkForSimRunningList().size();
		log.info("処理対象件数：" + this.processTargetCnt);

		workData.getSalesCalcResultWorkForSimRunningList().stream().forEach(work -> {
			if (executeTransactionProcess(work, args[0])) {
				accountingExecution.updateContractDetail(JobStepResult.SUCCESS);
				log.info(String.format("RJ管理番号%sの計上データ作成に成功しました。", work.getRjManageNumber()));
				this.processCompleteCnt++;
			} else {
				accountingExecution.updateContractDetail(JobStepResult.FAILURE);
				log.error(String.format("RJ管理番号%sの計上データ作成に失敗しました。", work.getRjManageNumber()));
				this.processErrorCnt++;
			}
		});

		log.info("処理正常終了件数：" + this.processCompleteCnt);
		if (processErrorCnt == 0) {
			log.info("!!!! 計上データ作成（SIMランニング分）を正常終了します。 !!!!");
		} else {
			log.error("!!!! 計上データ作成（SIMランニング分）を一部正常終了します。 !!!!");
			System.exit(2);
		}

		return;
	}

	/**
	 * トランザクション実行
	 *
	 * 計上処理 → 契約明細更新
	 *
	 * @param work
	 *            処理対象のデータ
	 * @param baseDate
	 *            基準日（yyyyMMdd）
	 * @return
	 * @throws Exception
	 */
	private boolean executeTransactionProcess(SalesCalcResultWorkForCspRunning work, String baseDate) {
		Date execDate = new Date();
		// 計上処理実行（トランザクション）
		accountingExecution.setAccounting(accountingExecutionParameterCreate.createParameter(work, baseDate, execDate));
		JobStepResult result = JobStepResult.FAILURE;
		try {
			result = accountingExecution.execute();
		} catch (Exception e) {
			log.error(e.toString());
			Arrays.asList(e.getStackTrace()).stream().forEach(s -> log.error(s));
		}
		return result == JobStepResult.SUCCESS;
	}

}
