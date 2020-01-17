package jp.co.ricoh.cotos.batch.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 *
 * 計上データ作成（SIMランニング分）のユーティリティクラス
 *
 */
@Component
public class AccountingCreateSimRunningUtil {

	/**
	 * 日付文字列（yyyyMMdd）をDate型に変換
	 * @param yyyyMMdd 日付文字列（yyyyMMdd）
	 * @return Date 変換結果
	 * @throws ParseException
	 */
	private Date toDate(String yyyyMMdd) throws ParseException {
		if (null == yyyyMMdd || "".equals(yyyyMMdd))
			return null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		sdf.setLenient(false);
		return sdf.parse(yyyyMMdd);
	}

	/**
	 * 実在する日付かどうかをチェックする
	 * @param yyyyMMdd 日付文字列（yyyyMMdd）
	 * @return boolean チェック結果（true：日付が存在する　false：日付が存在しない）
	 */
	public boolean existsDate(String yyyyMMdd) {
		try {
			toDate(yyyyMMdd);
		} catch (ParseException e) {
			return false;
		}
		return true;
	}

	/**
	 * 加算（BigDecimal）
	 * @param targets 演算対象
	 * @return BigDecimal 加算結果
	 */
	public BigDecimal add(BigDecimal... targets) {
		if (null == targets || 0 == targets.length)
			return new BigDecimal(0);
		return Arrays.asList(targets).stream().filter(val -> null != val).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/**
	 * 乗算（BigDecimal）
	 * @param target1 演算対象１
	 * @param target2 演算対象２
	 * @return BigDecimal 乗算結果
	 */
	public BigDecimal multiply(BigDecimal target1, BigDecimal target2) {
		if (null == target1)
			return new BigDecimal(0);
		if (null == target2)
			return new BigDecimal(0);

		return target1.multiply(target2).setScale(0, RoundingMode.FLOOR);
	}

	/**
	 * 乗算（BigDecimalとInteger）
	 * @param target1 演算対象１
	 * @param target2 演算対象２
	 * @return BigDecimal 乗算結果
	 */
	public BigDecimal multiply(BigDecimal target1Dec, Integer target2Int) {
		if (null == target1Dec)
			return new BigDecimal(0);
		if (null == target2Int)
			return new BigDecimal(0);

		return multiply(target1Dec, new BigDecimal(target2Int));
	}

	/**
	 * 消費税率区分のチェック処理
	 *
	 * @param cunsumptionTaxRateClass 消費税率区分
	 * @return boolean チェック結果（true：正常　false：異常）
	 */
	public boolean checkCunsumptionTaxRateClass(String cunsumptionTaxRateClass) {
		if (!StringUtils.isNotEmpty(cunsumptionTaxRateClass)) {
			return false;
		}
		if (cunsumptionTaxRateClass.contains(Pattern.quote("."))) {
			return false;
		}
		try {
			Integer.parseInt(cunsumptionTaxRateClass);
		} catch (NumberFormatException e) {
			return false;
		}

		return true;
	}

	/**
	 * 消費税額合算
	 * @param ammount 金額
	 * @param cunsumptionTaxRateClass 消費税率区分（8 または 10）
	 * @return BigDecimal 指定した金額と消費税額の合算
	 */
	public BigDecimal calcConsumptionTaxIncluded(BigDecimal ammount, String cunsumptionTaxRateClass) {
		if (!checkCunsumptionTaxRateClass(cunsumptionTaxRateClass)) {
			throw new IllegalArgumentException("cunsumptionTaxRate is invalid");
		}
		if (null == ammount) {
			return null;
		}

		BigDecimal calcConsumptionTax = calcConsumptionTax(ammount, cunsumptionTaxRateClass);
		return ammount.add(calcConsumptionTax).setScale(0, RoundingMode.FLOOR);
	}

	/**
	 * 消費税額計算
	 * @param ammount 金額
	 * @param cunsumptionTaxRateClass 消費税率区分（8 または 10）
	 * @return BigDecimal 指定した金額の消費税額
	 */
	public BigDecimal calcConsumptionTax(BigDecimal ammount, String cunsumptionTaxRateClass) {
		if (null == ammount) {
			return null;
		}
		BigDecimal cunsumptionTaxRate = new BigDecimal(cunsumptionTaxRateClass);
		cunsumptionTaxRate = cunsumptionTaxRate.divide(new BigDecimal(100), 2, RoundingMode.DOWN);
		return ammount.multiply(cunsumptionTaxRate).setScale(0, RoundingMode.FLOOR);
	}
}
