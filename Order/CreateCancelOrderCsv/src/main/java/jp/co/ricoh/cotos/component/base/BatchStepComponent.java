package jp.co.ricoh.cotos.component.base;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import jp.co.ricoh.cotos.BatchConstants;
import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.logic.businessday.BusinessDayUtil;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.component.IBatchStepComponent;
import jp.co.ricoh.cotos.dto.CancelOrderEntity;
import jp.co.ricoh.cotos.dto.CreateOrderCsvParameter;
import jp.co.ricoh.cotos.util.OperationDateException;
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

	/**
	 * パラメーターチェック処理
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @return
	 * @throws FileAlreadyExistsException
	 */
	@Override
	public final CreateOrderCsvParameter paramCheck(String[] args) throws FileAlreadyExistsException {
		CreateOrderCsvParameter param = new CreateOrderCsvParameter();

		// バッチパラメーターのチェックを実施
		if (null == args || args.length != 3) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "ParameterEmptyError", new String[] { BatchConstants.BATCH_PARAMETER_LIST_NAME }));
		}

		// 引数から処理日取得
		String operationDateStr = args[0];
		// 処理日
		LocalDate operationDate = null;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter yyyyMMformatter = DateTimeFormatter.ofPattern("yyyyMM");

		try {
			// 処理日：月末営業日-2営業日か 
			operationDate = LocalDate.parse(operationDateStr, formatter);
			// 処理日付から"yyyyMM"を文字列で取得
			String yyyyMM = operationDate.format(yyyyMMformatter);
			// 処理日当月の最終営業日
			Date lastBusinessDayTmp = businessDayUtil.getLastBusinessDayOfTheMonth(yyyyMM);
			if (lastBusinessDayTmp == null) {
				throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "APIGetFailsError", new String[] { "営業日", "月末最終営業日取得" }));
			}
			LocalDate lastBusinessDay = lastBusinessDayTmp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			int difference = businessDayUtil.calculateDifferenceBetweenBusinessDates(operationDate, lastBusinessDay);
			// 2営業日前でなければ処理を終了する
			if (difference != 2) {
				throw new OperationDateException();
			}
		} catch (DateTimeParseException e) {
			// 引数：処理日の変換に失敗
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "BatchParameterFormatError", new String[] { "yyyyMMdd" }));
		}

		// 引数から作成するファイル名と作成先フォルダパスを取得し、ファイル/フォルダの存在有無をチェック
		File csvFile = Paths.get(args[1], args[2]).toFile();
		// 既にファイルが存在する場合はエラー
		if (csvFile.exists()) {
			throw new FileAlreadyExistsException(csvFile.getAbsolutePath());
		}

		// 作成先フォルダパスが存在しない場合はエラー
		if (!csvFile.getParentFile().exists()) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "DirectoryNotFoundError"));
		}

		// 解約手配CSV作成処理で一時ファイルを利用するので、ファイル/フォルダの存在有無をチェック
		File tmpFile = Paths.get(args[1], "temp.csv").toFile();
		// 既に一時ファイルが存在する場合はエラー
		if (tmpFile.exists()) {
			throw new FileAlreadyExistsException(tmpFile.getAbsolutePath());
		}

		// 一時ファイル作成先フォルダパスが存在しない場合はエラー
		if (!tmpFile.getParentFile().exists()) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "DirectoryNotFoundError"));
		}

		param.setCsvFile(csvFile);
		param.setTmpFile(tmpFile);
		param.setOperationDate(operationDate);
		return param;
	}

	/**
	 * 解約オーダー取得
	 * @return 解約オーダーリスト
	 */
	@Override
	public List<CancelOrderEntity> getCancelOrder() {
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

	/**
	 * 解約手配CSV作成処理
	 * @param param バッチ処理引数
	 * @param cancelOrderList 解約オーダーリスト
	 */
	@Override
	public void process(CreateOrderCsvParameter param, List<CancelOrderEntity> cancelOrderList) throws ParseException, JsonProcessingException, IOException {
		// データ加工等の処理を実施
	}

	@Override
	public void afterProcess(Object param) {
		// 事後処理（ファイル出力、テーブルデータ更新等）を実施
	}
}
