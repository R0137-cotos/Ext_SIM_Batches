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
	 */
	private String type;
}
