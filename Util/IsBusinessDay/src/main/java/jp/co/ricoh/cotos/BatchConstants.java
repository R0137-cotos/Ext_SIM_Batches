package jp.co.ricoh.cotos;

public class BatchConstants {

	/**
	 * バッチ名称
	 */
	public static final String BATCH_NAME = "バッチテンプレートバッチ";

	/**
	 * バッチパラメーター
	 */
	public static final String BATCH_PARAMETER_LIST_NAME = "処理年月日";

	/**
	 * 商材
	 */
	public enum ProductDiv {
		BASE, // COTOS標準
		EDW, // EMPOWERING DIGITAL WORKPLACES（旧RSI：リコースマートインテグレーション）
		CSP, // サイバーセキュリティパック
		ROC, // リコーワンストップクラウド
	}
}
