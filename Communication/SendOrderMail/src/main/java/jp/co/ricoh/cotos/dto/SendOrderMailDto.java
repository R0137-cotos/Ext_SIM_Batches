package jp.co.ricoh.cotos.dto;

import java.io.File;
import java.util.List;

import lombok.Data;

@Data
public class SendOrderMailDto {

	/**
	 * 添付CSVファイル
	 */
	private String csvFile;

	/**
	 * 商品グループマスタID
	 */
	private long productGrpMasterId;

	/**
	 * メールアドレス
	 */
	private List<String> mailAddressList;
}
