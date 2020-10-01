package jp.co.ricoh.cotos.component.base;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.co.ricoh.cotos.BatchConstants;
import jp.co.ricoh.cotos.UtilProvider;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.logic.businessday.BusinessDayUtil;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.component.IBatchStepComponent;

public class BatchStepComponent implements IBatchStepComponent {
	
	static final String PROCESS_DATE_FORMAT = "yyyyMMdd";

	/**
	 * パラメーターチェック処理
	 * @return
	 */
	@Override
	public void paramCheck(String[] args) {
		CheckUtil checkUtil = UtilProvider.getCheckUtil();

		if (null == args || args.length != 1) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "ParameterEmptyError", new String[] { BatchConstants.BATCH_PARAMETER_LIST_NAME }));
		}

		SimpleDateFormat sdFormat = new SimpleDateFormat(PROCESS_DATE_FORMAT);

		try {
			// スラッシュ区切りの場合削除する
			args[0] = args[0].replace("/", "");
			sdFormat.parse(args[0]);
		} catch (ParseException e) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "IllegalFormatError", new String[] { BatchConstants.BATCH_PARAMETER_LIST_NAME, PROCESS_DATE_FORMAT }));
		}
	}

	/**
	 * 処理データ取得
	 * @param searchParam 処理データ取得用パラメーター
	 * @return 処理データリスト
	 */
	@Override
	public List<String> getDataList(String searchParam) {
		return null;
	}

	@Override
	public boolean dataCheck(List<String> dataList) {
		return true;
	}

	@Override
	public boolean beforeProcess(Object param) {
		return true;
	}

	/**
	 * 営業日
	 * @param processDate 処理実行日
	 * @return true:営業日である, false:営業日でない
	 */
	@Override
	public boolean process(String processDate) {
		BusinessDayUtil businessDayUtil = UtilProvider.getBusinessDayUtil();
		CheckUtil checkUtil = UtilProvider.getCheckUtil();

		SimpleDateFormat sdFormat = new SimpleDateFormat(PROCESS_DATE_FORMAT);

		Date targetDate = null;

		try {
			// スラッシュ区切りの場合削除する
			processDate = processDate.replace("/", "");
			targetDate = sdFormat.parse(processDate);
		} catch (ParseException e) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "IllegalFormatError", new String[] { BatchConstants.BATCH_PARAMETER_LIST_NAME, PROCESS_DATE_FORMAT }));
		}

		return businessDayUtil.isBusinessDay(targetDate);
	}

	@Override
	public boolean afterProcess(Object param) {
		return true;
	}
}
