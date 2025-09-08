package jp.co.ricoh.cotos.batch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

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
