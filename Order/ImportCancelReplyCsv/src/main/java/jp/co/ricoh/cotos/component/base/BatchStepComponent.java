package jp.co.ricoh.cotos.component.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

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
	 * @throws FileAlreadyExistsException 
	 */
	@Override
	public final void paramCheck(String[] args) {

		// バッチパラメーターのチェックを実施
		if (null == args || args.length != 2) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "ParameterEmptyError", new String[] { BatchConstants.BATCH_PARAMETER_LIST_NAME }));
		}

		File csvFile = Paths.get(args[0], args[1]).toFile();

		// フォルダが存在しない場合はエラー
		if (!csvFile.getParentFile().exists()) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "DirectoryNotFoundError"));
		}
		// 既にファイルが存在する場合はエラー
		if (!csvFile.exists()) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "FileNotFoundError"));
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
	public void process(String[] params)throws JsonProcessingException, FileNotFoundException, IOException{
		// データ加工等の処理を実施
	}

	@Override
	public void afterProcess(Object param) {
		// 事後処理（ファイル出力、テーブルデータ更新等）を実施
	}
}
