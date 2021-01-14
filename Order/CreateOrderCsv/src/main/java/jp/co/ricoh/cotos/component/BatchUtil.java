package jp.co.ricoh.cotos.component;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.ricoh.cotos.commonlib.entity.contract.ContractInstallationLocation;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractInstallationLocationRepository;

@Component
public class BatchUtil {

	@Autowired
	ObjectMapper om;

	@Autowired
	ContractInstallationLocationRepository contractInstallationLocationRepository;

	/**
	 * 文字列を日付に変換する
	 *
	 * @param yyyyMMdd
	 *            文字列の日付
	 * @return Date型の日付
	 */
	public Date toDate(String yyyyMMdd) {
		try {
			return new SimpleDateFormat("yyyyMMdd").parse(yyyyMMdd);
		} catch (ParseException e1) {
			return null;
		}
	}

	/**
	 * 日付のフォーマットを変更する
	 * 
	 * @param date
	 *         日付
	 * @return Strinｇ型の日付
	 */
	public String changeFormatString(Date date) {
		return new SimpleDateFormat("yyyyMMdd").format(date);
	}

	public int getOrderCsvCreationStatus(String extendsParameter) throws JsonProcessingException, IOException {
		JsonNode node = om.readTree(extendsParameter);
		return node.get("orderCsvCreationStatus").asInt();
	}

	/**
	 * 設置先（契約用）取得
	 * @param contId
	 * @return
	 */
	public ContractInstallationLocation findContractInstallationLocation(long contId) {
		return contractInstallationLocationRepository.findByContractId(contId);
	}

	/**
	 * 郵便番号取得（-(ハイフン)を省く）
	 * @param contId
	 * @return
	 */
	public String getPostNumber(long contId) {
		String postNumber = null;
		ContractInstallationLocation contractInstallationLocation = findContractInstallationLocation(contId);

		if (StringUtils.isNotBlank(contractInstallationLocation.getInputPostNumber())) {
			postNumber = contractInstallationLocation.getInputPostNumber();
		} else {
			postNumber = contractInstallationLocation.getPostNumber();
		}
		postNumber = postNumber.replace("-", "");
		return postNumber;
	}

	/**
	 * 項目値取得
	 * @param postNumber
	 * @return
	 */
	public String getSagawaCodeColumnF(String postNumber) {
		FileInputStream in = null;
		Workbook wb = null;

		// エクセルファイルを読み込む
		try {
			in = new FileInputStream("src/main/resources/file/佐川コード突き当て.xlsx");
			wb = WorkbookFactory.create(in);
		} catch (IOException e) {
			System.out.println(e.toString());
		} catch (InvalidFormatException e) {
			System.out.println(e.toString());
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}

		Sheet sheet = wb.getSheetAt(0);

		// 行が見つからなくなるか、項目値が取得できるまで繰り返す
		for (Row row : sheet) {
			// A列と郵便番号を比較し、F列を取得
			if (postNumber.equals(row.getCell(0).getStringCellValue())) {
				switch (row.getCell(5).getCachedFormulaResultTypeEnum()) {
				case NUMERIC:
					System.out.println(String.valueOf((int) row.getCell(5).getNumericCellValue()));
					return String.valueOf((int) row.getCell(5).getNumericCellValue());
				case STRING:
					System.out.println(row.getCell(5).getStringCellValue());
					return row.getCell(5).getStringCellValue();
				default:
					return null;
				}
			}
		}
		return null;
	}
}
