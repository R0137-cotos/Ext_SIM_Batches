package jp.co.ricoh.cotos.batch.test;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractAddedEditorEmp;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractDetail;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractPicSaEmp;
import jp.co.ricoh.cotos.commonlib.entity.contract.CustomerContract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ProductContract;
import jp.co.ricoh.cotos.commonlib.security.CotosAuthenticationDetails;
import jp.co.ricoh.cotos.commonlib.util.BatchMomInfoProperties;
import jp.co.ricoh.cotos.component.RestApiClient;
import jp.co.ricoh.cotos.security.CreateJwt;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CreateOrderCsvTests extends TestBase {

	static ConfigurableApplicationContext context;

	final private String outputPath = "output/";

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
			context.getBean(DBConfig.class).clearData();
			context.stop();
		}
	}

	private void fileDeleate(String pathname) {
		File file = new File(pathname);

		if (!file.exists()) {
			System.out.println("ファイル:[" + pathname + "]が存在しません");
			return;
		}
		if (file.delete()) {
			System.out.println("ファイル:[" + pathname + "]の削除に成功しました");
			return;
		}
		System.out.println("ファイル:[" + pathname + "]の削除に失敗しました");
	}

	@Test
	public void 正常系_ジョブテスト() {
		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		Mockito.doNothing().when(restApiClient).callContractApi(anyObject());

		try {
			BatchApplication.main(new String[] { "20191018", outputPath, "result_initial.csv", "1" });
		} catch (Exception e) {
			Assert.fail("エラーが発生した。");
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_ジョブテスト_容量変更() {
		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		Mockito.doNothing().when(restApiClient).callContractApi(anyObject());

		try {
			BatchApplication.main(new String[] { "20190926", outputPath, "result_initial.csv", "2" });
		} catch (Exception e) {
			Assert.fail("エラーが発生した。");
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_ジョブテスト_有償交換() {
		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		Mockito.doNothing().when(restApiClient).callContractApi(anyObject());

		try {
			BatchApplication.main(new String[] { "20191028", outputPath, "result_initial.csv", "3" });
		} catch (Exception e) {
			Assert.fail("エラーが発生した。");
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 異常系_ジョブテスト_パラメータ無し() {
		try {
			BatchApplication.main(new String[] {});
		} catch (ExitException e) {
			Assert.assertEquals(1, e.getStatus());
		}
	}

	@Test
	public void 正常系_パラメーター日付不正() {
		fileDeleate(outputPath + "result_initial.csv");
		try {
			BatchApplication.main(new String[] { "dummy", outputPath, "result_initial.csv", "1" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals(1, e.getStatus());
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_パラメーターパス不正() {
		fileDeleate(outputPath + "result_initial.csv");
		try {
			BatchApplication.main(new String[] { "20191018", "dummy", "result_initial.csv", "1" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals(1, e.getStatus());
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_パラメーター種別不正() {
		fileDeleate(outputPath + "result_initial.csv");
		try {
			BatchApplication.main(new String[] { "20191018", outputPath, "result_initial.csv", "dummy" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals(1, e.getStatus());
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 異常系_パラメーター数不一致() {
		fileDeleate(outputPath + "result_initial.csv");
		try {
			BatchApplication.main(new String[] { "dummy", "dummy" });
			Assert.fail("パラメータ数不一致なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals(1, e.getStatus());
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	private List<ContractDetail> getContractDetailList() {
		List<ContractDetail> contractDetailList = new ArrayList<ContractDetail>();
		ContractDetail contractDetail = new ContractDetail();
		contractDetail.setId(1L);
		contractDetailList.add(contractDetail);
		return contractDetailList;
	}

	private Contract dummyContract() {
		Contract contract = new Contract();
		contract.setId(1L);
		contract.setContractDetailList(getContractDetailList());
		contract.setEstimationNumber("testEstimationNumber");
		ContractPicSaEmp contractPicSaEmp = new ContractPicSaEmp();
		contractPicSaEmp.setMailAddress("testSaEmp@example.com");
		contract.setContractPicSaEmp(contractPicSaEmp);
		ContractAddedEditorEmp contractAddedEditorEmp = new ContractAddedEditorEmp();
		contractAddedEditorEmp.setMailAddress("testAddedEditor@example.com");
		contract.setContractAddedEditorEmpList(Arrays.asList(contractAddedEditorEmp));
		ProductContract productContract = new ProductContract();
		productContract.setProductContractName("testProductContractName");
		contract.setProductContractList(Arrays.asList(productContract));
		CustomerContract customerContract = new CustomerContract();
		customerContract.setCompanyName("testCompanyName");
		contract.setCustomerContract(customerContract);
		return contract;
	}
}