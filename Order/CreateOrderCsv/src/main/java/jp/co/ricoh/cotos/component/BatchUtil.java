package jp.co.ricoh.cotos.component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import jp.co.ricoh.cotos.commonlib.dto.result.SagawaCodeDto;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractInstallationLocation;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractInstallationLocationRepository;

@Component
public class BatchUtil {

	@Autowired
	ObjectMapper om;

	@Autowired
	ContractInstallationLocationRepository contractInstallationLocationRepository;

	@Autowired
	CheckUtil checkUtil;

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
	 * 郵便番号取得
	 * @param contId
	 * @return
	 */
	public List<String> getPostNumber(long contId) {
		List<String> postNumberList = new ArrayList<String>();
		ContractInstallationLocation contractInstallationLocation = findContractInstallationLocation(contId);

		if (null != contractInstallationLocation) {
			postNumberList.add(deleteHyphen(contractInstallationLocation.getInputPostNumber()));
			postNumberList.add(deleteHyphen(contractInstallationLocation.getPostNumber()));
		}
		return postNumberList;
	}

	/**
	 * -(ハイフン)を省く
	 * @param str
	 * @return
	 */
	private String deleteHyphen(String str) {
		if (StringUtils.isNotBlank(str)) {
			return str.replace("-", "");
		}
		return str;
	}

	/**
	 * 項目値取得
	 * @param postNumber
	 * @return
	 */
	public String getSagawaCodeColumnF(List<String> postNumberList) {
		// 佐川コード突き当てCSV読込
		try {
			InputStream in = this.getClass().getResourceAsStream("/file/佐川コード突き当て.csv");
			CsvMapper mapper = new CsvMapper();
			CsvSchema schema = CsvSchema.emptySchema().withHeader();
			CsvSchema quoteSchema = mapper.schemaFor(SagawaCodeDto.class).withoutQuoteChar();
			MappingIterator<SagawaCodeDto> it = mapper.readerFor(SagawaCodeDto.class).with(schema).with(quoteSchema).readValues(new InputStreamReader(in, "UTF-8"));
			List<SagawaCodeDto> csvlist = it.readAll();
			// CSVから取得した項目値と 設置先情報#郵便番号(手入力)、設置先情報#郵便番号の順で比較
			for (String postNumber : postNumberList) {
				List<SagawaCodeDto> SagawaCodeDtoList = csvlist.stream().filter(SagawaCodeDto -> SagawaCodeDto.getPostNumber().equals(postNumber)).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(SagawaCodeDtoList) && StringUtils.isNotBlank(SagawaCodeDtoList.get(0).getHp())) {
					return SagawaCodeDtoList.get(0).getHp();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "FileReadError"), e);
		}
		return null;
	}
}
