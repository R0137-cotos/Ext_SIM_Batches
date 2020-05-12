package jp.co.ricoh.cotos.component.base;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import jp.co.ricoh.cotos.BatchConstants;
import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.component.IBatchStepComponent;
import jp.co.ricoh.cotos.dto.CreateOrderCsvDataDto;
import jp.co.ricoh.cotos.dto.CreateOrderCsvDto;
import lombok.extern.log4j.Log4j;

@Component("BASE")
@Log4j
public class BatchStepComponent implements IBatchStepComponent {

	@Autowired
	CheckUtil checkUtil;

	@Autowired
	DBUtil dbUtil;

	/**
	 * パラメーターチェック処理
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @return
	 * @throws FileAlreadyExistsException
	 */
	@Override
	public CreateOrderCsvDto paramCheck(String[] args) throws FileAlreadyExistsException {
		CreateOrderCsvDto dto = new CreateOrderCsvDto();

		// バッチパラメーターのチェックを実施
		if (null == args || args.length != 4) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "ParameterEmptyError", new String[] { BatchConstants.BATCH_PARAMETER_LIST_NAME }));
		}

		String operationDate = args[0];
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			sdf.setLenient(false);
			sdf.parse(operationDate);
		} catch (ParseException pe) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "BatchParameterFormatError", new String[] { "yyyyMMdd" }));
		}

		File csvFile = Paths.get(args[1], args[2]).toFile();
		if (csvFile.exists()) {
			throw new FileAlreadyExistsException(csvFile.getAbsolutePath());
		}

		if (!csvFile.getParentFile().exists()) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "DirectoryNotFoundError"));
		}

		File tmpFile = Paths.get(args[1], "temp.csv").toFile();
		if (tmpFile.exists()) {
			throw new FileAlreadyExistsException(tmpFile.getAbsolutePath());
		}

		if (!tmpFile.getParentFile().exists()) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "DirectoryNotFoundError"));
		}

		String type = args[3];
		if (!("1".equals(type) || "2".equals(type) || "3".equals(type))) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "CannotIdentify", new String[] { "種別" }));
		}

		dto.setCsvFile(csvFile);
		dto.setTmpFile(tmpFile);
		dto.setOperationDate(operationDate);
		dto.setType(type);
		return dto;
	}

	/**
	 * 処理データ取得
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @param searchParam
	 *            処理データ取得用パラメーター
	 * @return 処理データリスト
	 */
	@Override
	public List<CreateOrderCsvDataDto> getDataList(String contractType) {
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<>();
		Map<String, Object> sqlParams = new HashMap<String, Object>();
		sqlParams.put("contractType", contractType);
		orderDataList = dbUtil.loadFromSQLFile("sql/findOrderData.sql", CreateOrderCsvDataDto.class, sqlParams);
		return orderDataList;
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
	public void process(CreateOrderCsvDto dto, List<CreateOrderCsvDataDto> orderDataList) throws ParseException, JsonProcessingException, IOException {
		// データ加工等の処理を実施
	}

	@Override
	public void afterProcess(Object param) {
		// 事後処理（ファイル出力、テーブルデータ更新等）を実施
	}
}
