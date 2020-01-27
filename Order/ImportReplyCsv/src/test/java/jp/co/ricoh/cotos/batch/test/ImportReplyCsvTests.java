package jp.co.ricoh.cotos.batch.test;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.entity.arrangement.Arrangement;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract.WorkflowStatus;
import jp.co.ricoh.cotos.commonlib.entity.contract.ProductContract;
import jp.co.ricoh.cotos.commonlib.repository.arrangement.ArrangementRepository;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractRepository;
import jp.co.ricoh.cotos.commonlib.repository.contract.ProductContractRepository;
import jp.co.ricoh.cotos.commonlib.security.CotosAuthenticationDetails;
import jp.co.ricoh.cotos.commonlib.util.BatchMomInfoProperties;
import jp.co.ricoh.cotos.logic.JobComponent;
import jp.co.ricoh.cotos.security.CreateJwt;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ImportReplyCsvTests extends TestBase {

	static ConfigurableApplicationContext context;

	static String extendsParameterContractId10 = "{\"extendsParameterList\":[{\"id\":2,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345671\",\"serialNumber\":\"8981200012345678911\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"CIC202001070001002\"},{\"id\":4,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345672\",\"serialNumber\":\"8981200012345678912\",\"device\":\"追加1\",\"invoiceNumber\":\"CIC202001070001003\"},{\"id\":1,\"contractType\":\"新規\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"08012345670\",\"serialNumber\":\"8981200012345678910\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"CIC202001070001001\"}]}";
	static String extendsParameterContractId20 = "{\"extendsParameterList\":[{\"id\":2,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345671\",\"serialNumber\":\"8981200012345678911\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"CIC202001080001002\"},{\"id\":4,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345672\",\"serialNumber\":\"8981200012345678912\",\"device\":\"追加1\",\"invoiceNumber\":\"CIC202001080001003\"},{\"id\":1,\"contractType\":\"新規\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"08012345670\",\"serialNumber\":\"8981200012345678910\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"CIC202001080001001\"}]}";

	@Autowired
	BatchMomInfoProperties batchProperty;

	@Autowired
	CreateJwt createJwt;

	@Autowired
	JobComponent jobComponent;

	@Autowired
	DBUtil dbUtil;

	@Autowired
	ContractRepository contractRepository;

	@Autowired
	ArrangementRepository arrangementRepository;

	@Autowired
	ProductContractRepository productContractRepository;

	@Autowired
	public void injectContext(ConfigurableApplicationContext injectContext) {
		String jwt = createJwt.execute();
		CotosAuthenticationDetails principal = new CotosAuthenticationDetails(batchProperty.getMomEmpId(), "sid", null, null, jwt, true, true, null);
		Authentication auth = new PreAuthenticatedAuthenticationToken(principal, null, null);
		SecurityContextHolder.getContext().setAuthentication(auth);
		context = injectContext;
		context.getBean(DBConfig.class).clearData();
	}

	@AfterClass
	public static void exit() throws Exception {
		if (null != context) {
			context.stop();
		}
	}

	private void テストデータ作成(String sql) {
		context.getBean(DBConfig.class).clearData();
		context.getBean(DBConfig.class).initTargetTestData(sql);
	}

	@Test
	@Ignore //APIコールが必要なテストであるため、検証時はcotos_devなどに向けて行なってください
	public void 正常系_リプライCSV取込() {

		テストデータ作成("sql/insertTestData.sql");
		jobComponent.run(new String[] { "src/test/resources/csv", "test.csv" });

		Contract contract10 = contractRepository.findOne(10L);
		ProductContract product1001 = productContractRepository.findOne(1001L);
		Assert.assertEquals("売上可能に更新されていること", WorkflowStatus.売上可能, contract10.getWorkflowStatus());
		Assert.assertEquals("拡張項目が設定されていること", extendsParameterContractId10, product1001.getExtendsParameterIterance());
		Arrangement arrangement1 = arrangementRepository.findByContractIdAndDisengagementFlg(contract10.getId(), 0);
		Assert.assertEquals("手配完了に更新されていること", Arrangement.WorkflowStatus.手配完了, arrangement1.getWorkflowStatus());

		Contract contract20 = contractRepository.findOne(20L);
		ProductContract product2001 = productContractRepository.findOne(2001L);
		Assert.assertEquals("売上可能に更新されていること", WorkflowStatus.売上可能, contract20.getWorkflowStatus());
		Assert.assertEquals("売上可能に更新されていること", extendsParameterContractId20, product2001.getExtendsParameterIterance());
		Arrangement arrangement2 = arrangementRepository.findByContractIdAndDisengagementFlg(contract20.getId(), 0);
		Assert.assertEquals("手配完了に更新されていること", Arrangement.WorkflowStatus.手配完了, arrangement2.getWorkflowStatus());

	}

	@Test
	public void 正常系_空ファイル_処理終了() {
		try {
			jobComponent.run(new String[] { "src/test/resources/csv", "empty.csv" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}
	}

	@Test
	@Ignore //APIコールが必要なテストであるため、検証時はcotos_devなどに向けて行なってください
	public void 正常系_リプライCSV取込_拡張項目読込失敗() {

		テストデータ作成("sql/insertTestDataExtendsParameterError.sql");
		jobComponent.run(new String[] { "src/test/resources/csv", "test.csv" });

		Contract contract10 = contractRepository.findOne(10L);
		Assert.assertEquals("売上可能に更新されていないこと", WorkflowStatus.承認済, contract10.getWorkflowStatus());
		Arrangement arrangement1 = arrangementRepository.findByContractIdAndDisengagementFlg(contract10.getId(), 0);
		Assert.assertEquals("手配完了に更新されていないこと", Arrangement.WorkflowStatus.手配中, arrangement1.getWorkflowStatus());

		Contract contract20 = contractRepository.findOne(20L);
		ProductContract product2001 = productContractRepository.findOne(2001L);
		Assert.assertEquals("売上可能に更新されていること", WorkflowStatus.売上可能, contract20.getWorkflowStatus());
		Assert.assertEquals("拡張項が設定されていること", extendsParameterContractId20, product2001.getExtendsParameterIterance());
		Arrangement arrangement2 = arrangementRepository.findByContractIdAndDisengagementFlg(contract20.getId(), 0);
		Assert.assertEquals("手配完了に更新されていること", Arrangement.WorkflowStatus.手配完了, arrangement2.getWorkflowStatus());

	}

	@Test
	@Ignore //APIコールが必要なテストであるため、検証時はcotos_devなどに向けて行なってください
	public void 正常系_リプライCSV取込_契約更新失敗() {

		テストデータ作成("sql/insertTestDataUpdateError.sql");
		jobComponent.run(new String[] { "src/test/resources/csv", "test.csv" });

		Contract contract10 = contractRepository.findOne(10L);
		Assert.assertEquals("売上可能に更新されていないこと", WorkflowStatus.承認済, contract10.getWorkflowStatus());
		Arrangement arrangement1 = arrangementRepository.findByContractIdAndDisengagementFlg(contract10.getId(), 0);
		Assert.assertEquals("手配完了に更新されていないこと", Arrangement.WorkflowStatus.手配中, arrangement1.getWorkflowStatus());

		Contract contract20 = contractRepository.findOne(20L);
		ProductContract product2001 = productContractRepository.findOne(2001L);
		Assert.assertEquals("売上可能に更新されていること", WorkflowStatus.売上可能, contract20.getWorkflowStatus());
		Assert.assertEquals("拡張項が設定されていること", extendsParameterContractId20, product2001.getExtendsParameterIterance());
		Arrangement arrangement2 = arrangementRepository.findByContractIdAndDisengagementFlg(contract20.getId(), 0);
		Assert.assertEquals("手配完了に更新されていること", Arrangement.WorkflowStatus.手配完了, arrangement2.getWorkflowStatus());

	}

	@Test
	public void 異常系_JOB_パラメーター数不一致() {
		try {
			jobComponent.run(new String[] { "dummy" });
			Assert.fail("パラメータがないのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ステータス", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_JOB_存在しないファイル() {
		try {
			jobComponent.run(new String[] { "src/test/resources/csv", "dummy.csv" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ステータス", 1, e.getStatus());
		}
	}

}
