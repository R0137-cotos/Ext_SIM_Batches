package jp.co.ricoh.cotos.batch.entity;

import lombok.Data;

@Data
public class ExtendsParameterIteranceDto {

	private String id;

	private String contractType;

	private String productCode;

	private String productName;

	private String lineNumber;

	private String serialNumber;

	private String device;

	private String invoiceNumber;

	//音声SIMの拡張項目
	/**
	 * IMEI (携帯シリアル番号)
	 */
	private String imeiNumber;
}
