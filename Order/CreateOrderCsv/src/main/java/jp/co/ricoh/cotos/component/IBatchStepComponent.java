package jp.co.ricoh.cotos.component;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.text.ParseException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import jp.co.ricoh.cotos.dto.CreateOrderCsvDataDto;
import jp.co.ricoh.cotos.dto.CreateOrderCsvDto;

public interface IBatchStepComponent {

	/**
	 * パラメーターチェック処理
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @return
	 * @throws FileAlreadyExistsException
	 */
	public CreateOrderCsvDto paramCheck(String[] args) throws FileAlreadyExistsException;

	/**
	 * 処理データ取得
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @param searchParam
	 *            処理データ取得用パラメーター
	 * @return 処理データリスト
	 */
	public List<CreateOrderCsvDataDto> getDataList(String contractType);

	/**
	 * データチェック処理
	 * 
	 * @return
	 */
	public boolean dataCheck(List<String> dataList);

	/**
	 * 事前処理
	 * 
	 * @return
	 */
	public void beforeProcess(Object param);

	/**
	 * プロセス
	 * 
	 * @return
	 * @throws NoSuchFileException
	 * @throws FileAlreadyExistsException
	 * @throws Exception
	 */
	public boolean process(CreateOrderCsvDto dto, List<CreateOrderCsvDataDto> orderDataList) throws ParseException, JsonProcessingException, IOException;

	/**
	 * 事後処理
	 * 
	 * @return
	 */
	public void afterProcess(Object param);
}
