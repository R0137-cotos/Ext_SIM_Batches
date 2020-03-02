package jp.co.ricoh.cotos.batch.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import lombok.Data;

/**
 * 契約No生成用のオブジェクト
 *
 */
@Data
@Entity
public class GeneratedNumber {

	/**
	 * numberだとOracleの予約後と重複するのでgeneratedNumberに。
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "csv_ifs_contract_sim_seq")
	@SequenceGenerator(name = "csv_ifs_contract_sim_seq", sequenceName = "csv_ifs_contract_sim_seq", allocationSize = 1)
	private long generatedNumber;

}
