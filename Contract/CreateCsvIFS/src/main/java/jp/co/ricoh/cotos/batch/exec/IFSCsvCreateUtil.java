package jp.co.ricoh.cotos.batch.exec;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.text.Transliterator;

import jp.co.ricoh.cotos.batch.entity.ExtendsParameterIteranceDto;
import jp.co.ricoh.cotos.batch.entity.ThrowableFunction;
import jp.co.ricoh.cotos.commonlib.dto.parameter.common.CommonMasterSearchParameter;
import jp.co.ricoh.cotos.commonlib.dto.result.CommonMasterResult;
import jp.co.ricoh.cotos.commonlib.entity.EnumType;
import jp.co.ricoh.cotos.commonlib.logic.findcommonmaster.FindCommonMaster;

@Component
public class IFSCsvCreateUtil {

	private final static String RICOH_ITEM_CODE_COLUMN_NAME = "ricoh_item_code";

	@Autowired
	FindCommonMaster findCommonMaster;
	
	@Autowired
	ObjectMapper om;

	/**
	 * 汎用コードマスタから品種コードを取得する
	 */
	public CommonMasterResult getRicohItemCodeCommonMasterResult() {
		CommonMasterSearchParameter commonMasterSearchParameter = new CommonMasterSearchParameter();
		commonMasterSearchParameter.setServiceCategory(EnumType.ServiceCategory.見積); // 共通を取得するためには、適当に何か入れる。
		List<CommonMasterResult> commonMasterResultList = findCommonMaster.findCommonMaster(commonMasterSearchParameter);

		return commonMasterResultList.stream().filter(e -> RICOH_ITEM_CODE_COLUMN_NAME.equals(e.getColumnName())).findFirst().get();
	}

	/**
	 * ゼロパディング
	 */
	public String paddingZero(String num) {
		return String.format("%4s", num).replace(" ", "0");
	}

	/**
	 * 半角 → 全角 変換メソッド
	 */
	public String transrateStr(String str) {
		return Optional.ofNullable(str).map(s -> {
			Transliterator transliterator = Transliterator.getInstance("Halfwidth-Fullwidth");
			return transliterator.transliterate(s);
		}).orElse(null);
	}

	/**
	 * 拡張項目文字列をオブジェクトに変換する
	 */
	public List<ExtendsParameterIteranceDto> readJson(String extendsParameterIterance) throws JsonParseException, JsonMappingException, IOException {
		HashMap<String, HashMap<String, Object>> basicContentsJsonMap = (HashMap<String, HashMap<String, Object>>) om.readValue(extendsParameterIterance, new TypeReference<Object>() {
		});

		String extendsJson = om.writeValueAsString(basicContentsJsonMap.get("extendsParameterList"));
		List<ExtendsParameterIteranceDto> extendsParameterList = om.readValue(extendsJson, new TypeReference<List<ExtendsParameterIteranceDto>>() {
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
