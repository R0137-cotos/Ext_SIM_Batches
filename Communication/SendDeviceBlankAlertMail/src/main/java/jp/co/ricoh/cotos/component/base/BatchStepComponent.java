package jp.co.ricoh.cotos.component.base;

import java.nio.file.FileAlreadyExistsException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.BatchConstants;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.commonlib.repository.master.ProductGrpMasterRepository;
import jp.co.ricoh.cotos.component.IBatchStepComponent;

@Component("BASE")
public class BatchStepComponent implements IBatchStepComponent {

	@Autowired
	CheckUtil checkUtil;

	@Autowired
	ProductGrpMasterRepository productGrpMasterRepository;

	/**
	 * パラメーターチェック処理
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @return
	 * @throws FileAlreadyExistsException 
	 */
	@Override
	public final String paramCheck(String[] args) {
		String serviceTermStart;

		String operationDate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		// バッチパラメーターのチェックを実施
		if (null == args || args.length == 0) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(getSysdate());
			calendar.add(Calendar.DAY_OF_MONTH, 14);
			operationDate = sdf.format(calendar.getTime());
		} else if (args.length > 1) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "ParameterEmptyError", new String[] { BatchConstants.BATCH_PARAMETER_LIST_NAME }));
		} else if (args.length == 1) {
			operationDate = args[0];
			try {
				sdf.parse(operationDate);
			} catch (ParseException pe) {
				throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "IllegalFormatError", new String[] { "日付", "ｙｙｙMMｄｄ" }));
			}
		}
		serviceTermStart = operationDate;
		return serviceTermStart;
	}

	/**
	 * 日付取得
	 * @return
	 */
	public Date getSysdate() {
		return new Date();
	}

	/**
	 * 処理データ取得
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @param searchParam 処理データ取得用パラメーター
	 * @return 処理データリスト
	 */
	@Override
	public final List<String> getDataList(String searchParam) {
		// ファイル読み込み、SQL等で処理データリストを取得
		return null;
	}

	@Override
	public boolean dataCheck(List<String> dataList) {
		// 処理データリストのチェックを実施
		return true;
	}

	@Override
	public void beforeProcess(Object param) {
		// 事前処理を実施
	}

	@Override
	public void process(String serviceTermStart) throws Exception {
		// データ加工等の処理を実施
	}

	@Override
	public void afterProcess(Object param) {
		// 事後処理（ファイル出力、テーブルデータ更新等）を実施
	}
}
