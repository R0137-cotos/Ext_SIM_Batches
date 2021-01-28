package jp.co.ricoh.cotos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 佐川コードのDTOです。<br />
 * 確認用のため<br />
 * CommonLibsではなくMasterドメインに定義します。
 */
@Data
@JsonPropertyOrder({ "郵便番号７桁", "都道府県名", "市区町村名", "町域名", "JIS５桁", "HP記載", "最終便", })
public class SagawaCodeDto {

	/**
	 * 郵便番号７桁
	 */
	@JsonProperty("郵便番号７桁")
	@ApiModelProperty(value = "郵便番号７桁", required = true, position = 1, example = "郵便番号７桁")
	private String postNumber;

	/**
	 * 都道府県名
	 */
	@JsonProperty("都道府県名")
	@ApiModelProperty(value = "都道府県名", required = true, position = 2, example = "都道府県名")
	private String prefecturesName;

	/**
	 * 市区町村名
	 */
	@JsonProperty("市区町村名")
	@ApiModelProperty(value = "市区町村名", required = true, position = 3, example = "市区町村名")
	private String cityName;

	/**
	 * 町域名
	 */
	@JsonProperty("町域名")
	@ApiModelProperty(value = "町域名", required = true, position = 4, example = "町域名")
	private String townAreaName;

	/**
	 * JIS５桁
	 */
	@JsonProperty("JIS５桁")
	@ApiModelProperty(value = "JIS５桁", required = true, position = 5, example = "JIS５桁")
	private String jis;

	/**
	 * HP記載
	 */
	@JsonProperty("HP記載")
	@ApiModelProperty(value = "HP記載", required = true, position = 6, example = "HP記載")
	private String hp;

	/**
	 * 最終便
	 */
	@JsonProperty("最終便")
	@ApiModelProperty(value = "最終便", required = true, position = 7, example = "最終便")
	private String lastFlight;
}
