package jp.co.ricoh.cotos.component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import jp.co.ricoh.cotos.dto.ReplyOrderDto;

public interface IBatchStepComponent {

	/**
	 * パラメーターチェック処理
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @param args バッチ起動引数
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
	 * @param dataList データリスト
	 * @return
	 */
	public boolean dataCheck(List<String> dataList);

	/**
	 * 事前処理
	 * @param args バッチ起動引数
	 * @return
	 * @throws IOException
	 */
	public List<ReplyOrderDto> beforeProcess(String[] args) throws IOException;

	/**
	 * プロセス
	 * @param csvlist CSVリスト
	 * @throws JsonProcessingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void process(List<ReplyOrderDto> csvlist) throws JsonProcessingException, FileNotFoundException, IOException;

	/**
	 * 事後処理
	 * @param param パラメータ
	 */
	public void afterProcess(Object param);
}
