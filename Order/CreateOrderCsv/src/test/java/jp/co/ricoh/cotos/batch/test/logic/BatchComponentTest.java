package jp.co.ricoh.cotos.batch.test.logic;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractAddedEditorEmp;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractDetail;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractPicSaEmp;
import jp.co.ricoh.cotos.commonlib.entity.contract.CustomerContract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ProductContract;
import jp.co.ricoh.cotos.commonlib.repository.arrangement.ArrangementPicWorkerEmpRepository;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractDetailRepository;
import jp.co.ricoh.cotos.component.RestApiClient;
import jp.co.ricoh.cotos.logic.BatchComponent;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchComponentTest extends TestBase {

	static ConfigurableApplicationContext context;

	final private String outputPath = "output/";
	@MockBean
	RestApiClient restApiClient;

	@Autowired
	BatchComponent batchComponent;

	@Autowired
	ContractDetailRepository contractDetailRepository;

	@Autowired
	ArrangementPicWorkerEmpRepository arrangementPicWorkerEmpRepository;

	@Autowired
	public void injectContext(ConfigurableApplicationContext injectContext) {
		context = injectContext;
		context.getBean(DBConfig.class).clearData();
	}

	private void テストデータ作成(String filePath) {
		context.getBean(DBConfig.class).clearData();
		context.getBean(DBConfig.class).initTargetTestData(filePath);
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
	public void 正常系_CSVファイルを出力できること() throws Exception {
		テストデータ作成("createOrderTestSuccessData.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		Mockito.doNothing().when(restApiClient).callContractApi(anyObject());
		try {
			batchComponent.execute(new String[] { "20191018", outputPath, "result_initial.csv", "1" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + "result_initial.csv"));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/initial.csv"));
		Assert.assertArrayEquals(expected, actuals);

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと() throws Exception {
		テストデータ作成("createOrderTestFailedData.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(null);
		Mockito.doNothing().when(restApiClient).callContractApi(anyObject());
		try {
			batchComponent.execute(new String[] { "20191018", outputPath, "result_initial.csv", "1" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_処理日が祝日() throws Exception {
		テストデータ作成("createOrderTestSuccessData.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		Mockito.doNothing().when(restApiClient).callContractApi(anyObject());
		try {
			batchComponent.execute(new String[] { "20191014", outputPath, "result_initial.csv", "1" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 引数無しで実行すると失敗すること() {
		try {
			batchComponent.execute(new String[] {});
			Assert.fail("引数無しで実行したのに異常終了しなかった");
		} catch (Exception e) {
		}
	}

	@Test
	public void 既存ファイルに上書きできないこと() throws Exception {
		テストデータ作成("createOrderTestSuccessData.sql");
		fileDeleate(outputPath + "duplicate.csv");
		if (!Files.exists(Paths.get("output/duplicate.csv"))) {
			Files.createFile(Paths.get("output/duplicate.csv"));
		}
		try {
			batchComponent.execute(new String[] { "20191018", outputPath, "duplicate.csv", "1" });
			Assert.fail("既存ファイルがあるのに異常終了しなかった");
		} catch (Exception e) {
			fileDeleate(outputPath + "duplicate.csv");
		}
		fileDeleate(outputPath + "duplicate.csv");
	}

	@Test
	public void パラメータ不正_存在しないディレクトリ() throws Exception {
		Files.deleteIfExists(Paths.get("output/dummy/result_initial.csv"));

		try {
			batchComponent.execute(new String[] { "20191018", outputPath + "dummy", "result_initial.csv", "1" });
			Assert.fail("CSVファイルが書き込めないのに異常終了しなかった");
		} catch (Exception e) {
		}
	}

	@Test
	public void パラメータ不正_処理年月日不正() throws Exception {
		try {
			batchComponent.execute(new String[] { "不正データ", outputPath, "result_initial.csv", "1" });
			Assert.fail("処理年月日のフォーマットが不正なのに異常終了しなかった");
		} catch (Exception e) {
		}

	}

	@Test
	public void 正常系_CSVファイルを出力できること_容量変更() throws Exception {
		テストデータ作成("createOrderTestSuccessDataCapacityChange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		Mockito.doNothing().when(restApiClient).callContractApi(anyObject());
		try {
			batchComponent.execute(new String[] { "20190926", outputPath, "result_initial.csv", "2" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + "result_initial.csv"));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/initial_capacity_change.csv"));
		Assert.assertArrayEquals(expected, actuals);

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_容量変更() throws Exception {
		テストデータ作成("createOrderTestFailedDataCapacityChange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(null);
		Mockito.doNothing().when(restApiClient).callContractApi(anyObject());
		try {
			batchComponent.execute(new String[] { "20190926", outputPath, "result_initial.csv", "2" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_処理日が処理年月日末営業日2営業日前でない_容量変更() throws Exception {
		テストデータ作成("createOrderTestSuccessDataCapacityChange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		Mockito.doNothing().when(restApiClient).callContractApi(anyObject());
		try {
			batchComponent.execute(new String[] { "20190927", outputPath, "result_initial.csv", "2" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力できること_有償交換() throws Exception {
		テストデータ作成("createOrderTestSuccessDataPaidExchange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		Mockito.doNothing().when(restApiClient).callContractApi(anyObject());
		try {
			batchComponent.execute(new String[] { "20191028", outputPath, "result_initial.csv", "3" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + "result_initial.csv"));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/initial_paid_exchange.csv"));
		Assert.assertArrayEquals(expected, actuals);

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_有償交換() throws Exception {
		テストデータ作成("createOrderTestFailedDataPaidExchange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(null);
		Mockito.doNothing().when(restApiClient).callContractApi(anyObject());
		try {
			batchComponent.execute(new String[] { "20191028", outputPath, "result_initial.csv", "3" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_処理日が祝日_有償交換() throws Exception {
		テストデータ作成("createOrderTestSuccessDataPaidExchange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		Mockito.doNothing().when(restApiClient).callContractApi(anyObject());
		try {
			batchComponent.execute(new String[] { "20191022", outputPath, "result_initial.csv", "3" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
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