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
import jp.co.ricoh.cotos.commonlib.logic.businessday.BusinessDayUtil;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractRepository;
import jp.co.ricoh.cotos.commonlib.repository.master.ProductGrpMasterRepository;
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

	@Autowired
	BusinessDayUtil businessDayUtil;

	@Autowired
	ContractRepository contractRepository;

	@Autowired
	ProductGrpMasterRepository productGrpMasterRepository;

	/**
	 * パラメーターチェック処理 ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * 
	 * @return
	 * @throws FileAlreadyExistsException
	 */
	@Override
	public final CreateOrderCsvDto paramCheck(String[] args) throws FileAlreadyExistsException {
		CreateOrderCsvDto dto = new CreateOrderCsvDto();

		// バッチパラメーターのチェックを実施
		if (null == args || args.length != 3) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "ParameterEmptyError", new String[] { BatchConstants.BATCH_PARAMETER_LIST_NAME }));
		}
		String[] paramList = BatchConstants.BATCH_PARAMETER_LIST_NAME.split("/");

		String operationDate = args[0];
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			sdf.setLenient(false);
			sdf.parse(operationDate);
		} catch (ParseException pe) {
			log.fatal("処理年月日が不正です。");
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "ParameterEmptyError", new String[] { paramList[0] }));
		}

		File csvFile = Paths.get(args[1], args[2] + "_" + args[0] + ".csv").toFile();
		if (csvFile.exists()) {
			throw new FileAlreadyExistsException(csvFile.getAbsolutePath());
		}

		if (!csvFile.getParentFile().exists()) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "DirectoryNotFoundError"));
		}

		dto.setCsvFile(csvFile);
		dto.setOperationDate(operationDate);
		return dto;
	}

	/**
	 * 処理データ取得 ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * 
	 * @param searchParam
	 *            処理データ取得用パラメーター
	 * @return 処理データリスト
	 * @throws ParseException
	 */
	@Override
	public final List<CreateOrderCsvDataDto> getDataList() {

		Map<String, Object> sqlParams = new HashMap<>();
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<>();

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
