package jp.co.ricoh.cotos.component;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.text.ParseException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import jp.co.ricoh.cotos.dto.CancelOrderEntity;
import jp.co.ricoh.cotos.dto.CreateOrderCsvParameter;

public interface IBatchStepComponent {

	/**
	 * バッチ処理引数チェック処理
	 * ※標準コンポーネントでのみ実装できます。商材固有になる場合は別バッチとして実装することを検討してください。
	 * @param args バッチ処理引数
	 * @return バッチ処理引数
	 * @throws FileAlreadyExistsException
	 */
	public CreateOrderCsvParameter paramCheck(String[] args) throws FileAlreadyExistsException;

	/**
	 * 解約オーダー取得
	 * @return 解約オーダーリスト
	 */
	public List<CancelOrderEntity> getDataList();

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
	 * 解約手配CSV作成処理
	 * @param param バッチ処理引数
	 * @param cancelOrderList 解約オーダーリスト
	 * 
	 * @throws ParseException
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public void process(CreateOrderCsvParameter dto, List<CancelOrderEntity> orderDataList) throws Exception;

	/**
	 * 事後処理
	 * 
	 * @return
	 */
	public void afterProcess(Object param);
}
