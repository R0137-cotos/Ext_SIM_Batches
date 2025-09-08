package jp.co.ricoh.cotos.component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.ricoh.cotos.dto.ExtendsParameterDto;
import jp.co.ricoh.cotos.dto.ThrowableFunction;

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
	public Date changeDate(String yyyyMMdd) {
		try {
			return new SimpleDateFormat("yyyyMMdd").parse(yyyyMMdd);
		} catch (ParseException e1) {
			return null;
		}
	}

	/*
	 * 拡張項目文字列をオブジェクトに変換する
	 */
	public List<ExtendsParameterDto> readJson(String extendsParameterIterance) throws JsonParseException, JsonMappingException, IOException {
		HashMap<String, HashMap<String, Object>> basicContentsJsonMap = (HashMap<String, HashMap<String, Object>>) om.readValue(extendsParameterIterance, new TypeReference<Object>() {
		});

		String extendsJson = om.writeValueAsString(basicContentsJsonMap.get("extendsParameterList"));
		List<ExtendsParameterDto> extendsParameterList = om.readValue(extendsJson, new TypeReference<List<ExtendsParameterDto>>() {
		});

		return extendsParameterList;
	}

	/**
	 * 例外Throw可能な関数型インターフェース
	 */
	public <T, R> Function<T, R> Try(ThrowableFunction<T, R> onTry, BiFunction<Exception, T, R> onCatch) {
		return x -> {
			try {
				return onTry.apply(x);
			} catch (Exception e) {
				e.printStackTrace();
				return onCatch.apply(e, x);
			}
		};
	}

}
