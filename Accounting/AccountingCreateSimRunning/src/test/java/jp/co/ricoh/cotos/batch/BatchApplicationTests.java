package jp.co.ricoh.cotos.batch;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.ibm.icu.text.Transliterator;

import jp.co.ricoh.cotos.BatchApplication;
import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.entity.EnumType.InitialRunningDiv;
import jp.co.ricoh.cotos.commonlib.entity.accounting.Accounting;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract.LifecycleStatus;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractDetail;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractDetail.RunningAccountSalesStatus;
import jp.co.ricoh.cotos.commonlib.entity.contract.CustomerContract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ItemContract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ItemDetailContract;
import jp.co.ricoh.cotos.commonlib.entity.master.MvWjmoc020OrgAllInfoCom;
import jp.co.ricoh.cotos.commonlib.repository.accounting.AccountingRepository;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractDetailRepository;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractRepository;
import jp.co.ricoh.cotos.commonlib.repository.contract.ItemContractRepository;
import jp.co.ricoh.cotos.commonlib.repository.master.MvEmployeeMasterRepository;
import jp.co.ricoh.cotos.commonlib.repository.master.MvTJmci101MasterRepository;
import jp.co.ricoh.cotos.commonlib.repository.master.MvTJmci108MasterRepository;
import jp.co.ricoh.cotos.commonlib.repository.master.MvWjmoc020OrgAllInfoComRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchApplicationTests extends TestBase {

	@Autowired
	DBUtil dbUtil;

	static ConfigurableApplicationContext context;

	@Autowired
	AccountingRepository accountingRepository;

	@Autowired
	ContractRepository contractRepository;

	@Autowired
	ContractDetailRepository contractDetailRepository;

	@Autowired
	ItemContractRepository itemContractRepository;

	@Autowired
	MvWjmoc020OrgAllInfoComRepository mvWjmoc020OrgAllInfoComRepository;

	@Autowired
	MvEmployeeMasterRepository mvEmployeeMasterRepository;

	@Autowired
	MvTJmci101MasterRepository mvTJmci101MasterRepository;

	@Autowired
	MvTJmci108MasterRepository mvTJmci108MasterRepository;

	@Autowired
	public void injectContext(ConfigurableApplicationContext injectContext) {
		context = injectContext;
		context.getBean(DBConfig.class).clearData();
		context.getBean(DBConfig.class).initTargetTestData("sql/CreateAccountingBaseData.sql");
	}

	@AfterClass
	public static void stopAPServer() throws InterruptedException {
		if (null != context) {
			context.getBean(DBConfig.class).clearData();
			context.stop();
		}
	}

	private void バッチ起動(String baseDate) {
		BatchApplication.main(new String[] { baseDate });
	}

	@Test
	@Transactional
	public void 正常系_締結中() throws ParseException {
		// 検証
		final String baseDate = "20200201";
		try {
			バッチ起動(baseDate);
		} catch (ExitException e) {
			Assert.fail("異常終了しました。試験失敗です。");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了しました。試験失敗です。");
		}
		値検証(baseDate);
	}

	@Test
	@Transactional
	public void 正常系_解約手続き中() throws ParseException {
		// データ更新
		context.getBean(DBConfig.class).initTargetTestData("sql/解約手続き中更新.sql");
		// 検証
		final String baseDate = "20200201";
		try {
			バッチ起動(baseDate);
		} catch (ExitException e) {
			Assert.fail("異常終了しました。試験失敗です。");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了しました。試験失敗です。");
		}
		値検証(baseDate);
	}

	@Test
	@Transactional
	public void 正常系_解約予定日待ち() throws ParseException {
		// データ更新
		context.getBean(DBConfig.class).initTargetTestData("sql/解約予定日待ち更新.sql");
		// 検証
		final String baseDate = "20200201";
		try {
			バッチ起動(baseDate);
		} catch (ExitException e) {
			Assert.fail("異常終了しました。試験失敗です。");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了しました。試験失敗です。");
		}
		値検証(baseDate);
	}

	// 以下のようなデータを作成し、処理対象としている
	// 新規　　　　契約ID:100　ライフサイクル:旧契約　サービス開始日:2020/10/1　課金開始日:2020/11/1　サービス利用希望日:2020/10/1
	// 情報変更　契約ID:200　ライフサイクル:旧契約　サービス開始日:2020/10/1　課金開始日:2020/11/1　サービス利用希望日:2020/10/1
	// 契約変更　契約ID:300　ライフサイクル:締結中　サービス開始日:2020/11/4　課金開始日:2020/12/1　サービス利用希望日:2020/10/1
	@Test
	@Transactional
	public void 正常系_課金開始日考慮() throws ParseException {
		// データ更新
		// 検証
		final String baseDate = "20201105";
		try {
			バッチ起動(baseDate);
		} catch (ExitException e) {
			Assert.fail("異常終了しました。試験失敗です。");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了しました。試験失敗です。");
		}

		値検証(baseDate);
		旧契約が計上処理対象となっていることを確認();
	}

	/**
	 * NOTE: 納品書・請求書印字用コメントがNULLでないケースは「正常系_締結中」でカバー
	 *       納品書・請求書印字用コメント、企業名がともにNULLのケースは「正常系_課金開始日考慮」でカバー
	 */
	@Test
	@Transactional
	public void 正常系_納品書_請求書印字用コメントがnull() throws ParseException {
		// データ更新
		context.getBean(DBConfig.class).initTargetTestData("sql/納品書・請求書印字用コメントがnull.sql");
		// 検証
		final String baseDate = "20200201";
		try {
			バッチ起動(baseDate);
		} catch (ExitException e) {
			Assert.fail("異常終了しました。試験失敗です。");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了しました。試験失敗です。");
		}
		値検証(baseDate);
		}

	@Test
	public void パラメータ不正() {
		// 検証
		try {
			String baseDate = "2019/01/01";
			バッチ起動(baseDate);
			Assert.fail("パラメータ不正：異常終了しませんでした。");
		} catch (ExitException e) {
		}
	}

	private void データ作成区分_20_売上請求チェック(Accounting accounting, String baseDate) {
		Contract contract = contractRepository.findOne(accounting.getContractId());
		ContractDetail contractDetail = contract.getContractDetailList().stream()
				.filter(d -> d.getId() == accounting.getContractDetailId()).findFirst().get();
		ItemContract itemContract = contractDetail.getItemContract();
		CustomerContract customerContract = contract.getCustomerContract();

		// 22 仕入データパターン
		Assert.assertTrue("作成データパターンが20(固定値)と同じであること", StringUtils.equals(accounting.getFfmDataPtn(), "20"));

		// 50 契約金額
		Assert.assertTrue("契約金額が契約明細.金額と同じであること",
				accounting.getFfmContractPrice().compareTo(contractDetail.getAmountSummary()) == 0);

		// 77 売上社員設定区分
		Assert.assertNull("売上社員設定区分がNullであること", accounting.getFfmSalesEmpType());

		// 78 売上社員コード
		Assert.assertNull("売上社員コードがNullであること", accounting.getFfmSalesEmpCd());

		// 84 代直区分（販売店データリンク・売上用）
		// ※SIMは商流区分=1のみ
		switch (contract.getCommercialFlowDiv()) {
		case "1":
			Assert.assertTrue("代直区分（販売店データリンク・売上用）が商流区分が1の場合2(直売)と同じであること",
					StringUtils.equals(accounting.getFfmDistType(), "2"));
			break;
		default:
			Assert.fail("代直区分（販売店データリンク・売上用）が不正です");
			break;
		}

		// 94 RJ売上単価
		// ※SIMは商流区分=1のみ
		switch (contract.getCommercialFlowDiv()) {
		case "1":
			Assert.assertTrue("商流区分が1の場合、RJ売上単価が契約明細.単価と同じであること",
					accounting.getFfmRjSalesPrice().compareTo(contractDetail.getUnitPrice()) == 0);
			break;
		default:
			Assert.fail("RJ売上単価が不正です");
			break;
		}

		// 96 RJ売上金額
		// ※SIMは商流区分=1のみ
		switch (contract.getCommercialFlowDiv()) {
		case "1":
			Assert.assertTrue("商流区分が1の場合、RJ売上金額が契約明細.金額と同じであること",
					accounting.getFfmRjSalesAmt().compareTo(contractDetail.getAmountSummary()) == 0);
			break;
		default:
			Assert.fail("RJ売上金額が不正です");
			break;
		}

		// 98 RJ売上消費税区分
		Assert.assertNull("RJ売上消費税区分がNullであること", accounting.getFfmRjSalesTaxType());

		// 99 RJ売上消費税率区分
		Assert.assertNull("RJ売上消費税率区分がNullであること", accounting.getFfmRjSalesTaxRate());

		// 100 RJ売上消費税額
		Assert.assertNull("RJ売上消費税額がNullであること", accounting.getFfmRjSalesTaxPrice());

		// 117 R原価数量
		Assert.assertTrue("R原価数量が契約明細.数量と同じであること", accounting.getFfmRCostCnt() == contractDetail.getQuantity());

		// 118 R原価単価
		if (accounting.getFfmRCostPrice() != null) {
			Assert.assertTrue("R原価単価が品種（契約用）.R原価と同じであること",
					accounting.getFfmRCostPrice().equals(itemContract.getRCost()));
		} else {
			Assert.assertTrue("R原価単価が品種（契約用）.R原価と同じであること", itemContract.getRCost() == null);
		}

		// 120 R原価金額
		if (accounting.getFfmRCostPrice() != null) {
			Assert.assertTrue("R原価金額が品種（契約用）.R原価＊契約明細.数量と同じであること", accounting.getFfmRCostAmt().compareTo(
					accounting.getFfmRCostPrice().multiply(new BigDecimal(contractDetail.getQuantity()))) == 0);
		}

		// 122 R原価消費税区分
		Assert.assertNull("R原価消費税区分がNullであること", accounting.getFfmRCostTaxType());

		// 123 R原価消費税率区分
		Assert.assertNull("R原価消費税率区分がNullであること", accounting.getFfmRCostTaxRate());

		// 124 R原価消費税額
		Assert.assertNull("R原価消費税額がNullであること", accounting.getFfmRCostTaxPrice());

		// 134 納品書要否区分
		// ※SIMは商流区分=1のみ
		if (StringUtils.equals(contract.getCommercialFlowDiv(), "1")) {
			Assert.assertNull("134 納品書要否区分がnullであること", accounting.getFfmBillOutputFlg());
		}

		// 135 納品書出力パターン
		Assert.assertTrue("納品書出力パターンが2(固定)であること", StringUtils.equals(accounting.getFfmBillOutputPtn(), "2"));

		// 136 納品書出力形式
		Assert.assertTrue("納品書出力形式が10(固定)であること", StringUtils.equals(accounting.getFfmBillOutputFmt(), "10"));

		// 137 請求書発行システム
		Assert.assertTrue("請求書発行システムが1(固定)であること", StringUtils.equals(accounting.getFfmBillOutputSystem(), "1"));

		// 145 今回の請求回数
		Assert.assertTrue("今回の請求回数が1(固定)であること", accounting.getFfmThisBillingCnt() == 1);

		// 147 コメント１
		String halfWidthCompanyKana = Optional.ofNullable(customerContract.getCompanyNameKana()).filter(s -> StringUtils.isNotEmpty(s)).map(s -> {
			Transliterator transliterator = Transliterator.getInstance("Fullwidth-Halfwidth");
			return transliterator.transliterate(s);
		}).orElse("");
		if (StringUtils.isNotEmpty(contract.getPurchaseManageNumber())) {
			Assert.assertEquals("コメント１が契約.RJ管理番号 ＋ 契約.納品書・請求書印字用コメント ＋ 顧客の企業名（カナ）を半角カナ変換と同じであること", String.format("%s %s %s", contract.getRjManageNumber(), contract.getPurchaseManageNumber(), halfWidthCompanyKana), accounting.getFfmOutputComment1());
		} else if (StringUtils.isNotEmpty(halfWidthCompanyKana)) {
			Assert.assertEquals("コメント１が契約.RJ管理番号 ＋ 顧客の企業名（カナ）を半角カナ変換と同じであること", String.format("%s %s", contract.getRjManageNumber(), halfWidthCompanyKana), accounting.getFfmOutputComment1());
		} else {
			Assert.assertEquals("コメント１が契約.RJ管理番号と同じであること", contract.getRjManageNumber(), accounting.getFfmOutputComment1());
		}

		// 154 納品場所識別
		Assert.assertTrue("納品場所識別が11(固定)であること", StringUtils.equals(accounting.getFfmDstType(), "11"));
	}

	private void データ作成区分_31_振替の個別チェック(Accounting accounting) {
		Contract contract = contractRepository.findOne(accounting.getContractId());
		ContractDetail contractDetail = contract.getContractDetailList().stream()
				.filter(d -> d.getId() == accounting.getContractDetailId()).findFirst().get();
		ItemContract itemContract = contractDetail.getItemContract();
		List<ItemDetailContract> itemDetailContractList = itemContract.getItemDetailContractList().stream()
				.filter(idc -> idc.getInitialRunningDiv() == InitialRunningDiv.ランニング).collect(Collectors.toList());
		List<MvWjmoc020OrgAllInfoCom> orgMasterList = new ArrayList<>();

		// 得意先コードをキーに導出した売上担当社員の従業員番号を取得
		String empNumber = mvEmployeeMasterRepository.findByMomEmployeeId(mvTJmci108MasterRepository.findByCustomerSiteNumber(mvTJmci101MasterRepository.findByOriginalSystemCode(contract.getBillingCustomerSpCode()).getCustomerSiteNumber()).getSusSalMomShainCd()).getEmpNumber();

		// 品種明細（契約用）の振替先課所コードと紐づくoM組織情報提供マスタ.CUBIC部門コードのリストを取得
		try {
			Date date = (new SimpleDateFormat("yyyy/MM/dd")).parse("2019/08/19");
			itemDetailContractList.stream().map(s -> s.getTransToServiceOrgCode()).forEach(s -> {
				orgMasterList.add(mvWjmoc020OrgAllInfoComRepository.findByOrgId(s, date));
			});
		} catch (Exception neverOccur) {
			throw new RuntimeException();
		}

		// 22 作成データパターン
		Assert.assertTrue("作成データパターンが31(固定)であること", StringUtils.equals(accounting.getFfmDataPtn(), "31"));

		// 47 振替先課所コード
		if (accounting.getFfmTrnsLocationCd() == null) {
			Assert.assertTrue("振替先課所コードが'MoM組織情報提供マスタ.CUBIC部門コードであること", orgMasterList.stream()
					.anyMatch(oml -> oml.getCubicOrgId() == null));
		} else {
			Assert.assertTrue("振替先課所コードが'MoM組織情報提供マスタ.CUBIC部門コードであること", orgMasterList.stream()
					.anyMatch(oml -> accounting.getFfmTrnsLocationCd().equals(oml.getCubicOrgId())));
		}

		// 77 売上社員設定区分
		Assert.assertTrue("売上社員設定区分が1(固定)であること", StringUtils.equals(accounting.getFfmSalesEmpType(), "1"));

		// 78 売上社員コード
		Assert.assertTrue("売上社員コードが得意先コードをキーに導出した売上担当社員の従業員番号コードであること", StringUtils.equals(accounting.getFfmSalesEmpCd(), empNumber));

		// 83 振替先振替金額
		Assert.assertTrue("振替先振替金額が品種明細(契約用).原価＊契約明細.数量であること", itemDetailContractList.stream()
				.anyMatch(idc -> accounting.getFfmTrnsPrice().compareTo(idc.getPrice().multiply(new BigDecimal(contractDetail.getQuantity()))) == 0));
	}

	private void 課金計上テーブル登録データ共通チェック(Accounting accounting) throws ParseException {
		Contract contract = contractRepository.findOne(accounting.getContractId());
		ContractDetail contractDetail = contract.getContractDetailList().stream()
				.filter(d -> d.getId() == accounting.getContractDetailId()).findFirst().get();
		ItemContract itemContract = contractDetail.getItemContract();

		// 2 RJ管理番号
		Assert.assertTrue("RJ管理番号が契約.RJ管理番号と同じであること",
				StringUtils.equals(accounting.getRjManageNumber(), contract.getRjManageNumber()));
		// 3 契約ID(COTOS_一意ID)
		Assert.assertTrue("契約ID(COTOS_一意ID)が契約.契約IDと同じであること", accounting.getContractId() == contract.getId());
		// 4 契約明細ID(COTOS_一意ID)
		Assert.assertTrue("契約明細ID(COTOS_一意ID)が契約明細.契約明細IDと同じであること",
				accounting.getContractDetailId() == contractDetail.getId());
		// 5 取引年月日
		Assert.assertNull("取引年月日がNullであること", accounting.getTransactionDate());
		// 6 締め日
		Assert.assertNull("締日がNullであること", accounting.getClosingDate());
		// 10 請求年月
		Assert.assertNull("請求年月がNullであること", accounting.getBillingDate());
		// 11 サービス期間開始日
		Assert.assertNull("サービス期間開始日がNullであること", accounting.getSrvStartDate());
		// 12 サービス期間開始日
		Assert.assertNull("サービス期間終了日がNullであること", accounting.getSrvEndDate());
		// 13 注文番号
		Assert.assertNull("注文番号がNullであること", accounting.getWebOrderNo());
		// 17 CUBIC計上処理フラグ
		Assert.assertNull("CUBIC計上処理フラグがNullであること", accounting.getCubicFlg());
		// 20 会社コード
		Assert.assertEquals("会社コードが、3139(固定値)であること。", "3139", accounting.getFfmCompanyCd());
		// 21 契約種類区分
		Assert.assertEquals("契約種類区分が、28(固定値)であること。", "28", accounting.getFfmContractTypeKbn());
		// 23 勘定識別
		Assert.assertNull("勘定識別がNullであること", accounting.getFfmAccountType());
		// 24 データ種類
		Assert.assertEquals("データ種類が、C2103(固定値)であること。", "C2103", accounting.getFfmDataType());
		// 25 赤黒区分
		Assert.assertEquals("赤黒区分が、0(固定値)であること。", "0", accounting.getFfmRedBlackType());
		// 26 債権債務照合キー
		Assert.assertNull("債権債務照合キーがnullであること", accounting.getFfmMatchingKey());
		// 27 NSPユニークキー
		Assert.assertTrue("NSPユニークキーが契約.契約番号＋品種（契約用）.リコー品種コード＋計上IDと同じであること",
				StringUtils.equals(accounting.getFfmNspKey(),
						accounting.getFfmContractDocNo().substring(3) + itemContract.getRicohItemCode() + accounting.getId()));
		// 28 案件番号
		Assert.assertTrue("案件番号がRJ管理番号と同じであること",
				StringUtils.equals(accounting.getFfmProjectNo(), accounting.getRjManageNumber()));
		// 29 契約書番号
		Assert.assertTrue("契約書番号が契約.契約番号であること",
				StringUtils.equals(accounting.getFfmContractDocNo(), contract.getContractNumber()));
		// 30 契約番号
		Assert.assertNull("契約番号がNullであること", accounting.getFfmContractNo());
		// 31 契約明細番号
		Assert.assertNull("契約明細番号がNullであること", accounting.getFfmContractDetailNo());
		// 32 請求明細番号
		Assert.assertNull("請求明細番号がNullであること", accounting.getFfmBillingDetailNo());
		// 33 お問い合わせ番号
		Assert.assertNull("お問い合わせ番号がNullであること", accounting.getFfmInqNo());
		// 34 お問い合わせ明細番号
		Assert.assertNull("お問い合わせ明細番号がNullであること", accounting.getFfmInqDetailNo());
		// 35 手配時の案件番号
		Assert.assertNull("手配時の案件番号がNullであること", accounting.getFfmArrProjectNo());
		// 36 手配時の問合せ番号
		Assert.assertNull("手配時の問合せ番号がNullであること", accounting.getFfmArrInqNo());
		// 37 赤伝理由
		Assert.assertNull("赤伝理由がNullであること", accounting.getFfmCancelReason());
		// 38 元契約番号
		Assert.assertNull("元契約番号がNullであること", accounting.getFfmOrgContractCd());
		// 39 元請求明細番号
		Assert.assertNull("元請求明細番号がNullであること", accounting.getFfmOrgContractDetailNo());
		// 40 請求条件
		Assert.assertNull("請求条件がNullであること", accounting.getFfmBillingCondition());
		// 41 請求分割回数
		Assert.assertNull("請求分割回数がNullであること", accounting.getFfmTotalBillingCount());
		// 42 契約締結日
		Assert.assertNull("契約締結日がNullであること", accounting.getFfmContractDate());
		// 43 契約期間(開始)
		if (accounting.getFfmContractPeriodStart() != null && contract.getServiceTermStart() != null) {
			Assert.assertTrue("契約期間（開始）が契約.サービス開始日と同じであること",
					DateUtils.isSameDay(new SimpleDateFormat("yyyyMMdd").parse(accounting.getFfmContractPeriodStart()),
							contract.getServiceTermStart()));
		}
		// 44 契約期間(終了)
		if (accounting.getFfmContractPeriodEnd() != null && contract.getServiceTermEnd() != null) {
			Assert.assertTrue("契約期間(終了)が契約.サービス終了日と同じであること",
					DateUtils.isSameDay(new SimpleDateFormat("yyyyMMdd").parse(accounting.getFfmContractPeriodEnd()),
							contract.getServiceTermEnd()));
		}
		// 45 契約SSコード
		Assert.assertNull("契約SSコードがNullであること", accounting.getFfmContractSscd());
		// 46 契約SS社員コード
		Assert.assertNull("契約SS社員コードがNullであること", accounting.getFfmContractSspiccd());
		// 48 振替先社員コード
		Assert.assertNull("振替先社員コードがNullであること", accounting.getFfmTrnsPicCd());
		// 49 保守契約／リース/レンタルＮｏ
		Assert.assertNull("保守契約／リース/レンタルＮｏがNullであること", accounting.getFfmMntLeaseNo());
		// 51 仕切前計上金額
		Assert.assertNull("仕切前計上金額がNullであること", accounting.getFfmPriceBeforeInvoice());
		// 52 仕切前消費税額
		Assert.assertNull("仕切前消費税額がNullであること", accounting.getFfmTaxPriceBeforeInvoice());
		// 53 商品コード
		Assert.assertTrue("商品コードが'品種（契約用）.リコー品種コードと同じであること",
				StringUtils.equals(accounting.getFfmProdactCd(), itemContract.getRicohItemCode()));
		// 54 機種略号
		Assert.assertNull("機種略号がNullであること", accounting.getFfmModelId());
		// 55 機番
		Assert.assertNull("機番がNullであること", accounting.getFfmSerialId());
		// 56 見積時の商品入力名
		Assert.assertNull("見積時の商品入力名がNullであること", accounting.getFfmQuotationProdactName());
		// 57 原価計上商品コード
		Assert.assertNull("原価計上商品コードがNullであること", accounting.getFfmCostProdactName());
		// 58 仕入れ区分
		Assert.assertNull("仕入れ区分がNullであること", accounting.getFfmPurchaseType());
		// 59 仕入値引区分
		Assert.assertNull("仕入値引区分がNullであること", accounting.getFfmPurchaseDiscntType());
		// 60 仕入購買区分
		Assert.assertNull("仕入購買区分がNullであること", accounting.getFfmPurchaseClassType());
		// 61 仕入取引日
		Assert.assertNull("仕入取引日がNullであること", accounting.getFfmPurchaseDate());
		// 62 他社商品区分
		Assert.assertNull("他社商品区分がNullであること", accounting.getFfmNonRItemCd());
		// 63 仕入取引先コード
		Assert.assertNull("仕入取引先コードがNullであること", accounting.getFfmSupplierCd());
		// 64 仕入課所設定区分
		Assert.assertNull("仕入課所設定区分がNullであること", accounting.getFfmDeptAssortType());
		// 65 仕入課所コード
		Assert.assertNull("仕入課所コードがNullであること", accounting.getFfmPurchaseLocationCd());
		// 67 在庫区コード
		Assert.assertNull("在庫区コードがNullであること", accounting.getFfmRdStrctInventoryCd());
		// 68 特価番号
		Assert.assertNull("特価番号コードがNullであること", accounting.getFfmDealsNo());
		// 69 仕入先請求ＮＯ
		Assert.assertNull("仕入先請求ＮＯがNullであること", accounting.getFfmSupplierBillingNo());
		// 70 商品名（支払通知書用）
		Assert.assertNull("商品名（支払通知書用）がNullであること", accounting.getFfmPaymentProdactName());
		// 71 売上区分
		Assert.assertTrue("売上区分が00(固定文字列)と同じであること", StringUtils.equals(accounting.getFfmSalesType(), "00"));
		// 72 売上値引区分
		Assert.assertNull("売上値引区分がNullであること", accounting.getFfmSalesDiscountType());
		// 73 売上取引日（納品日）
		Calendar ffmSalesTradeDate = Calendar.getInstance();
		ffmSalesTradeDate.set(Calendar.DAY_OF_MONTH, 1);
		Assert.assertTrue("売上取引日が請求月（データ作成日）の月1日と同じであること", StringUtils.equals(accounting.getFfmSalesTradeDate(),
				new SimpleDateFormat("yyyyMMdd").format(ffmSalesTradeDate.getTime())));
		// 74 得意先コード
		Assert.assertTrue("得意先コードが契約.得意先コードと同じであること",
				StringUtils.equals(accounting.getFfmClientCd(), contract.getBillingCustomerSpCode()));
		// 75 売上課所設定区分
		Assert.assertNull("売上課所設定区分がNullであること", accounting.getFfmSalesLocationType());
		// 76 売上課所コード
		Assert.assertNull("売上課所コードがNullであること", accounting.getFfmSalesLocationCd());
		// 79 値引番号
		Assert.assertNull("値引番号がNullであること", accounting.getFfmDiscntNo());
		// 80 伝票番号
		Assert.assertNull("伝票番号がNullであること", accounting.getFfmSlipNo());
		// 81 契約区分
		Assert.assertNull("契約区分がNullであること", accounting.getFfmContractType());
		// 82 売上原価金額
		Assert.assertNull("売上原価金額がNullであること", accounting.getFfmRevenueCostprice());
		// 85 売上数量
		Assert.assertNull("売上数量がNullであること", accounting.getFfmUserSalesCnt());
		// 86 ユーザ売上単価
		Assert.assertNull("ユーザ売上単価がNullであること", accounting.getFfmUserSalesPrice());
		// 87 ユーザ売上単価（税込）
		Assert.assertNull("ユーザ売上単価（税込）がNullであること", accounting.getFfmUserSalesPriceInTax());
		// 88 ユーザ売上金額
		Assert.assertNull("ユーザ売上金額がNullであること", accounting.getFfmUserSalesAmt());
		// 89 ユーザ売上金額（税込）
		Assert.assertNull("ユーザ売上金額（税込）がNullであること", accounting.getFfmUserSalesAmtInTax());
		// 90 ユーザ売上消費税区分
		Assert.assertNull("ユーザ売上消費税区分がNullであること", accounting.getFfmUserSalesTaxType());
		// 91 ユーザ売上消費税率区分
		Assert.assertNull("ユーザ売上消費税率区分がNullであること", accounting.getFfmUserSalesTaxRate());
		// 92 ユーザ売上消費税額
		Assert.assertNull("ユーザ売上消費税額がNullであること", accounting.getFfmUserSalesTaxPrice());
		// 93 RJ売上数量 ffm_rj_sales_cnt
		Assert.assertTrue("RJ売上数量が契約明細.数量と同じであること", accounting.getFfmRjSalesCnt() == contractDetail.getQuantity());
		// 95 RJ売上単価（税込）
		Assert.assertNull("RJ売上単価（税込）がNullであること", accounting.getFfmRjSalesPriceInTax());
		// 97 RJ売上金額（税込）
		Assert.assertNull("RJ売上金額（税込）がNullであること", accounting.getFfmRjSalesAmtInTax());
		// 101 RJ仕入数量
		Assert.assertNull("RJ仕入数量がNullであること", accounting.getFfmRjPurchaseCnt());
		// 102 RJ仕入単価
		Assert.assertNull("RJ仕入単価がNullであること", accounting.getFfmRjPurchasePrice());
		// 103 RJ仕入単価（税込）
		Assert.assertNull("RJ仕入単価（税込）がNullであること", accounting.getFfmRjPurchasePriceInTax());
		// 104 RJ仕入金額
		Assert.assertNull("RJ仕入金額がNullであること", accounting.getFfmRjPurchaseAmt());
		// 105 RJ仕入金額（税込）
		Assert.assertNull("RJ仕入金額（税込）がNullであること", accounting.getFfmRjPurchaseAmtInTax());
		// 106 RJ仕入消費税区分
		Assert.assertNull("RJ仕入消費税区分がNullであること", accounting.getFfmRjPurchaseTaxType());
		// 107 RJ仕入消費税率区分
		Assert.assertNull("RJ仕入消費税率区分がNullであること", accounting.getFfmRjPurchaseTaxRate());
		// 108 RJ仕入消費税額
		Assert.assertNull("RJ仕入消費税額がNullであること", accounting.getFfmRjPurchaseTaxPrice());
		// 109 販売店売上数量 ffm_shop_sales_cnt
		Assert.assertNull("販売店売上数量がNullであること", accounting.getFfmShopSalesCnt());
		// 110 販売店売上単価
		Assert.assertNull("販売店売上単価がNullであること", accounting.getFfmShopSalesPrice());
		// 111 販売店売上単価（税込）
		Assert.assertNull("販売店売上単価（税込）がNullであること", accounting.getFfmShopSalesPriceInTax());
		// 112 販売店売上金額
		Assert.assertNull("販売店売上金額がNullであること", accounting.getFfmShopSalesAmt());
		// 113 販売店売上金額（税込）
		Assert.assertNull("販売店売上金額（税込）がNullであること", accounting.getFfmShopSalesAmtInTax());
		// 114 販売店売上消費税区分
		Assert.assertNull("販売店売上消費税区分がNullであること", accounting.getFfmShopSalesTaxType());
		// 115 販売店売上消費税率区分
		Assert.assertNull("販売店売上消費税率区分がNullであること", accounting.getFfmShopSalesTaxRate());
		// 116 販売店売上消費税額
		Assert.assertNull("販売店売上消費税額がNullであること", accounting.getFfmShopSalesTaxPrice());
		// 119 R原価単価（税込）
		Assert.assertNull("R原価単価（税込）がNullであること", accounting.getFfmRCostPriceInTax());
		// 121 R原価金額（税込）
		Assert.assertNull("R原価金額（税込）がNullであること", accounting.getFfmRCostAmtInTax());
		// 125 手数料数量 ffm_commission_cnt
		Assert.assertNull("R原価金額（税込）がNullであること", accounting.getFfmRCostAmtInTax());
		// 126 手数料単価
		Assert.assertNull("手数料単価がNullであること", accounting.getFfmCommissionPrice());
		// 127 手数料単価（税込）
		Assert.assertNull("手数料単価（税込）がNullであること", accounting.getFfmCommissionPriceInTax());
		// 128 手数料金額
		Assert.assertNull("手数料金額がNullであること", accounting.getFfmCommissionAmt());
		// 129 手数料金額（税込）
		Assert.assertNull("手数料金額（税込）がNullであること", accounting.getFfmCommissionAmtInTax());
		// 130 手数料消費税区分
		Assert.assertNull("手数料消費税区分がNullであること", accounting.getFfmCommissionTaxType());
		// 131 手数料消費税率区分
		Assert.assertNull("手数料消費税率区分がNullであること", accounting.getFfmCommissionTaxRate());
		// 132 手数料消費税額
		Assert.assertNull("手数料消費税額がNullであること", accounting.getFfmCommissionTaxPrice());
		// 133 請求書明細識別コード
		Assert.assertNull("請求書明細識別コードがNullであること", accounting.getFfmBillDetailCd());
		// 138 商品名パターン番号（納品書・請求書用）
		Assert.assertNull("商品名パターン番号（納品書・請求書用）がNullであること", accounting.getFfmProdactPtnNo());
		// 139 商品名（納品書・請求書用）
		Assert.assertNull("商品名（納品書・請求書用）がNullであること", accounting.getFfmProdactNameForBill());
		// 140 業務への連絡事項
		Assert.assertNull("業務への連絡事項がNullであること", accounting.getFfmMessageForBiz());
		// 141 備考（納品書・請求書用）
		Assert.assertNull("備考（納品書・請求書用）がNullであること", accounting.getFfmRemarkForBill());
		// 142 請求期間（開始）
		Assert.assertNull("請求期間（開始）がNullであること", accounting.getFfmRBillingPeriodStart());
		// 143 請求期間（終了）
		Assert.assertNull("請求期間（終了）がNullであること", accounting.getFfmRBillingPeriodEnd());
		// 144 請求月
		Assert.assertNull("請求月がNullであること", accounting.getFfmBillingYm());
		// 146 カウンター
		Assert.assertNull("カウンターがNullであること", accounting.getFfmCounter());
		// 148 コメント２
		Assert.assertNull("コメント２がNullであること", accounting.getFfmOutputComment2());
		// 149 強制フラグ
		Assert.assertNull("強制フラグがNullであること", accounting.getFfmForcedFlg());
		// 150 機器設置先名 ffm_installtion_name
		Assert.assertNull("強制フラグがNullであること", accounting.getFfmForcedFlg());
		// 151 機器設置先部課名
		Assert.assertNull("機器設置先部課名がNullであること", accounting.getFfmInstalltionDptName());
		// 152 RINGS届先コード(3桁）
		Assert.assertNull("RINGS届先コード(3桁）がNullであること", accounting.getFfmRingsDstCd());
		// 153 OE届先コード(11桁）
		Assert.assertNull("OE届先コード(11桁）がNullであること", accounting.getFfmOeDstCd());
		// 155 届先名１（会社名）
		Assert.assertNull("届先名１（会社名）がNullであること", accounting.getFfmDstName1());
		// 156 届先名２（会社部課名）
		Assert.assertNull("届先名２（会社部課名）がNullであること", accounting.getFfmDstName2());
		// 157 顧客名
		Assert.assertNull("顧客名がNullであること", accounting.getFfmDstClientName());
		// 158 届先住所１
		Assert.assertNull("届先住所１がNullであること", accounting.getFfmDstAddr1());
		// 159 届先住所２
		Assert.assertNull("届先住所２がNullであること", accounting.getFfmDstAddr2());
		// 160 届先住所３
		Assert.assertNull("届先住所３がNullであること", accounting.getFfmDstAddr3());
		// 161 届先郵便番号
		Assert.assertNull("届先郵便番号がNullであること", accounting.getFfmDstZipCd());
		// 162 届先電話番号
		Assert.assertNull("届先電話番号がNullであること", accounting.getFfmDstTel());
		// 163 届先ＦＡＸ番号
		Assert.assertNull("届先ＦＡＸ番号がNullであること", accounting.getFfmDstFax());
		// 164 届先名（カナ）
		Assert.assertNull("届先名（カナ）がNullであること", accounting.getFfmDstNameKana());
		// 165 得意先コード（二次店）
		Assert.assertNull("得意先コード（二次店）がNullであること", accounting.getFfmClientCdSec());
		// 166 届先コード（二次店）
		Assert.assertNull("届先コード（二次店）がNullであること", accounting.getFfmDstCdSec());
		// 167 支払利息相当額
		Assert.assertNull("支払利息相当額がNullであること", accounting.getFfmInterestExpensePrice());
		// 168 受取利息相当額
		Assert.assertNull("受取利息相当額がNullであること", accounting.getFfmInterestIncomePrice());
		// 169 見積番号
		Assert.assertNull("見積番号がNullであること", accounting.getFfmQuotationCd());
		// 170 見積明細番号
		Assert.assertNull("見積明細番号がNullであること", accounting.getFfmQuotationDetailCd());
		// 171 本体見積明細番号
		Assert.assertNull("本体見積明細番号がNullであること", accounting.getFfmMainQuotationDetailCd());
		// 172 請求書摘要
		Assert.assertNull("請求書摘要がNullであること", accounting.getChgBillingText());
		// 173 一次店_R会社コード
		Assert.assertNull("一次店_R会社コードがNullであること", accounting.getChgRCompanyCode1st());
		// 174 一次店_R会社名
		Assert.assertNull("一次店_R会社名がNullであること", accounting.getChgRCompanyName1st());
		// 175 一次店_販売店ID
		Assert.assertNull("一次店_販売店IDがNullであること", accounting.getChgShopId1st());
		// 176 一次店_販売店名
		Assert.assertNull("一次店_販売店名がNullであること", accounting.getChgShopName1st());
		// 177 一次店_販売店摘要
		Assert.assertNull("一次店_販売店摘要がNullであること", accounting.getChgShopText1st());
		// 178 二次店_R会社コード
		Assert.assertNull("二次店_R会社コードがNullであること", accounting.getChgRCompanyCode2st());
		// 179 二次店_R会社名
		Assert.assertNull("二次店_R会社名がNullであること", accounting.getChgRCompanyName2st());
		// 180 二次店_販売店ID
		Assert.assertNull("二次店_販売店IDがNullであること", accounting.getChgShopId2st());
		// 181 二次店_販売店名
		Assert.assertNull("二次店_販売店名がNullであること", accounting.getChgShopName2st());
		// 182 二次店_販売店摘要
		Assert.assertNull("二次店_販売店摘要がNullであること", accounting.getChgShopText2st());
		// 183 フォーマット種別
		Assert.assertNull("フォーマット種別がNullであること", accounting.getCubicFmtType());
		// 184 勘定科目コード
		Assert.assertNull("勘定科目コードがNullであること", accounting.getCubicAccountingCd());
		// 185 貸借区分
		Assert.assertNull("貸借区分がNullであること", accounting.getCubicLcType());
		// 186 システムコード
		Assert.assertNull("システムコードがNullであること", accounting.getCubicSystemCd());
		// 187 会社コード
		Assert.assertNull("会社コードがNullであること", accounting.getCubicCompanyCd());
		// 188 会計計上日
		Assert.assertNull("会計計上日がNullであること", accounting.getCubicAccountingDate());
		// 189 伝票ＮＯ
		Assert.assertNull("伝票ＮＯがNullであること", accounting.getCubicVoucherDate());
		// 190 伝票明細NO
		Assert.assertNull("伝票明細NOがNullであること", accounting.getCubicVoucherDetailDate());
		// 191 計上部門コード
		Assert.assertNull("計上部門コードがNullであること", accounting.getCubicAccountDeptCd());
		// 192 商品軸
		Assert.assertNull("商品軸がNullであること", accounting.getCubicProductAxis());
		// 193 営業軸
		Assert.assertNull("営業軸がNullであること", accounting.getCubicSalesAxis());
		// 194 決算識別子
		Assert.assertNull("決算識別子がNullであること", accounting.getCubicFinancialIdentifier());
		// 195 品種コード
		Assert.assertNull("品種コードがNullであること", accounting.getCubicProductTypeCd());
		// 196 増減理由
		Assert.assertNull("増減理由がNullであること", accounting.getCubicInDecReason());
		// 197 環境会計コード
		Assert.assertNull("環境会計コードがNullであること", accounting.getCubicEnvAccountCd());
		// 198 プロジェクトコード
		Assert.assertNull("プロジェクトコードがNullであること", accounting.getCubicProjectCd());
		// 199 数量
		Assert.assertNull("数量がNullであること", accounting.getCubicCount());
		// 200 取引金額
		Assert.assertNull("取引金額がNullであること", accounting.getCubicAmount());
		// 201 外貨取引金額
		Assert.assertNull("外貨取引金額がNullであること", accounting.getCubicAmountForForeign());
		// 202 通貨コード
		Assert.assertNull("通貨コードがNullであること", accounting.getCubicCurrencyCd());
		// 203 通貨換算タイプ
		Assert.assertNull("通貨換算タイプがNullであること", accounting.getCubicCurrencyConvType());
		// 204 通貨換算レート
		Assert.assertNull("通貨換算レートがNullであること", accounting.getCubicCurrencyConvRate());
		// 205 通貨換算日
		Assert.assertNull("通貨換算日がNullであること", accounting.getCubicCurrencyConvDate());
		// 206 摘要
		Assert.assertNull("摘要がNullであること", accounting.getCubicText());
		// 207 明細摘要
		Assert.assertNull("明細摘要がNullであること", accounting.getCubicTextDetail());
		// 208 各社セグメント
		Assert.assertNull("各社セグメントがNullであること", accounting.getCubicCoSegment());
		// 209 管理セグメント予備2
		Assert.assertNull("管理セグメント予備2がnullであること", accounting.getCubicCoSegment1());
		// 210 管理セグメント予備3
		Assert.assertNull("管理セグメント予備3がnullであること", accounting.getCubicCoSegment2());
		// 211 消し込みキー
		Assert.assertNull("消し込みキーがnullであること", accounting.getCubicDeleteKey());
		// 212 扱い者コード
		Assert.assertNull("扱い者コードがnullであること", accounting.getCubicOperatorCd());
		// 213 グリーン購買コード
		Assert.assertNull("グリーン購買コードがnullであること", accounting.getCubicGreenBuyCd());
		// 214 案件ＮＯ
		Assert.assertNull("案件ＮＯがnullであること", accounting.getCubicProjectNo());
		// 215 Ｄ／Ｆ ＮＯ
		Assert.assertNull("Ｄ／Ｆ　ＮＯがnullであること", accounting.getCubicDfNo());
		// 216 予算ＮＯ
		Assert.assertNull("予算ＮＯがnullであること", accounting.getCubicBudgetNo());
		// 217 顧客コード
		Assert.assertNull("顧客コードがnullであること", accounting.getCubicClientCd());
		// 218 各社固有管理セグメント1
		Assert.assertNull("各社固有管理セグメント1がnullであること", accounting.getCubicCoMgtSegment());
		// 219 取引日
		Assert.assertNull("取引日がnullであること", accounting.getCubicTransactionDate());
		// 220 請求先サイトコード
		Assert.assertNull("請求先サイトコードがnullであること", accounting.getCubicBillDstSiteCd());
		// 221 国内／海外区分
		Assert.assertNull("国内／海外区分がnullであること", accounting.getCubicDomesticForeignType());
		// 222 取引単価（税抜）
		Assert.assertNull("取引単価（税抜）がnullであること", accounting.getCubicSalesPriceNoTax());
		// 223 外貨取引単価
		Assert.assertNull("外貨取引単価がnullであること", accounting.getCubicSalesPriceForeign());
		// 224 回収条件名
		Assert.assertNull("回収条件名がnullであること", accounting.getCubicRecoveryReqName());
		// 225 回収方法名
		Assert.assertNull("回収方法名がnullであること", accounting.getCubicRecoveryMethodName());
		// 226 回収起算日
		Assert.assertNull("回収起算日がnullであること", accounting.getCubicRecoveryDate());
		// 227 請求分類名
		Assert.assertNull("請求分類名がnullであること", accounting.getCubicBillingTypeName());
		// 228 請求書明細識別コード
		Assert.assertNull("請求書明細識別コードがnullであること", accounting.getCubicBillDetailTypeCode());
		// 229 値引名称
		Assert.assertNull("値引名称コードがnullであること", accounting.getCubicDiscountName());
		// 230 請求書発行区分
		Assert.assertNull("請求書発行区分がnullであること", accounting.getCubicBillOutputType());
		// 231 請求書ＮＯ
		Assert.assertNull("請求書ＮＯがnullであること", accounting.getCubicBillingNo());
		// 232 荷為替手形ＮＯ
		Assert.assertNull("荷為替手形ＮＯがnullであること", accounting.getCubicDocumentaryBillNo());
		// 233 Ｐ／ＣキーＮＯ
		Assert.assertNull("Ｐ／ＣキーＮＯがnullであること", accounting.getCubicPcKeyNo());
		// 234 請求書出力用伝票ＮＯ
		Assert.assertNull("請求書出力用伝票ＮＯがnullであること", accounting.getCubicBillingOutputNo());
		// 235 受注ＮＯ
		Assert.assertNull("受注ＮＯがnullであること", accounting.getCubicReceivedOrderNo());
		// 236 発注ＮＯ
		Assert.assertNull("発注ＮＯがnullであること", accounting.getCubicOrderNo());
		// 237 前受管理ＮＯ
		Assert.assertNull("前受管理ＮＯがnullであること", accounting.getCubicBeforeManageNo());
		// 238 前受金消込額
		Assert.assertNull("前受金消込額がnullであること", accounting.getCubicBeforeCancelAmt());
		// 239 追加ＴＥＲＭ cubic_add_term
		Assert.assertNull("追加ＴＥＲＭがnullであること", accounting.getCubicAddTerm());
		// 240 契約NO
		Assert.assertNull("契約NOがnullであること", accounting.getCubicContractNo());
		// 241 品名
		Assert.assertNull("品名がnullであること", accounting.getCubicProdactName());
		// 242 債権債務照合キー
		Assert.assertNull("債権債務照合キーがnullであること", accounting.getCubicMatchingKey());
		// 243 汎用転送データ
		Assert.assertNull("汎用転送データがnullであること", accounting.getCubicGeneralTransferData());
		// 244 元伝票ＮＯ（赤伝時）
		Assert.assertNull("元伝票ＮＯ（赤伝時）がnullであること", accounting.getCubicOrgSlipNoForRed());
		// 245 元伝票明細ＮＯ（赤伝時）
		Assert.assertNull("元伝票明細ＮＯ（赤伝時）がnullであること", accounting.getCubicOrgSlipNoForRed());
		// 246 元会計計上日（赤伝時）
		Assert.assertNull("元会計計上日（赤伝時）がnullであること", accounting.getCubicOrgAcctDateForRed());
		// 247 設置先サイトコード
		Assert.assertNull("設置先サイトコードがnullであること", accounting.getCubicDstSiteCd());
		// 248 項目予備1
		Assert.assertNull("項目予備1がnullであること", accounting.getCubicItem1());
		// 249 項目予備2
		Assert.assertNull("項目予備2がnullであること", accounting.getCubicItem2());
		// 250 項目予備3
		Assert.assertNull("項目予備3がnullであること", accounting.getCubicItem3());
		// 251 拡張項目
		Assert.assertNull("拡張項目がnullであること", accounting.getExtendItem());
	}

	private void 契約明細の更新チェック(Long contractDetailId) {
		ContractDetail detail = contractDetailRepository.findOne(contractDetailId);

		// 契約明細.ランニング売上計上処理状態
		Assert.assertTrue("契約明細.ランニング売上計上処理状態が0:正常であること",
				detail.getRunningAccountSalesStatus() == RunningAccountSalesStatus.正常);

		// 契約明細.ランニング売上計上処理日
		Assert.assertTrue("契約明細.ランニング売上計上処理日がシステム日付であること",
				DateUtils.isSameDay(detail.getRunningAccountSalesDate(), new Date()));
	}

	private void 値検証(String baseDate) {
		// バッチ実行により生成されたAccountingテーブルを取得
		Iterable<Accounting> iterableAccounting = accountingRepository.findAll();
		// 全チェック対象データリスト
		List<Accounting> allTargetList = new ArrayList<>();
		// チェック対象売上請求データリスト (データ作成区分=20)
		List<Accounting> salesTargetList = new ArrayList<>();
		// チェック対象振替データリスト (データ作成区分=31)
		List<Accounting> transferTargetList = new ArrayList<>();

		// Accountingテーブルの内容を、データ作成区分ごとに振り分ける
		for (Accounting accounting : iterableAccounting) {
			allTargetList.add(accounting);
			if ("20".equals(accounting.getFfmDataPtn())) {
				salesTargetList.add(accounting);
			} else if ("31".equals(accounting.getFfmDataPtn())) {
				transferTargetList.add(accounting);
			}
			if (accounting.getContractId() == 3) {
				Assert.fail("対象外データ（計上年月日＞計上処理日）が抽出されています。");
			} else if (accounting.getContractId() == 4) {
				Assert.fail("対象外データ（費用種別!=月額(2or4)）が抽出されています。");
			} else if (accounting.getContractId() == 5) {
				Assert.fail("対象外データ（ランニング区分!=ランニング(2)）が抽出されています。");
			} else if (accounting.getContractId() == 6) {
				Assert.fail("対象外データ（ライフサイクル状態!=締結中(6)）が抽出されています。");
			} else if (accounting.getContractId() == 7) {
				Assert.fail("対象外データ（商品種類区分=CSP）が抽出されています。");
			}
		}

		// 全データ共通のチェック
		allTargetList.forEach(target -> {
			try {
				課金計上テーブル登録データ共通チェック(target);
			} catch (ParseException e) {
				Assert.fail("チェック処理に失敗しました");
			}
		});

		// データ作成区分＝20:売上請求の個別チェック
		// 契約明細の更新チェック
		salesTargetList.forEach(target -> {
			データ作成区分_20_売上請求チェック(target, baseDate);
			契約明細の更新チェック(target.getContractDetailId());
		});

		// データ作成区分=31:振替の個別チェック
		transferTargetList.forEach(target -> {
			データ作成区分_31_振替の個別チェック(target);
		});
	}

	private void 旧契約が計上処理対象となっていることを確認() {
		List<Accounting> accountingList = (List<Accounting>) accountingRepository.findAll();
		List<Accounting> accountingListContractId200 = accountingList.stream().filter(accounting -> accounting.getContractId() == 200).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(accountingListContractId200)) {
			Assert.fail("想定外のデータが対象になっている、あるいは対象データ無しとなっている。");
		} else {
			accountingListContractId200.stream().forEach(accounting -> {
				Contract contract = contractRepository.findOne(accounting.getContractId());
				Assert.assertEquals("旧契約の契約が処理対象になっていること", LifecycleStatus.旧契約, contract.getLifecycleStatus());
			});
		}
	}
}
