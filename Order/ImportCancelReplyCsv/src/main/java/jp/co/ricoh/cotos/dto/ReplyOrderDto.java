package jp.co.ricoh.cotos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({ "契約No", "商品コード", "商品名", "オーダー日", "解約月", "回線番号", "ICCID" })
public class ReplyOrderDto {

	/**契約no.*/
	@JsonProperty("契約No")
	private String contractId;

	/**商品コード*/
	@JsonProperty("商品コード")
	private String ricohItemCode;

	/**商品名*/
	@JsonProperty("商品名")
	private String itemContractName;

	/**オーダー日*/
	@JsonProperty("オーダー日")
	private String orderDate;

	/**解約月*/
	@JsonProperty("解約月")
	private String cancelMonth;

	/**回線番号*/
	@JsonProperty("回線番号")
	private String lineNumber;

	/**ICCID*/
	@JsonProperty("ICCID")
	private String serialNumber;
}
