package jp.co.ricoh.cotos.batch.exec;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ibm.icu.text.Transliterator;

import jp.co.ricoh.cotos.commonlib.dto.parameter.common.CommonMasterSearchParameter;
import jp.co.ricoh.cotos.commonlib.dto.result.CommonMasterResult;
import jp.co.ricoh.cotos.commonlib.entity.EnumType;
import jp.co.ricoh.cotos.commonlib.logic.findcommonmaster.FindCommonMaster;

@Component
public class IFSCsvCreateUtil {

	private final static String RICOH_ITEM_CODE_COLUMN_NAME = "ricoh_item_code";

	@Autowired
	FindCommonMaster findCommonMaster;

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
		return String.format("%9s", num).replace(" ", "0");
	}

	public String paddingZero(long num) {
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

}
