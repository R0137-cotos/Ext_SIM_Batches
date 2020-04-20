package jp.co.ricoh.cotos.dto;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Entity
@Data
public class SearchMailTargetDto {

	@Id
	@ApiModelProperty(value = "連番", required = true, position = 1)
	private long seqNo;

	/**
	 * 商品グループマスタID
	 */
	@ApiModelProperty(value = "商品グループマスタID", required = true, position = 2)
	private Long productGrpMasterId;

	/**
	 * メールアドレス
	 */
	@ApiModelProperty(value = "メールアドレス", required = true, position = 3)
	private String mailAddress;
}
