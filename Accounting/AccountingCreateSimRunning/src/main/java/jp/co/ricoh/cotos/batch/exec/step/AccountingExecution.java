package jp.co.ricoh.cotos.batch.exec.step;

import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.batch.util.AccountingCreateSimRunningUtil;
import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.entity.accounting.Accounting;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractDetail;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractDetail.RunningAccountSalesStatus;
import jp.co.ricoh.cotos.commonlib.entity.contract.ItemContract;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.commonlib.repository.accounting.AccountingRepository;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractDetailRepository;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractRepository;
import jp.co.ricoh.cotos.commonlib.repository.contract.ItemContractRepository;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

/**
 *
 * ジョブステップ２～４：「計上処理→対象契約データ登録→売上計算結果WORKの処理済計上データフラグ更新」
 *
 * ジョブステップ２～４の途中でエラーが発生した場合、 トランザクションをロールバックして処理を終了する。
 *
 */
@NoArgsConstructor
@Component
@Log4j
public class AccountingExecution {

	@Autowired
	CheckUtil checkUtil;

	@Autowired
	DBUtil dbUtil;

	@Autowired
	AccountingCreateSimRunningUtil appUtil;

	@Autowired
	AccountingRepository accountingRepository;

	@Autowired
	ContractRepository contractRepository;

	@Autowired
	ContractDetailRepository contractDetailRepository;

	@Autowired
	ItemContractRepository itemContractRepository;

	/** 課金計上テーブルのEntity */
	@Setter
	private Accounting accounting;

	/**
	 * 課金計上テーブルのEntityを取得
	 *
	 * @return Accounting 課金計上テーブルのEntity
	 */
	public Accounting getAccounting() {
		return accounting;
	}

	public JobStepResult execute() {

		log.info(String.format("RJ管理番号%sの計上データ作成を開始します。", accounting.getRjManageNumber()));
		JobStepResult result = JobStepResult.FAILURE;
		try {
			// 課金計上テーブルにデータ登録
			saveAccounting();
			result = JobStepResult.SUCCESS;
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

	/**
	 * 保存時に採番されたIDを元に項目作成
	 *
	 * @param savedAccounting
	 */
	@Transactional
	private void saveAccounting() {
		accountingRepository.save(this.accounting);

		// 27 NSPユニークキー
		// 契約.契約番号(頭3桁除去)＋品種（契約用）.リコー品種コード＋計上ID
		this.accounting.setFfmNspKey(this.accounting.getFfmContractDocNo().substring(3) + this.accounting.getProductTypeCd() + this.accounting.getId());
		accountingRepository.save(this.accounting);
	}

	/**
	 * 実行結果に応じて契約明細を更新
	 *
	 * @param result
	 */
	@Transactional
	public void updateContractDetail(JobStepResult result) {
		ContractDetail contractDetail = contractDetailRepository.findOne(this.accounting.getContractDetailId());
		if (result == JobStepResult.SUCCESS) {
			contractDetail.setRunningAccountSalesStatus(RunningAccountSalesStatus.正常);
			contractDetail.setRunningAccountSalesDate(new Date());
		} else {
			contractDetail.setRunningAccountSalesStatus(RunningAccountSalesStatus.処理エラー);
		}

		Contract contract = new Contract();
		contract.setId(contractDetail.getContract().getId());
		contractDetail.setContract(contract);
		ItemContract itemContract = new ItemContract();
		itemContract.setId(contractDetail.getItemContract().getId());
		ContractDetail contractDetailtemp = new ContractDetail();
		contractDetailtemp.setId(contractDetail.getId());
		itemContract.setContractDetail(contractDetailtemp);
		contractDetail.setItemContract(itemContract);
		contractDetailRepository.save(contractDetail);
	}
}
