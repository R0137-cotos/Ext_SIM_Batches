package jp.co.ricoh.cotos.dto;

import java.io.File;

import lombok.Data;

@Data
public class CreateOrderCsvDto {

	/**
	 * ファイル
	 */
	private File csvFile;

	/**
	 * 仮置きファイル
	 */
	private File tmpFile;

	/**
	 * 処理日
	 */
	private String operationDate;

	/**
	 * 種別
	 * 1:新規 2:容量変更 3:有償交換
	 */
	private String type;
}
