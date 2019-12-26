package jp.co.ricoh.cotos.component.base;

import java.nio.file.FileAlreadyExistsException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
	public final void paramCheck(String[] args) {
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
	public void process(Object param) {
		// データ加工等の処理を実施
	}

	@Override
	public void afterProcess(Object param) {
		// 事後処理（ファイル出力、テーブルデータ更新等）を実施
	}
}
