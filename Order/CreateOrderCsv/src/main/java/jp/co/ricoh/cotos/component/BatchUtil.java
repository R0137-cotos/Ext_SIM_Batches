package jp.co.ricoh.cotos.component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BatchUtil {

	@Autowired
	ObjectMapper om;

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
}
