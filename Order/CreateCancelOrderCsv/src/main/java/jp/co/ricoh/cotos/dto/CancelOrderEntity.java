package jp.co.ricoh.cotos.dto;

import java.time.LocalDate;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateConverter;

import lombok.Data;

@Entity
@Data
public class CancelOrderEntity {

	@Id
	private long id;

	/**
	 * 契約ID
	 */
	private long contractIdTemp;

	/**
	 * 契約番号
	 */
	private String contractNumber;

	/**
	 * 商品コード
	 */
	private String ricohItemCode;

	/**
	 * 商品名
	 */
	private String itemContractName;

	/**
	 * 拡張項目繰返
	 */
	private String extendsParameterIterance;

	/**
	 * 解約申込日
	 */
	@Convert(converter = LocalDateConverter.class)
	private LocalDate cancelApplicationDate;

	/**
	 * 解約予定日
	 */
	@Convert(converter = LocalDateConverter.class)
	private LocalDate cancelScheduledDate;

	/**
	 * 申込日
	 */
	@Convert(converter = LocalDateConverter.class)
	private LocalDate applicationDate;

	/**
	 * サービス利用希望日
	 */
	@Convert(converter = LocalDateConverter.class)
	private LocalDate conclusionPreferredDate;

	/**
	 * ライフサイクル状態
	 */
	private String lifecycleStatus;

	/**
	 * 契約種別
	 */
	private String contractType;
}
