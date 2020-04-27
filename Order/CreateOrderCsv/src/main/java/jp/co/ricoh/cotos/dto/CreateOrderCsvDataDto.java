package jp.co.ricoh.cotos.dto;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class CreateOrderCsvDataDto {

	@Id
	private long id;

	/**
	 * 契約ID
	 */
	private long contractIdTemp;

	/**
	 * 契約No
	 */
	private String contractNumber;

	/**
	 * 数量
	 */
	private String quantity;

	/**
	 * 商品コード
	 */
	private String ricohItemCode;

	/**
	 * 商品名
	 */
	private String itemContractName;

	/**
	 * 納入希望日
	 */
	private Date conclusionPreferredDate;

	/**
	 * 最短納入日数
	 */
	private int shortestDeliveryDate;

	/**
	 * 氏名(漢字)
	 */
	private String picName;

	/**
	 * 氏名(カナ)
	 */
	private String picNameKana;

	/**
	 * 郵便番号
	 */
	private String postNumber;

	/**
	 * 住所
	 */
	private String address;

	/**
	 * 組織名
	 */
	private String companyName;

	/**
	 * 部署名
	 */
	private String officeName;

	/**
	 * 電話番号
	 */
	private String picPhoneNumber;

	/**
	 * FAX番号
	 */
	private String picFaxNumber;

	/**
	 * 担当者メールアドレス
	 */
	private String picMailAddress;

	/**
	 * 拡張項目
	 */
	private String extendsParameter;

	/**
	 * 契約明細ID
	 */
	private Long contractDetailId;

	/**
	 * 更新日時
	 */
	private Date updatedAt;
}
