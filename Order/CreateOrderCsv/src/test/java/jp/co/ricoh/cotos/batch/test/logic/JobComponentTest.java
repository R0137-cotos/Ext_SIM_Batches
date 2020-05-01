package jp.co.ricoh.cotos.batch.test.logic;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.entity.arrangement.ArrangementPicWorkerEmp;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractDetail;
import jp.co.ricoh.cotos.commonlib.repository.arrangement.ArrangementPicWorkerEmpRepository;
import jp.co.ricoh.cotos.commonlib.repository.arrangement.ArrangementWorkRepository;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractDetailRepository;
import jp.co.ricoh.cotos.commonlib.security.CotosAuthenticationDetails;
import jp.co.ricoh.cotos.commonlib.util.BatchMomInfoProperties;
import jp.co.ricoh.cotos.component.RestApiClient;
import jp.co.ricoh.cotos.logic.JobComponent;
import jp.co.ricoh.cotos.security.CreateJwt;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JobComponentTest extends TestBase {

	static ConfigurableApplicationContext context;

	final private String outputPath = "output/";

	final private String successExtendsParameter = "{\"orderCsvCreationStatus\":\"1\",\"orderCsvCreationDate\":\"20191018\"}";

	final private String successExtendsParameterCapacityChange = "{\"orderCsvCreationStatus\":\"1\",\"orderCsvCreationDate\":\"20190926\"}";

	final private String successExtendsParameterPaidExchange = "{\"orderCsvCreationStatus\":\"1\",\"orderCsvCreationDate\":\"20191028\"}";

	final private String dummySuccessExtendsParameter = "{\"orderCsvCreationStatus\":\"1\",\"orderCsvCreationDate\":\"\"}";

	final private String extendsParameter = "{\"orderCsvCreationStatus\":\"0\",\"orderCsvCreationDate\":\"\"}";

	@MockBean
	RestApiClient restApiClient;

	@Autowired
	JobComponent jobComponent;

	@Autowired
	BatchMomInfoProperties batchProperty;

	@Autowired
	ContractDetailRepository contractDetailRepository;

	@Autowired
	ArrangementWorkRepository arrangementWorkRepository;

	@Autowired
	ArrangementPicWorkerEmpRepository arrangementPicWorkerEmpRepository;

	@Autowired
	CreateJwt createJwt;

	@Autowired
	public void injectContext(ConfigurableApplicationContext injectContext) {
		String jwt = createJwt.execute();
		CotosAuthenticationDetails principal = new CotosAuthenticationDetails(batchProperty.getMomEmpId(), "sid", null, null, jwt, true, true, null);
		Authentication auth = new PreAuthenticatedAuthenticationToken(principal, null, null);
		SecurityContextHolder.getContext().setAuthentication(auth);
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
	public void 正常系_CSVファイルを出力できること() throws IOException {
		テストデータ作成("createOrderTestSuccessData.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		doNothing().when(restApiClient).callContractApi(anyObject());

		jobComponent.run(new String[] { "20191018", outputPath, "result_initial.csv", "1" });

		ArrangementPicWorkerEmp arrangementPicWorkerEmp4 = arrangementPicWorkerEmpRepository.findOne(4L);
		ContractDetail contractDetail11 = contractDetailRepository.findOne(11L);
		ContractDetail contractDetail12 = contractDetailRepository.findOne(12L);
		ContractDetail contractDetail21 = contractDetailRepository.findOne(21L);
		ContractDetail contractDetail22 = contractDetailRepository.findOne(22L);
		ContractDetail contractDetail31 = contractDetailRepository.findOne(31L);
		ContractDetail contractDetail32 = contractDetailRepository.findOne(32L);
		ContractDetail contractDetail41 = contractDetailRepository.findOne(11L);
		ContractDetail contractDetail42 = contractDetailRepository.findOne(12L);

		Assert.assertEquals("更新者が変更されていないこと", "00229692", arrangementPicWorkerEmp4.getMomEmployeeId());

		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameter, contractDetail11.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameter, contractDetail12.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameter, contractDetail21.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameter, contractDetail22.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameter, contractDetail31.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameter, contractDetail32.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameter, contractDetail41.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameter, contractDetail42.getExtendsParameter());

		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + "result_initial.csv"));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/initial.csv"));
		Assert.assertArrayEquals(expected, actuals);

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと() throws IOException {
		テストデータ作成("createOrderTestFailedData.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		doNothing().when(restApiClient).callContractApi(anyObject());

		jobComponent.run(new String[] { "20191018", outputPath, "result_initial.csv", "1" });

		ContractDetail contractDetail31 = contractDetailRepository.findOne(31L);
		ContractDetail contractDetail32 = contractDetailRepository.findOne(32L);
		ContractDetail contractDetail41 = contractDetailRepository.findOne(41L);
		ContractDetail contractDetail42 = contractDetailRepository.findOne(42L);
		ContractDetail contractDetail51 = contractDetailRepository.findOne(51L);
		ContractDetail contractDetail52 = contractDetailRepository.findOne(52L);
		ContractDetail contractDetail61 = contractDetailRepository.findOne(61L);
		ContractDetail contractDetail62 = contractDetailRepository.findOne(62L);
		ContractDetail contractDetail71 = contractDetailRepository.findOne(71L);
		ContractDetail contractDetail72 = contractDetailRepository.findOne(72L);
		ContractDetail contractDetail81 = contractDetailRepository.findOne(81L);
		ContractDetail contractDetail82 = contractDetailRepository.findOne(82L);

		Assert.assertEquals("拡張項目が変更されていないこと", dummySuccessExtendsParameter, contractDetail31.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", dummySuccessExtendsParameter, contractDetail32.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail41.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail42.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail51.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail52.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail61.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail62.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail71.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail72.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail81.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail82.getExtendsParameter());

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_処理日が祝日() throws IOException {
		テストデータ作成("createOrderTestSuccessData.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		doNothing().when(restApiClient).callContractApi(anyObject());

		jobComponent.run(new String[] { "20191014", outputPath, "result_initial.csv", "1" });

		ContractDetail contractDetail11 = contractDetailRepository.findOne(11L);
		ContractDetail contractDetail12 = contractDetailRepository.findOne(12L);
		ContractDetail contractDetail21 = contractDetailRepository.findOne(21L);
		ContractDetail contractDetail22 = contractDetailRepository.findOne(22L);
		ContractDetail contractDetail31 = contractDetailRepository.findOne(31L);
		ContractDetail contractDetail32 = contractDetailRepository.findOne(32L);
		ContractDetail contractDetail41 = contractDetailRepository.findOne(11L);
		ContractDetail contractDetail42 = contractDetailRepository.findOne(12L);

		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail11.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail12.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail21.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail22.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail31.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail32.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail41.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail42.getExtendsParameter());

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 引数無しで実行すると失敗すること() {
		try {
			jobComponent.run(new String[] {});
			Assert.fail("引数無しで実行したのに異常終了しなかった");
		} catch (ExitException e) {
		}
	}

	@Test
	public void 既存ファイルに上書きできないこと() throws IOException {
		テストデータ作成("createOrderTestSuccessData.sql");
		fileDeleate(outputPath + "duplicate.csv");
		if (!Files.exists(Paths.get("output/duplicate.csv"))) {
			Files.createFile(Paths.get("output/duplicate.csv"));
		}
		try {
			jobComponent.run(new String[] { "20191018", outputPath, "duplicate.csv", "1" });
			Assert.fail("既存ファイルがあるのに異常終了しなかった");
		} catch (ExitException e) {
			fileDeleate(outputPath + "duplicate.csv");
		}
		fileDeleate(outputPath + "duplicate.csv");
	}

	@Test
	public void パラメータ不正_存在しないディレクトリ() throws IOException {
		Files.deleteIfExists(Paths.get("output/dummy/result_initial.csv"));

		try {
			jobComponent.run(new String[] { "20191018", outputPath + "dummy", "result_initial.csv", "1" });
			Assert.fail("CSVファイルが書き込めないのに異常終了しなかった");
		} catch (ExitException e) {
		}
	}

	@Test
	public void パラメータ不正_処理年月日不正() throws IOException {
		try {
			jobComponent.run(new String[] { "不正データ", outputPath, "result_initial.csv", "1" });
			Assert.fail("処理年月日のフォーマットが不正なのに異常終了しなかった");
		} catch (ExitException e) {
		}

	}

	@Test
	public void 正常系_CSVファイルを出力できること_容量変更() throws IOException {
		テストデータ作成("createOrderTestSuccessDataCapacityChange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		doNothing().when(restApiClient).callContractApi(anyObject());

		jobComponent.run(new String[] { "20190926", outputPath, "result_initial.csv", "2" });

		ArrangementPicWorkerEmp arrangementPicWorkerEmp4 = arrangementPicWorkerEmpRepository.findOne(4L);
		ContractDetail contractDetail11 = contractDetailRepository.findOne(11L);
		ContractDetail contractDetail12 = contractDetailRepository.findOne(12L);
		ContractDetail contractDetail21 = contractDetailRepository.findOne(21L);
		ContractDetail contractDetail22 = contractDetailRepository.findOne(22L);
		ContractDetail contractDetail31 = contractDetailRepository.findOne(31L);
		ContractDetail contractDetail32 = contractDetailRepository.findOne(32L);
		ContractDetail contractDetail41 = contractDetailRepository.findOne(11L);
		ContractDetail contractDetail42 = contractDetailRepository.findOne(12L);

		Assert.assertEquals("更新者が変更されていないこと", "00229692", arrangementPicWorkerEmp4.getMomEmployeeId());

		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterCapacityChange, contractDetail11.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterCapacityChange, contractDetail12.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterCapacityChange, contractDetail21.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterCapacityChange, contractDetail22.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterCapacityChange, contractDetail31.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterCapacityChange, contractDetail32.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterCapacityChange, contractDetail41.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterCapacityChange, contractDetail42.getExtendsParameter());

		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + "result_initial.csv"));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/initial_capacity_change.csv"));
		Assert.assertArrayEquals(expected, actuals);

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_容量変更() throws IOException {
		テストデータ作成("createOrderTestFailedDataCapacityChange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		doNothing().when(restApiClient).callContractApi(anyObject());

		jobComponent.run(new String[] { "20190926", outputPath, "result_initial.csv", "2" });

		ContractDetail contractDetail31 = contractDetailRepository.findOne(31L);
		ContractDetail contractDetail32 = contractDetailRepository.findOne(32L);
		ContractDetail contractDetail41 = contractDetailRepository.findOne(41L);
		ContractDetail contractDetail42 = contractDetailRepository.findOne(42L);
		ContractDetail contractDetail51 = contractDetailRepository.findOne(51L);
		ContractDetail contractDetail52 = contractDetailRepository.findOne(52L);
		ContractDetail contractDetail61 = contractDetailRepository.findOne(61L);
		ContractDetail contractDetail62 = contractDetailRepository.findOne(62L);
		ContractDetail contractDetail71 = contractDetailRepository.findOne(71L);
		ContractDetail contractDetail72 = contractDetailRepository.findOne(72L);
		ContractDetail contractDetail81 = contractDetailRepository.findOne(81L);
		ContractDetail contractDetail82 = contractDetailRepository.findOne(82L);
		ContractDetail contractDetail91 = contractDetailRepository.findOne(91L);
		ContractDetail contractDetail92 = contractDetailRepository.findOne(92L);

		Assert.assertEquals("拡張項目が変更されていないこと", dummySuccessExtendsParameter, contractDetail31.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", dummySuccessExtendsParameter, contractDetail32.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail41.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail42.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail51.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail52.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail61.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail62.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail71.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail72.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail81.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail82.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail91.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail92.getExtendsParameter());

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_処理日が処理年月日末営業日2営業日前でない_容量変更() throws IOException {
		テストデータ作成("createOrderTestSuccessDataCapacityChange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		doNothing().when(restApiClient).callContractApi(anyObject());

		jobComponent.run(new String[] { "20190927", outputPath, "result_initial.csv", "2" });

		ContractDetail contractDetail11 = contractDetailRepository.findOne(11L);
		ContractDetail contractDetail12 = contractDetailRepository.findOne(12L);
		ContractDetail contractDetail21 = contractDetailRepository.findOne(21L);
		ContractDetail contractDetail22 = contractDetailRepository.findOne(22L);
		ContractDetail contractDetail31 = contractDetailRepository.findOne(31L);
		ContractDetail contractDetail32 = contractDetailRepository.findOne(32L);
		ContractDetail contractDetail41 = contractDetailRepository.findOne(11L);
		ContractDetail contractDetail42 = contractDetailRepository.findOne(12L);

		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail11.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail12.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail21.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail22.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail31.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail32.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail41.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail42.getExtendsParameter());

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力できること_有償交換() throws IOException {
		テストデータ作成("createOrderTestSuccessDataPaidExchange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		doNothing().when(restApiClient).callContractApi(anyObject());

		jobComponent.run(new String[] { "20191028", outputPath, "result_initial.csv", "3" });

		ArrangementPicWorkerEmp arrangementPicWorkerEmp4 = arrangementPicWorkerEmpRepository.findOne(4L);
		ContractDetail contractDetail11 = contractDetailRepository.findOne(11L);
		ContractDetail contractDetail12 = contractDetailRepository.findOne(12L);
		ContractDetail contractDetail21 = contractDetailRepository.findOne(21L);
		ContractDetail contractDetail22 = contractDetailRepository.findOne(22L);
		ContractDetail contractDetail31 = contractDetailRepository.findOne(31L);
		ContractDetail contractDetail32 = contractDetailRepository.findOne(32L);
		ContractDetail contractDetail41 = contractDetailRepository.findOne(11L);
		ContractDetail contractDetail42 = contractDetailRepository.findOne(12L);

		Assert.assertEquals("更新者が変更されていないこと", "00229692", arrangementPicWorkerEmp4.getMomEmployeeId());

		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterPaidExchange, contractDetail11.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterPaidExchange, contractDetail12.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterPaidExchange, contractDetail21.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterPaidExchange, contractDetail22.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterPaidExchange, contractDetail31.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterPaidExchange, contractDetail32.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterPaidExchange, contractDetail41.getExtendsParameter());
		Assert.assertEquals("拡張項目が設定されていること", successExtendsParameterPaidExchange, contractDetail42.getExtendsParameter());

		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + "result_initial.csv"));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/initial_paid_exchange.csv"));
		Assert.assertArrayEquals(expected, actuals);

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_有償交換() throws IOException {
		テストデータ作成("createOrderTestFailedDataPaidExchange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		doNothing().when(restApiClient).callContractApi(anyObject());

		jobComponent.run(new String[] { "20191028", outputPath, "result_initial.csv", "3" });

		ContractDetail contractDetail31 = contractDetailRepository.findOne(31L);
		ContractDetail contractDetail32 = contractDetailRepository.findOne(32L);
		ContractDetail contractDetail41 = contractDetailRepository.findOne(41L);
		ContractDetail contractDetail42 = contractDetailRepository.findOne(42L);
		ContractDetail contractDetail51 = contractDetailRepository.findOne(51L);
		ContractDetail contractDetail52 = contractDetailRepository.findOne(52L);
		ContractDetail contractDetail61 = contractDetailRepository.findOne(61L);
		ContractDetail contractDetail62 = contractDetailRepository.findOne(62L);
		ContractDetail contractDetail71 = contractDetailRepository.findOne(71L);
		ContractDetail contractDetail72 = contractDetailRepository.findOne(72L);
		ContractDetail contractDetail81 = contractDetailRepository.findOne(81L);
		ContractDetail contractDetail82 = contractDetailRepository.findOne(82L);

		Assert.assertEquals("拡張項目が変更されていないこと", dummySuccessExtendsParameter, contractDetail31.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", dummySuccessExtendsParameter, contractDetail32.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail41.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail42.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail51.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail52.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail61.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail62.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail71.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail72.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail81.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail82.getExtendsParameter());

		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_処理日が祝日_有償交換() throws IOException {
		テストデータ作成("createOrderTestSuccessDataPaidExchange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		doNothing().when(restApiClient).callContractApi(anyObject());

		jobComponent.run(new String[] { "20191022", outputPath, "result_initial.csv", "3" });

		ContractDetail contractDetail11 = contractDetailRepository.findOne(11L);
		ContractDetail contractDetail12 = contractDetailRepository.findOne(12L);
		ContractDetail contractDetail21 = contractDetailRepository.findOne(21L);
		ContractDetail contractDetail22 = contractDetailRepository.findOne(22L);
		ContractDetail contractDetail31 = contractDetailRepository.findOne(31L);
		ContractDetail contractDetail32 = contractDetailRepository.findOne(32L);
		ContractDetail contractDetail41 = contractDetailRepository.findOne(11L);
		ContractDetail contractDetail42 = contractDetailRepository.findOne(12L);

		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail11.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail12.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail21.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail22.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail31.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail32.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail41.getExtendsParameter());
		Assert.assertEquals("拡張項目が変更されていないこと", extendsParameter, contractDetail42.getExtendsParameter());

		fileDeleate(outputPath + "result_initial.csv");
	}
}
