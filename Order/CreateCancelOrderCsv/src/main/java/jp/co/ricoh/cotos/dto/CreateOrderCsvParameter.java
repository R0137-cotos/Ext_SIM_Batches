package jp.co.ricoh.cotos.dto;

import java.io.File;
import java.time.LocalDate;

import lombok.Data;

/**
 *  解約手配CSV作成 引数クラス
 */
@Data
public class CreateOrderCsvParameter {

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
	private LocalDate operationDate;
}
