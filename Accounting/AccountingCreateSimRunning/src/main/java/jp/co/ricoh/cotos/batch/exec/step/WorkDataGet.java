package jp.co.ricoh.cotos.batch.exec.step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.dto.result.SalesCalcResultWorkForCspRunning;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

/**
 *
 * ジョブステップ１：「対象データ取得」
 *
 * 売上計算結果WORK（未処理）のデータを対象として、データを抽出する。 条件：COTOS処理フラグ＝「0：未処理」
 * また、計上処理、および計上データ登録に必要な内容を、 契約、契約明細、品種（契約用）、汎用マスタ（消費税情報を取得）等のテーブルから取得する。
 */
@NoArgsConstructor
@Component
@Log4j
public class WorkDataGet {

	/** 契約データ作成用データ検索SQL */
	private final static String SQL_SALES_CALC_RESULT_WORK_FIND = "sql/FindSalesCalcResultWorkForSimRunning.sql";

	@Autowired
	CheckUtil checkUtil;

	@Autowired
	DBUtil dbUtil;

	/** 売上計算結果WORK（未処理）のデータのリスト */
	@Getter
	private List<SalesCalcResultWorkForCspRunning> salesCalcResultWorkForSimRunningList;

	/**
	 * 計上日付(yyyymmdd)
	 */
	@Setter
	private String baseDate;

	/**
	 * フィールド変数の初期化
	 */
	public void init() {
		this.salesCalcResultWorkForSimRunningList = null;
	}

	public JobStepResult execute() {

		// フィールド変数の初期化
		init();

		try {
			this.salesCalcResultWorkForSimRunningList = findSalesCalcResultWorkForCspRunningList(this.baseDate);
		} catch (Exception e) {
			List<ErrorInfo> error = checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "CannotIdentify", new String[] { "契約データ作成用データ" });
			error.stream().forEach(errorInfo -> log.fatal(errorInfo.getErrorId() + ":" + errorInfo.getErrorMessage(), e));
			log.warn("想定外のエラーが発生しました。");
			return JobStepResult.FAILURE;
		}

		return JobStepResult.SUCCESS;
	}

	/**
	 * 売上計算結果WORK（未処理）のデータをテーブルから取得
	 * 
	 * @return
	 */
	private List<SalesCalcResultWorkForCspRunning> findSalesCalcResultWorkForCspRunningList(String baseDate) {
		Map<String, Object> params = new HashMap<>();
		params.put("baseDate", baseDate);
		List<SalesCalcResultWorkForCspRunning> findSalesCalcResultWorkForCspRunningList = dbUtil.loadFromSQLFile(SQL_SALES_CALC_RESULT_WORK_FIND, SalesCalcResultWorkForCspRunning.class, params);
		return findSalesCalcResultWorkForCspRunningList;
	}
}
