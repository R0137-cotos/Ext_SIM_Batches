package jp.co.ricoh.cotos.batch.test.logic;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import jp.co.ricoh.cotos.logic.BatchComponent;
import jp.co.ricoh.cotos.util.OperationDateException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchComponentTest extends TestBase {

	static ConfigurableApplicationContext context;

	@SpyBean(name = "BASE")
	BatchStepComponent batchStepComponent;

	@Autowired
	BatchComponent batchComponent;

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
	public void 正常系() throws Exception {
		context.getBean(DBConfig.class).initTargetTestData("createCancelOrderSuccessTestData.sql");

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
		// 2019/06/26 月末営業日-2日　要処理日付
		try {
			batchComponent.execute(new String[] { "20190626", filePath, fileName });
		} catch (ErrorCheckException e) {
			Assert.fail("エラーが発生した。");
		}

		Assert.assertTrue("解約手配CSVが出力されていること。", csvFile.exists());
		String expectedFilePath = "src/test/resources/expected";
		// 作成されたCSVを期待結果と比較
		String expectedFileName = "normal.csv";
		if (csvFile.exists() && Paths.get(expectedFilePath, expectedFileName).toFile().exists()) {
			byte[] actuals = Files.readAllBytes(Paths.get(filePath + "/" + fileName));
			byte[] expected = Files.readAllBytes(Paths.get(expectedFilePath + "/" + expectedFileName));
			Assert.assertArrayEquals(expected, actuals);
		}
	}

	@Test
	public void 正常系_処理対象データ無し() throws Exception {
		// データ投入を行わない

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
		// 2019/06/26 月末営業日-2日　要処理日付
		try {
			batchComponent.execute(new String[] { "20190626", filePath, fileName });
		} catch (ErrorCheckException e) {
			Assert.fail("エラーが発生した。");
		}
		Assert.assertFalse("解約手配CSVが出力されていないこと。", csvFile.exists());
	}

	@Test
	public void 正常系_処理対象データ無し_processでデータ無し判定() throws Exception {
		// 解約オーダーリストの取得には成功するが、processメソッド処理(解約手配CSV作成処理)で出力データ無しと判定されるケース
		context.getBean(DBConfig.class).initTargetTestData("createCancelOrderNoTargetTestData.sql");

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
		// 2019/06/26 月末営業日-2日　要処理日付
		try {
			batchComponent.execute(new String[] { "20190626", filePath, fileName });
		} catch (ErrorCheckException e) {
			Assert.fail("エラーが発生した。");
		}

		Assert.assertFalse("解約手配CSVが出力されていないこと。", csvFile.exists());
	}

	@Test
	public void 異常系_月末営業日マイナス2営業日以外() throws Exception {
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
		// 2019/06/26 月末営業日-2日　要処理日付

		// 処理不要日付　営業日 月末営業日-2日以降 2019/06/27
		try {
			batchComponent.execute(new String[] { "20190627", filePath, fileName });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (OperationDateException e) {
			// OperationDateExceptionが発生していること
			Assert.assertNotEquals(null, e);
		}

		// 処理不要日付　営業日 月末営業日-2日以前 2019/06/25
		try {
			batchComponent.execute(new String[] { "20190625", filePath, fileName });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (OperationDateException e) {
			// OperationDateExceptionが発生していること
			Assert.assertNotEquals(null, e);
		}


		// 処理不要日付　非営業日 月末営業日-2日以降 2019/06/29
		try {
			batchComponent.execute(new String[] { "20190629", filePath, fileName });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (OperationDateException e) {
			// OperationDateExceptionが発生していること
			Assert.assertNotEquals(null, e);
		}


		// 処理不要日付　非営業日 月末営業日-2日以前 2019/06/23
		try {
			batchComponent.execute(new String[] { "20190623", filePath, fileName });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (OperationDateException e) {
			// OperationDateExceptionが発生していること
			Assert.assertNotEquals(null, e);
		}
	}

	@Test
	public void 異常系_拡張項目繰返がJSON形式でない_全解約分() throws Exception {
		context.getBean(DBConfig.class).initTargetTestData("createCancelOrderJsonParseErrorTestData1.sql");

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
		// 2019/06/26 月末営業日-2日　要処理日付
		try {
			batchComponent.execute(new String[] { "20190626", filePath, fileName });
			Assert.fail("JSON形式のparse失敗エラーが発生しなかった。");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00113", messageInfo.get(0).getErrorId());
			Assert.assertEquals("JSONデータのマッピングに失敗しました。", messageInfo.get(0).getErrorMessage());
		}
	}

	@Test
	public void 異常系_拡張項目繰返がJSON形式でない_数量減分() throws Exception {
		context.getBean(DBConfig.class).initTargetTestData("createCancelOrderJsonParseErrorTestData2.sql");

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
		// 2019/06/26 月末営業日-2日　要処理日付
		try {
			batchComponent.execute(new String[] { "20190626", filePath, fileName });
			Assert.fail("JSON形式のparse失敗エラーが発生しなかった。");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00113", messageInfo.get(0).getErrorId());
			Assert.assertEquals("JSONデータのマッピングに失敗しました。", messageInfo.get(0).getErrorMessage());
		}
	}

	@Test
	public void 異常系_拡張項目繰返でJSONマッピングエラー_全解約分() throws Exception {
		context.getBean(DBConfig.class).initTargetTestData("createCancelOrderJsonMappingErrorTestData1.sql");

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
		// 2019/06/26 月末営業日-2日　要処理日付
		try {
			batchComponent.execute(new String[] { "20190626", filePath, fileName });
			Assert.fail("JSON形式のmapping失敗エラーが発生しなかった。");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00113", messageInfo.get(0).getErrorId());
			Assert.assertEquals("JSONデータのマッピングに失敗しました。", messageInfo.get(0).getErrorMessage());
		}
	}

	@Test
	public void 異常系_拡張項目繰返でJSONマッピングエラー_数量減分() throws Exception {
		context.getBean(DBConfig.class).initTargetTestData("createCancelOrderJsonMappingErrorTestData2.sql");

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
		// 2019/06/26 月末営業日-2日　要処理日付
		try {
			batchComponent.execute(new String[] { "20190626", filePath, fileName });
			Assert.fail("JSON形式のmapping失敗エラーが発生しなかった。");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00113", messageInfo.get(0).getErrorId());
			Assert.assertEquals("JSONデータのマッピングに失敗しました。", messageInfo.get(0).getErrorMessage());
		}
	}

	@Test
	public void 異常系_パラメーター数不一致() throws Exception {
		try {
			// パラメータ無し
			batchComponent.execute(new String[] {});
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
			batchComponent.execute(new String[] { "20190626" });
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
			batchComponent.execute(new String[] { "20190626", filePath });
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
			batchComponent.execute(new String[] { "20190626", filePath, fileName, "dummy" });
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
	public void 異常系_日付変換失敗() throws Exception {
		try {
			batchComponent.execute(new String[] { "2019/06/26", filePath, fileName });
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
	public void 異常系_ファイルが既に存在() throws Exception {
		// ファイルを事前に作成する
		if (!csvFile.exists()) {
			csvFile.createNewFile();
		}

		try {
			batchComponent.execute(new String[] { "20190626", filePath, fileName });
			Assert.fail("ファイルが存在する状態で処理が実行された。");
		} catch (FileAlreadyExistsException e) {
			// FileAlreadyExistsExceptionが発生していること
			Assert.assertNotEquals(null, e);
		}
	}

	@Test
	public void 異常系_一時ファイルが既に存在() throws Exception {
		// 一時ファイルを事前に作成する
		if (!tmpFile.exists()) {
			tmpFile.createNewFile();
		}

		try {
			batchComponent.execute(new String[] { "20190626", filePath, fileName });
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
	public void 異常系_ディレクトリが存在しない() throws Exception {
		// 出力ファイルパス　※テスト環境に存在しないこと
		String filePath = "hoge12345678999";

		try {
			batchComponent.execute(new String[] { "20190626", filePath, fileName });
			Assert.fail("ディレクトリが存在しない状態で処理が実行された。");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00110", messageInfo.get(0).getErrorId());
			Assert.assertEquals("指定されたディレクトリが存在しません。", messageInfo.get(0).getErrorMessage());
		}
	}
}
