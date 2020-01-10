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
	 * 処理日
	 */
	private String operationDate;
}
