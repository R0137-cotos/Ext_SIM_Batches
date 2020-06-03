package jp.co.ricoh.cotos.dto;

import lombok.Data;

@Data

public class SIMExtendsParameterIteranceDto {
	private long id;
	private String contractType;
	private String productCode;
	private String productName;
	private String lineNumber;
	private String serialNumber;
	private String device;
	private String invoiceNumber;
	private String cancelDate;
}
