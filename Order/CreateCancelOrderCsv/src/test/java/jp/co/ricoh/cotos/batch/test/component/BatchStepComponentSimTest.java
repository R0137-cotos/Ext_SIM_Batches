package jp.co.ricoh.cotos.batch.test.component;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.CancelOrderEntity;
import jp.co.ricoh.cotos.dto.CreateOrderCsvParameter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchStepComponentSimTest extends TestBase {

	static ConfigurableApplicationContext context;

	@SpyBean(name = "SIM")
	BatchStepComponent batchStepComponent;

	@Autowired
	public void injectContext(ConfigurableApplicationContext injectContext) {
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

	@Test
	public void パラメータチェック_正常系() throws IOException {
		try {
			batchStepComponent.paramCheck(new String[] { "20190626", filePath, fileName });
		} catch (ErrorCheckException e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void パラメータチェック_異常系_パラメータ数不一致() throws Exception {
		try {
			// パラメータ無し
			batchStepComponent.paramCheck(new String[] {});
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00001", messageInfo.get(0).getErrorId());
			Assert.assertEquals("パラメータ「処理年月日/ディレクトリ名/作成ファイル名」が設定されていません。", messageInfo.get(0).getErrorMessage());
		}

		try {
			// パラメータ1つ
			batchStepComponent.paramCheck(new String[] { "20190626" });
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00001", messageInfo.get(0).getErrorId());
			Assert.assertEquals("パラメータ「処理年月日/ディレクトリ名/作成ファイル名」が設定されていません。", messageInfo.get(0).getErrorMessage());
		}

		try {
			// パラメータ2つ
			batchStepComponent.paramCheck(new String[] { "20190626", "output" });
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00001", messageInfo.get(0).getErrorId());
			Assert.assertEquals("パラメータ「処理年月日/ディレクトリ名/作成ファイル名」が設定されていません。", messageInfo.get(0).getErrorMessage());
		}

		try {
			// パラメータ4つ
			batchStepComponent.paramCheck(new String[] { "20190626", "output", "test.csv", "dummy" });
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00001", messageInfo.get(0).getErrorId());
			Assert.assertEquals("パラメータ「処理年月日/ディレクトリ名/作成ファイル名」が設定されていません。", messageInfo.get(0).getErrorMessage());
		}
	}

	@Test
	public void パラメータチェック_異常系_日付変換失敗() throws Exception {
		try {
			batchStepComponent.paramCheck(new String[] { "2019/06/26", "output", "test.csv" });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("RBA00001", messageInfo.get(0).getErrorId());
			Assert.assertEquals("業務日付のフォーマットはyyyyMMddです。", messageInfo.get(0).getErrorMessage());
		}
	}

	@Test
	public void パラメータチェック_異常系_ファイルが既に存在() throws Exception {
		// ファイルを事前に作成する
		if (!csvFile.exists()) {
			csvFile.createNewFile();
		}

		try {
			batchStepComponent.paramCheck(new String[] { "20190626", filePath, fileName });
			Assert.fail("ファイルが存在する状態で処理が実行された。");
		} catch (FileAlreadyExistsException e) {
			// FileAlreadyExistsExceptionが発生していること
			Assert.assertNotEquals(null, e);
		}
	}

	@Test
	public void パラメータチェック_異常系_一時ファイルが既に存在() throws Exception {
		// 一時ファイルを事前に作成する
		if (!tmpFile.exists()) {
			tmpFile.createNewFile();
		}

		try {
			batchStepComponent.paramCheck(new String[] { "20190626", filePath, fileName });
			Assert.fail("ファイルが存在する状態で処理が実行された。");
		} catch (FileAlreadyExistsException e) {
			// FileAlreadyExistsExceptionが発生していること
			Assert.assertNotEquals(null, e);
		} finally {
			// 作成した一時ファイルを削除
			Files.deleteIfExists(tmpFile.toPath());
		}
	}

	@Test
	public void パラメータチェック_異常系_ディレクトリが存在しない() throws Exception {
		// 出力ファイルパス　※テスト環境に存在しないこと
		String filePath = "hoge12345678999";

		try {
			batchStepComponent.paramCheck(new String[] { "20190626", filePath, fileName });
			Assert.fail("ディレクトリが存在しない状態で処理が実行された。");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00110", messageInfo.get(0).getErrorId());
			Assert.assertEquals("指定されたディレクトリが存在しません。", messageInfo.get(0).getErrorMessage());
		}
	}

	@Test
	public void 解約オーダー取得テスト_正常系() throws IOException {
		// テストデータ投入
		context.getBean(DBConfig.class).initTargetTestData("createCancelOrderSuccessTestData.sql");
		try {
			List<CancelOrderEntity> cancelOrderEntityList = batchStepComponent.getDataList();
			// 一件以上取得できていること
			Assert.assertNotEquals(0, cancelOrderEntityList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void 解約オーダー取得テスト_正常系_合計数量16を超える() throws IOException {
		// テストデータ投入
		context.getBean(DBConfig.class).initTargetTestData("createCancelOrderOver16Data.sql");
		try {
			List<CancelOrderEntity> cancelOrderEntityList = batchStepComponent.getDataList();
			// 8件取得できていること(契約ID=140の2GB,5GB,10GB,20GB 契約ID=150の2GB,5GB,10GB,20GB)
			Assert.assertNotEquals(0, cancelOrderEntityList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void 解約オーダー取得テスト_正常系_取得データ0件() throws IOException {
		// テストデータを投入しない
		try {
			List<CancelOrderEntity> cancelOrderEntityList = batchStepComponent.getDataList();
			// 一件も取得されていないこと
			Assert.assertEquals(0, cancelOrderEntityList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void 解約手配CSV作成処理_正常系() throws IOException {
		// テストデータ作成
		CreateOrderCsvParameter param = new CreateOrderCsvParameter();

		// パラメータセット
		param.setCsvFile(csvFile);
		param.setTmpFile(tmpFile);
		param.setOperationDate(LocalDate.of(2019, 6, 26));

		// 解約データ作成
		List<CancelOrderEntity> cancelOrderEntityList = new ArrayList<CancelOrderEntity>();
		CancelOrderEntity entity = new CancelOrderEntity();
		entity.setId(1);
		entity.setApplicationDate(LocalDate.of(2020, 4, 14));
		entity.setCancelApplicationDate(LocalDate.of(2019, 6, 25));
		entity.setCancelScheduledDate(LocalDate.of(2019, 6, 30));
		entity.setConclusionPreferredDate(LocalDate.of(2019, 6, 30));
		entity.setContractIdTemp(140);
		entity.setContractNumber("CC2020041400140");
		entity.setContractBranchNumber(1);
		entity.setContractType("1");
		StringBuilder sb = new StringBuilder();
		sb.append("{\"extendsParameterList\":[");
		// 拡張項目繰返.種別 = 新規
		sb.append("{\"id\":1,\"contractType\":\"新規\",\"productCode\":\"915868\",\"productName\":\"リコーモバイル通信サービス　データＳＩＭ（Ｓ）２ＧＢ　月額利用料\",\"lineNumber\":\"11111\",\"serialNumber\":\"1\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"11\"},");
		// 拡張項目繰返.種別 = 解約
		sb.append("{\"id\":1,\"contractType\":\"解約\",\"productCode\":\"000000\",\"productName\":\"リコーモバイル通信サービス　データＳＩＭ（Ｓ）２ＧＢ　月額利用料\",\"lineNumber\":\"11111\",\"serialNumber\":\"1\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"11\"},");
		// 拡張項目繰返.種別 = 解約済
		sb.append("{\"id\":1,\"contractType\":\"解約済\",\"productCode\":\"555555\",\"productName\":\"リコーモバイル通信サービス　データＳＩＭ（Ｓ）２ＧＢ　月額利用料\",\"lineNumber\":\"11111\",\"serialNumber\":\"1\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"11\"}");
		sb.append("]}");
		entity.setExtendsParameterIterance(sb.toString());
		entity.setItemContractName("リコーモバイル通信サービス　データＳＩＭ（Ｓ）２ＧＢ　月額利用料");
		entity.setLifecycleStatus("8");
		entity.setRicohItemCode("915868");
		cancelOrderEntityList.add(entity);

		try {
			batchStepComponent.process(param, cancelOrderEntityList);
			Assert.assertTrue("解約手配CSVが出力されていること。", csvFile.exists());
			String expectedFilePath = "src/test/resources/expected";
			// 作成されたCSVを期待結果と比較
			String expectedFileName = "componentSim_normal.csv";
			if (csvFile.exists() && Paths.get(expectedFilePath, expectedFileName).toFile().exists()) {
				byte[] actuals = Files.readAllBytes(Paths.get(filePath + "/" + fileName));
				byte[] expected = Files.readAllBytes(Paths.get(expectedFilePath + "/" + expectedFileName));
				Assert.assertArrayEquals(expected, actuals);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("エラーが発生した。");
		}
	}
}
