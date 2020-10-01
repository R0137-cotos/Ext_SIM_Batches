package jp.co.ricoh.cotos.component;

import java.util.List;

public interface IBatchStepComponent {


	/**
	 * パラメーターチェック処理
	 * @return
	 */
	public void paramCheck(String[] args);

	/**
	 * 処理データ取得
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
	 * @param 処理実行日
	 * @return
	 */
	public boolean process(String processDate);

	/**
	 * 事後処理
	 * @return
	 */
	public boolean afterProcess(Object param);
}
