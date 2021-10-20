package jp.co.ricoh.cotos.component;

import java.nio.file.FileAlreadyExistsException;
import java.util.List;

import jp.co.ricoh.cotos.dto.SearchMailTargetDto;

public interface IBatchStepComponent {

	/**
	 * パラメーターチェック処理
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @return
	 * @throws FileAlreadyExistsException 
	 */
	public String paramCheck(String[] args);

	/**
	 * 処理データ取得
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @param searchParam 処理データ取得用パラメーター
	 * @return 処理データリスト
	 */
	public List<SearchMailTargetDto> getDataList(String searchParam);

	/**
	 * データチェック処理
	 * @return
	 */
	public boolean dataCheck(List<String> dataList);

	/**
	 * 事前処理
	 * @return
	 */
	public void beforeProcess(Object param);

	/**
	 * プロセス
	 * @return
	 * @throws Exception 
	 */
	public boolean process(List<SearchMailTargetDto> serchMailTargetDtoList, long mailControlMasterId) throws Exception;

	/**
	 * 事後処理
	 * @return
	 */
	public void afterProcess(Object param);
}
