package jp.co.ricoh.cotos.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({ "契約No", "商品コード", "商品名", "オーダー日", "納入希望日", "氏名(漢字)", "氏名(カナ)", "郵便番号", "住所", "組織名", "部署名", "電話番号", "FAX番号", "担当者メールアドレス", "回線番号", "シリアル番号", "納入予定日", "送り状番号", "備考" })
public class FindCreateOrderCsvDataDto {

	/**
	 * 契約ID
	 */
	@JsonIgnore
	private long contractIdTemp;
	
	/**
	 * 契約明細ID
	 */
	@JsonIgnore
	private long contractDetailId;

	/**
	 * 契約No
	 */
	@JsonProperty("契約No")
	private String contractId;

	/**
	 * 商品コード
	 */
	@JsonProperty("商品コード")
	private String ricohItemCode;

	/**
	 * 商品名
	 */
	@JsonProperty("商品名")
	private String itemContractName;

	/**
	 * オーダー日
	 */
	@JsonProperty("オーダー日")
	private Date orderDate;

	/**
	 * 納入希望日
	 */
	@JsonProperty("納入希望日")
	private Date conclusionPreferredDate;

	/**
	 * 氏名(漢字)
	 */
	@JsonProperty("氏名(漢字)")
	private String picName;

	/**
	 * 氏名(カナ)
	 */
	@JsonProperty("氏名(カナ)")
	private String picNameKana;

	/**
	 * 郵便番号
	 */
	@JsonProperty("郵便番号")
	private String postNumber;

	/**
	 * 住所
	 */
	@JsonProperty("住所")
	private String address;

	/**
	 * 組織名
	 */
	@JsonProperty("組織名")
	private String companyName;

	/**
	 * 部署名
	 */
	@JsonProperty("部署名")
	private String officeName;

	/**
	 * 電話番号
	 */
	@JsonProperty("電話番号")
	private String picPhoneNumber;

	/**
	 * FAX番号
	 */
	@JsonProperty("FAX番号")
	private String picFaxNumber;

	/**
	 * 担当者メールアドレス
	 */
	@JsonProperty("担当者メールアドレス")
	private String picMailAddress;

	/**
	 * 回線番号
	 */
	@JsonProperty("回線番号")
	private String lineNumber;

	/**
	 * シリアル番号
	 */
	@JsonProperty("シリアル番号")
	private String serialNumber;

	/**
	 * 納入予定日
	 */
	@JsonProperty("納入予定日")
	private String deliveryExpectedDate;

	/**
	 * 送り状番号
	 */
	@JsonProperty("送り状番号")
	private String invoiceNumber;

	/**
	 * 備考
	 */
	@JsonProperty("備考")
	private String remarks;
}
