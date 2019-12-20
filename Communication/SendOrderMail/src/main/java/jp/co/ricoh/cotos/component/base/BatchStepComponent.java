package jp.co.ricoh.cotos.component.base;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.BatchConstants;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.component.IBatchStepComponent;

@Component("BASE")
public class BatchStepComponent implements IBatchStepComponent {

	@Autowired
	CheckUtil checkUtil;

	/**
	 * パラメーターチェック処理
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @return
	 */
	@Override
	public final void paramCheck(String[] args) {
		// TODO バッチパラメーターのチェックを実施
		if (null == args || args.length != 1) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "ParameterEmptyError", new String[] { BatchConstants.BATCH_PARAMETER_LIST_NAME }));
		}
	}

	/**
	 * 処理データ取得
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @param searchParam 処理データ取得用パラメーター
	 * @return 処理データリスト
	 */
	@Override
	public final List<String> getDataList(String searchParam) {
		// TODO ファイル読み込み、SQL等で処理データリストを取得
		List<String> dataList = new ArrayList<>();
		dataList.add("ROC");

		return dataList;
	}

	@Override
	public boolean dataCheck(List<String> dataList) {
		// TODO 処理データリストのチェックを実施
		return true;
	}

	@Override
	public boolean beforeProcess(Object param) {
		// TODO 事前処理を実施
		return true;
	}

	@Override
	public boolean process(Object param) {
		// TODO データ加工等の処理を実施
		return true;
	}

	@Override
	public boolean afterProcess(Object param) {
		// TODO 事後処理（ファイル出力、テーブルデータ更新等）を実施
		return true;
	}
}
