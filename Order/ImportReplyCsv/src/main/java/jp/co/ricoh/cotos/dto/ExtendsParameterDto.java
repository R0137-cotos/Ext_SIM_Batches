package jp.co.ricoh.cotos.dto;

import lombok.Data;

@Data
public class ExtendsParameterDto {

	private long id;

	/**
	 * 種別
	 */
	private String contractType;

	/**
	 * 商品コード
	 */
	private String productCode;

	/**
	 * 商品名
	 */
	private String productName;

	/**
	 * 回線番号
	 */
	private String lineNumber;

	/**
	 * シリアル番号
	 */
	private String serialNumber;

	/**
	 * 使用デバイス
	 */
	private String device;

	/**
	 * 送り状番号
	 */
	private String InvoiceNumber;

}
