package jp.co.ricoh.cotos.batch.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.BatchApplication;
import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract.LifecycleStatus;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractAddedEditorEmp;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractApprovalRoute;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractAttachedFile;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractCheckResult;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractDetail;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractOperationLog;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractPicSaEmp;
import jp.co.ricoh.cotos.commonlib.entity.contract.CustomerContract;
import jp.co.ricoh.cotos.commonlib.entity.contract.DealerContract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ItemContract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ManagedEstimationDetail;
import jp.co.ricoh.cotos.commonlib.entity.contract.ProductContract;
import jp.co.ricoh.cotos.commonlib.security.CotosAuthenticationDetails;
import jp.co.ricoh.cotos.commonlib.util.BatchMomInfoProperties;
import jp.co.ricoh.cotos.component.RestApiClient;
import jp.co.ricoh.cotos.security.CreateJwt;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ImportReplyCsvTests extends TestBase {

	static ConfigurableApplicationContext context;

	@MockBean
	RestApiClient restApiClient;

	@Autowired
	CreateJwt createJwt;

	@Autowired
	BatchMomInfoProperties batchProperty;

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
			//context.getBean(DBConfig.class).clearData();
			context.stop();
		}
	}

	@Test
	public void 正常系_新規() throws IOException {
		Mockito.when(restApiClient.callFindTargetContractList(Mockito.anyObject())).thenReturn(dummyContractList("新規"));
		Mockito.when(restApiClient.callFindContract(Mockito.anyLong())).thenReturn(dummyContract("新規"));
		Mockito.doNothing().when(restApiClient).callUpdateContract(Mockito.anyObject());
		Mockito.doNothing().when(restApiClient).callCompleteArrangement(Mockito.anyLong());

		テストデータ作成("sql/insertTestData.sql");
		try {
			BatchApplication.main(new String[] { filePath, fileName });
		} catch (Exception e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void 正常系_容量変更() throws IOException {
		Mockito.when(restApiClient.callFindTargetContractList(Mockito.anyObject())).thenReturn(dummyContractList("容量変更"));
		Mockito.when(restApiClient.callFindContract(Mockito.anyLong())).thenReturn(dummyContract("容量変更"));
		Mockito.doNothing().when(restApiClient).callUpdateContract(Mockito.anyObject());
		Mockito.doNothing().when(restApiClient).callCompleteArrangement(Mockito.anyLong());
		テストデータ作成("sql/insertTestData.sql");
		try {
			BatchApplication.main(new String[] { filePath, fileName });
		} catch (Exception e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void 正常系_有償交換() throws IOException {
		Mockito.when(restApiClient.callFindTargetContractList(Mockito.anyObject())).thenReturn(dummyContractList("有償交換"));
		Mockito.when(restApiClient.callFindContract(Mockito.anyLong())).thenReturn(dummyContract("有償交換"));
		Mockito.doNothing().when(restApiClient).callUpdateContract(Mockito.anyObject());
		Mockito.doNothing().when(restApiClient).callCompleteArrangement(Mockito.anyLong());

		テストデータ作成("sql/insertTestData.sql");
		try {
			BatchApplication.main(new String[] { filePath, fileName });
		} catch (Exception e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void 異常系_JOB_パラメーター数不一致() {
		try {
			// パラメータ無し
			BatchApplication.main(new String[] {});
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}

		try {
			// パラメータ1つ
			BatchApplication.main(new String[] { filePath });
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}

		try {
			// パラメータ3つ
			BatchApplication.main(new String[] { filePath, fileName, "dummy" });
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_JOB_ディレクトリが存在しない() throws IOException {
		// 出力ファイルパス　※テスト環境に存在しないこと
		String filePath = "dummy.csv";

		try {
			BatchApplication.main(new String[] { filePath, fileName });
			Assert.fail("ディレクトリが存在しない状態で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_JOB_ファイルが存在しない() throws IOException {
		String fileName = "dummy.csv";

		try {
			BatchApplication.main(new String[] { filePath, fileName });
			Assert.fail("ファイルが存在しない状態で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}
	}

	private void テストデータ作成(String sql) {
		context.getBean(DBConfig.class).clearData();
		context.getBean(DBConfig.class).initTargetTestData(sql);
	}

	private Contract dummyContract(String type) {
		Contract contract = new Contract();
		contract.setId(10L);
		contract.setLifecycleStatus(LifecycleStatus.締結中);
		contract.setRjManageNumber("rj_manage_number");
		contract.setEstimationId(4L);
		contract.setServiceTermEnd(new Date());
		ContractDetail contractDetail = new ContractDetail();
		contractDetail.setItemContract(new ItemContract());
		contract.setContractDetailList(Arrays.asList(contractDetail));

		contract.setContractCheckResultList(Arrays.asList(new ContractCheckResult()));

		contract.setContractApprovalRouteList(Arrays.asList(new ContractApprovalRoute()));

		contract.setContractAttachedFileList(Arrays.asList(new ContractAttachedFile()));

		contract.setContractPicSaEmp(new ContractPicSaEmp());

		contract.setContractAddedEditorEmpList(Arrays.asList(new ContractAddedEditorEmp()));

		contract.setDealerContractList(Arrays.asList(new DealerContract()));

		contract.setCustomerContract(new CustomerContract());

		contract.setContractOperationLogList(Arrays.asList(new ContractOperationLog()));

		contract.setContractNumber("CC2020010700001");
		contract.setContractBranchNumber(1);
		ProductContract productContract = new ProductContract();
		productContract.setProductMasterId(1002L);
		productContract.setExtendsParameter("{\"tenantTakeOverFlg\":null,\"subscriptionNumber\":null,\"zuoraAccountId\":null,\"tenantId\":null,\"userId\":null}");
		if (type.equals("新規")) {
			productContract.setExtendsParameterIterance("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"新規\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"\"},{\"id\":2,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"\"},{\"id\":3,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"追加1\",\"invoiceNumber\":\"\"}]}");
		} else if (type.equals("容量変更")) {
			productContract.setExtendsParameterIterance("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"容量変更\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"08012345670\",\"serialNumber\":\"\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"\"},{\"id\":2,\"contractType\":\"容量変更\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345671\",\"serialNumber\":\"\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"\"},{\"id\":3,\"contractType\":\"容量変更\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345672\",\"serialNumber\":\"\",\"device\":\"追加1\",\"invoiceNumber\":\"\"}]}");
		} else if (type.equals("有償交換")) {
			productContract.setExtendsParameterIterance("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"有償交換\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"08012345670\",\"serialNumber\":\"\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"\"},{\"id\":2,\"contractType\":\"有償交換\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345671\",\"serialNumber\":\"\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"\"},{\"id\":3,\"contractType\":\"有償交換\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345672\",\"serialNumber\":\"\",\"device\":\"追加1\",\"invoiceNumber\":\"\"}]}");
		} else if (type.equals("失敗")) {
			productContract.setExtendsParameterIterance("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"新規\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"\"},{\"id\":2,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"\"},{\"id\":3,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"追加1\",\"invoiceNumber\":\"\"}]}");
		}
		contract.setProductContractList(Arrays.asList(productContract));

		contract.setManagedEstimationDetailList(Arrays.asList(new ManagedEstimationDetail()));

		return contract;
	}

	private List<Contract> dummyContractList(String type) {
		Contract contract = new Contract();
		contract.setId(10L);
		contract.setLifecycleStatus(LifecycleStatus.締結中);
		contract.setRjManageNumber("rj_manage_number");
		contract.setEstimationId(4L);
		contract.setServiceTermEnd(new Date());
		ContractDetail contractDetail = new ContractDetail();
		contractDetail.setItemContract(new ItemContract());
		contract.setContractDetailList(Arrays.asList(contractDetail));

		contract.setContractCheckResultList(Arrays.asList(new ContractCheckResult()));

		contract.setContractApprovalRouteList(Arrays.asList(new ContractApprovalRoute()));

		contract.setContractAttachedFileList(Arrays.asList(new ContractAttachedFile()));

		contract.setContractPicSaEmp(new ContractPicSaEmp());

		contract.setContractAddedEditorEmpList(Arrays.asList(new ContractAddedEditorEmp()));

		contract.setDealerContractList(Arrays.asList(new DealerContract()));

		contract.setCustomerContract(new CustomerContract());

		contract.setContractOperationLogList(Arrays.asList(new ContractOperationLog()));

		contract.setContractNumber("CC2020010700001");
		contract.setContractBranchNumber(1);
		ProductContract productContract = new ProductContract();
		productContract.setProductMasterId(1002L);
		productContract.setExtendsParameter("{\"tenantTakeOverFlg\":null,\"subscriptionNumber\":null,\"zuoraAccountId\":null,\"tenantId\":null,\"userId\":null}");
		if (type.equals("新規")) {
			productContract.setExtendsParameterIterance("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"新規\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"\"},{\"id\":2,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"\"},{\"id\":3,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"追加1\",\"invoiceNumber\":\"\"}]}");
		} else if (type.equals("容量変更")) {
			productContract.setExtendsParameterIterance("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"容量変更\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"08012345670\",\"serialNumber\":\"\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"\"},{\"id\":2,\"contractType\":\"容量変更\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345671\",\"serialNumber\":\"\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"\"},{\"id\":3,\"contractType\":\"容量変更\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345672\",\"serialNumber\":\"\",\"device\":\"追加1\",\"invoiceNumber\":\"\"}]}");
		} else if (type.equals("有償交換")) {
			productContract.setExtendsParameterIterance("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"有償交換\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"08012345670\",\"serialNumber\":\"\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"\"},{\"id\":2,\"contractType\":\"有償交換\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345671\",\"serialNumber\":\"\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"\"},{\"id\":3,\"contractType\":\"有償交換\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345672\",\"serialNumber\":\"\",\"device\":\"追加1\",\"invoiceNumber\":\"\"}]}");
		} else if (type.equals("失敗")) {
			productContract.setExtendsParameterIterance("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"新規\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"\"},{\"id\":2,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"\"},{\"id\":3,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"追加1\",\"invoiceNumber\":\"\"}]}");
		}
		contract.setProductContractList(Arrays.asList(productContract));

		contract.setManagedEstimationDetailList(Arrays.asList(new ManagedEstimationDetail()));

		return Arrays.asList(contract);
	}

}