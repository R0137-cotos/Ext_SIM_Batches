package jp.co.ricoh.cotos.batch.test.logic;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;

import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.batch.test.TestExitHandler;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract.ContractType;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractAddedEditorEmp;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractDetail;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractInstallationLocation;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractPicSaEmp;
import jp.co.ricoh.cotos.commonlib.entity.contract.CustomerContract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ProductContract;
import jp.co.ricoh.cotos.commonlib.logic.businessday.BusinessDayUtil;
import jp.co.ricoh.cotos.commonlib.security.CotosAuthenticationDetails;
import jp.co.ricoh.cotos.commonlib.util.BatchMomInfoProperties;
import jp.co.ricoh.cotos.component.BatchUtil;
import jp.co.ricoh.cotos.component.RestApiClient;
import jp.co.ricoh.cotos.logic.JobComponent;
import jp.co.ricoh.cotos.security.CreateJwt;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JobComponentTest extends TestBase {

	static ConfigurableApplicationContext context;

	final private String outputPath = "output/";

	@MockBean
	RestApiClient restApiClient;

	@Autowired
	JobComponent jobComponent;

	@Autowired
	BatchMomInfoProperties batchProperty;

	@Autowired
	CreateJwt createJwt;

	@SpyBean
	BusinessDayUtil businessDayUtil;

	@SpyBean
	BatchUtil batchUtil;

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
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract("新規"));
		Mockito.doNothing().when(restApiClient).callContractApi(any());
		Mockito.doReturn(ContractInstallationLocationMock("2", "無")).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20191018", outputPath, "result_initial.csv", "1" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}
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
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(null);
		Mockito.doNothing().when(restApiClient).callContractApi(any());
		Mockito.doReturn(ContractInstallationLocationMock("2", "無")).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20191018", outputPath, "result_initial.csv", "1" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		Assert.assertFalse("オーダーCSVが出力されていないこと。", Files.exists(Paths.get("output/result_initial.csv")));
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_処理日が祝日() throws IOException {
		テストデータ作成("createOrderTestSuccessData.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract("新規"));
		Mockito.doNothing().when(restApiClient).callContractApi(any());
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20191014", outputPath, "result_initial.csv", "1" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		Assert.assertFalse("オーダーCSVが出力されていないこと。", Files.exists(Paths.get("output/result_initial.csv")));
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_契約変更_容量変更() throws IOException {
		テストデータ作成("createOrderTestSuccessDataCapacityChange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract("容量変更"));
		Mockito.doNothing().when(restApiClient).callContractApi(any());
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20191018", outputPath, "result_initial.csv", "1" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		Assert.assertFalse("オーダーCSVが出力されていないこと。", Files.exists(Paths.get("output/result_initial.csv")));
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_契約変更_有償交換() throws IOException {
		テストデータ作成("createOrderTestSuccessDataPaidExchange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract("有償交換"));
		Mockito.doNothing().when(restApiClient).callContractApi(any());
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20191018", outputPath, "result_initial.csv", "1" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		Assert.assertFalse("オーダーCSVが出力されていないこと。", Files.exists(Paths.get("output/result_initial.csv")));
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 引数無しで実行すると失敗すること() {
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] {});
			Assert.fail("引数無しで実行したのに異常終了しなかった");
		} catch (ExitException e) {
		}
	}

	@Test
	public void 既存ファイルに上書きできないこと() throws IOException {
		テストデータ作成("createOrderTestSuccessData.sql");
		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract("新規"));
		Mockito.doNothing().when(restApiClient).callContractApi(any());
		fileDeleate(outputPath + "duplicate.csv");
		if (!Files.exists(Paths.get("output/duplicate.csv"))) {
			Files.createFile(Paths.get("output/duplicate.csv"));
		}
		try {
			JobComponent.setExitHandler(new TestExitHandler());
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
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20191018", outputPath + "dummy", "result_initial.csv", "1" });
			Assert.fail("CSVファイルが書き込めないのに異常終了しなかった");
		} catch (ExitException e) {
		}
	}

	@Test
	public void パラメータ不正_処理年月日不正() throws IOException {
		try {
			JobComponent.setExitHandler(new TestExitHandler());
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
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract("容量変更"));
		Mockito.doNothing().when(restApiClient).callContractApi(any());
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20190920", outputPath, "result_initial.csv", "2" });
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("テスト失敗");
		}

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
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract("容量変更"));
		Mockito.doNothing().when(restApiClient).callContractApi(any());
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20190920", outputPath, "result_initial.csv", "2" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		Assert.assertFalse("オーダーCSVが出力されていないこと。", Files.exists(Paths.get("output/result_initial.csv")));
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 異常系_CSVファイルを出力しないこと_容量変更_月末営業日マイナス5営業日以外() {
		// 2019年6月の非営業日は以下を想定
		// 2019/06/01
		// 2019/06/02
		// 2019/06/08
		// 2019/06/09
		// 2019/06/15
		// 2019/06/16
		// 2019/06/22
		// 2019/06/23
		// 2019/06/29
		// 2019/06/30

		// 2019/06/28 月末営業日
		// 2019/06/21 月末営業日-5日　要処理日付

		// 処理不要日付　営業日 月末営業日-5日以降 2019/06/24
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20190624", outputPath, "result_initial.csv", "2" });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が2であること", 2, e.getStatus());
		}

		// 処理不要日付　営業日 月末営業日-5日以前 2019/06/20
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20190620", outputPath, "result_initial.csv", "2" });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が2であること", 2, e.getStatus());
		}

		// 処理不要日付　非営業日 月末営業日-5日以降 2019/06/22
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20190622", outputPath, "result_initial.csv", "2" });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が2であること", 2, e.getStatus());
		}

		// 処理不要日付　非営業日 月末営業日-5日以前 2019/06/16
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20190616", outputPath, "result_initial.csv", "2" });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が2であること", 2, e.getStatus());
		}
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_容量変更_新規() throws IOException {
		テストデータ作成("createOrderTestSuccessData.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract("新規"));
		Mockito.doNothing().when(restApiClient).callContractApi(any());
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20190920", outputPath, "result_initial.csv", "2" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		Assert.assertFalse("オーダーCSVが出力されていないこと。", Files.exists(Paths.get("output/result_initial.csv")));
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力できること_有償交換() throws IOException {
		テストデータ作成("createOrderTestSuccessDataPaidExchange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract("有償交換"));
		Mockito.doNothing().when(restApiClient).callContractApi(any());
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20191028", outputPath, "result_initial.csv", "3" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

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
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract("有償交換"));
		Mockito.doNothing().when(restApiClient).callContractApi(any());
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20191028", outputPath, "result_initial.csv", "3" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		Assert.assertFalse("オーダーCSVが出力されていないこと。", Files.exists(Paths.get("output/result_initial.csv")));
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_処理日が祝日_有償交換() throws IOException {
		テストデータ作成("createOrderTestSuccessDataPaidExchange.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.doNothing().when(restApiClient).callContractApi(any());
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20191022", outputPath, "result_initial.csv", "3" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		Assert.assertFalse("オーダーCSVが出力されていないこと。", Files.exists(Paths.get("output/result_initial.csv")));
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_CSVファイルを出力しないこと_有償交換_新規() throws IOException {
		テストデータ作成("createOrderTestSuccessData.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract("新規"));
		Mockito.doNothing().when(restApiClient).callContractApi(any());
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20191028", outputPath, "result_initial.csv", "3" });
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}

		Assert.assertFalse("オーダーCSVが出力されていないこと。", Files.exists(Paths.get("output/result_initial.csv")));
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 異常系_CSVファイルを出力できること_APIエラー_callAssignWorker() throws IOException {
		テストデータ作成("createOrderTestSuccessData.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		doThrow(new RestClientException("何らかの失敗")).when(restApiClient).callAssignWorker(anyList());
		doThrow(new RestClientException("何らかの失敗")).when(restApiClient).callAcceptWorkApi(anyList());
		doThrow(new RestClientException("何らかの失敗")).when(restApiClient).callFindOneContractApi(anyLong());
		doThrow(new RestClientException("何らかの失敗")).when(restApiClient).callContractApi(any());
		Mockito.doReturn(ContractInstallationLocationMock("2", "無")).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20191018", outputPath, "result_initial.csv", "1" });
		} catch (ExitException e) {
			Assert.assertEquals("意図した通りExceptionが発生した", e.getStatus(), 3);
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 異常系_CSVファイルを出力できること_APIエラー_callFindOneContractApi() throws IOException {
		テストデータ作成("createOrderTestSuccessData.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract("新規"));
		doThrow(new RestClientException("何らかの失敗")).when(restApiClient).callContractApi(any());
		Mockito.doReturn(ContractInstallationLocationMock("2", "無")).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20191018", outputPath, "result_initial.csv", "1" });
		} catch (ExitException e) {
			Assert.assertEquals("意図した通りExceptionが発生した", e.getStatus(), 3);
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 異常系_CSVファイルを出力できること_APIエラー_callContractApi() throws IOException {
		テストデータ作成("createOrderTestSuccessData.sql");
		fileDeleate(outputPath + "result_initial.csv");

		// モック
		Mockito.doNothing().when(restApiClient).callAssignWorker(anyList());
		Mockito.doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		doThrow(new RestClientException("何らかの失敗")).when(restApiClient).callFindOneContractApi(anyLong());
		doThrow(new RestClientException("何らかの失敗")).when(restApiClient).callContractApi(any());
		Mockito.doReturn(ContractInstallationLocationMock("2", "無")).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20191018", outputPath, "result_initial.csv", "1" });
		} catch (ExitException e) {
			Assert.assertEquals("意図した通りExceptionが発生した", e.getStatus(), 3);
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 異常系_意図しないエラー発生_Exception() throws FileAlreadyExistsException {
		Mockito.doThrow(new RuntimeException()).when(businessDayUtil).getLastBusinessDayOfTheMonthFromNonBusinessCalendarMaster(Mockito.any());
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20191018", outputPath, "result_initial.csv", "2" });
		} catch (ExitException e) {
			Assert.assertEquals("意図した通りExceptionが発生した", e.getStatus(), 1);
		} catch (Exception e) {
			Assert.fail("テスト失敗");
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 異常系_意図しないエラー発生_Throwable() {
		Mockito.doThrow(new ThreadDeath()).when(businessDayUtil).getLastBusinessDayOfTheMonthFromNonBusinessCalendarMaster(Mockito.any());
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			jobComponent.run(new String[] { "20191018", outputPath, "result_initial.csv", "2" });
		} catch (ExitException e) {
			Assert.assertEquals("意図した通りThrowableが発生した", e.getStatus(), 1);
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

	private Contract dummyContract(String type) {
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
		if ("新規".equals(type)) {
			contract.setContractType(ContractType.新規);
		} else if ("容量変更".equals(type) || "有償交換".equals(type)) {
			contract.setContractType(ContractType.契約変更);
		}
		return contract;
	}

	/**
	 * 設置先（契約用）モック
	 * @param wantSagawaCodeColumnF
	 * @param isInputPostNumber
	 * @return
	 */
	private ContractInstallationLocation ContractInstallationLocationMock(String wantSagawaCodeColumnF, String isInputPostNumber) {
		ContractInstallationLocation contractInstallationLocation = new ContractInstallationLocation();
		contractInstallationLocation.setId(1);

		// 郵便番号をセット
		// 9633602 1
		// 6890535 2
		// 9020067 3日以上
		// 1000104 離島は問合せ
		// 9750000 不能
		switch (wantSagawaCodeColumnF) {
		case "1":
			contractInstallationLocation.setPostNumber("9633602");
			break;
		case "2":
			contractInstallationLocation.setPostNumber("6890535");
			break;
		case "3日以上":
			contractInstallationLocation.setPostNumber("9020067");
			break;
		case "離島は問合せ":
			contractInstallationLocation.setPostNumber("1000104");
			break;
		case "不能":
			contractInstallationLocation.setPostNumber("9750000");
			break;
		case "失敗":
			contractInstallationLocation.setPostNumber("0000000000000");
			break;
		case "無":
			break;
		}
		// isInputPostNumberがtrueの時郵便番号(手入力)に郵便番号をセット
		// 0070834 2
		switch (isInputPostNumber) {
		case "無":
			break;
		case "2":
			contractInstallationLocation.setInputPostNumber("007-0834");
			break;
		case "失敗":
			contractInstallationLocation.setInputPostNumber("0000000000000");
			break;
		default:
		}

		return contractInstallationLocation;
	}
}
