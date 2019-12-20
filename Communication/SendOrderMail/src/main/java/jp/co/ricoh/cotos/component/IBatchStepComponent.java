package jp.co.ricoh.cotos.component;

import java.util.List;

public interface IBatchStepComponent {


	/**
	 * パラメーターチェック処理
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @return
	 */
	public void paramCheck(String[] args);

	/**
	 * 処理データ取得
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @param searchParam 処理データ取得用パラメーター
	 * @return 処理データリスト
	 */
	public List<String> getDataList(String searchParam);

	/**
	 * データチェック処理
	 * @return
	 */
	public boolean dataCheck(List<String> dataList);

	/**
	 * 事前処理
	 * @return
	 */
	public boolean beforeProcess(Object param);

	/**
	 * プロセス
	 * @return
	 */
	public boolean process(Object param);

	/**
	 * 事後処理
	 * @return
	 */
	public boolean afterProcess(Object param);
}
