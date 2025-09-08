package jp.co.ricoh.cotos.batch.test.logic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.logic.JobComponent;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JobComponentTest extends TestBase {

	static ConfigurableApplicationContext context;

	@Autowired
	JobComponent jobComponent;

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
	public void 正常系() throws IOException {
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
		// 2019/06/21 月末営業日-5日　要処理日付
		try {
			jobComponent.run(new String[] { "20190621", filePath, fileName });
		} catch (ExitException e) {
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
	public void 正常系_処理対象データ無し() throws IOException {
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
		// 2019/06/21 月末営業日-5日　要処理日付
		try {
			jobComponent.run(new String[] { "20190621", filePath, fileName });
		} catch (ExitException e) {
			Assert.fail("エラーが発生した。");
		}
		Assert.assertFalse("解約手配CSVが出力されていないこと。", csvFile.exists());
	}

	@Test
	public void 正常系_処理対象データ無し_processでデータ無し判定() throws IOException {
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
		// 2019/06/21 月末営業日-5日　要処理日付
		try {
			jobComponent.run(new String[] { "20190621", filePath, fileName });
		} catch (ExitException e) {
			Assert.fail("エラーが発生した。");
		}

		Assert.assertFalse("解約手配CSVが出力されていないこと。", csvFile.exists());
	}

	@Test
	public void 異常系_JOB_月末営業日マイナス5営業日以外() {
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
			jobComponent.run(new String[] { "20190624", filePath, fileName });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が2であること", 2, e.getStatus());
		}

		// 処理不要日付　営業日 月末営業日-5日以前 2019/06/20
		try {
			jobComponent.run(new String[] { "20190620", filePath, fileName });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が2であること", 2, e.getStatus());
		}

		// 処理不要日付　非営業日 月末営業日-5日以降 2019/06/22
		try {
			jobComponent.run(new String[] { "20190622", filePath, fileName });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が2であること", 2, e.getStatus());
		}

		// 処理不要日付　非営業日 月末営業日-5日以前 2019/06/16
		try {
			jobComponent.run(new String[] { "20190616", filePath, fileName });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が2であること", 2, e.getStatus());
		}
	}

	@Test
	public void 異常系_拡張項目繰返がJSON形式でない_全解約分() throws IOException {
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
		// 2019/06/21 月末営業日-5日　要処理日付
		try {
			jobComponent.run(new String[] { "20190621", filePath, fileName });
			Assert.fail("JSON形式のparse失敗エラーが発生しなかった。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_拡張項目繰返でJSONマッピングエラー_全解約分() throws IOException {
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
		// 2019/06/21 月末営業日-5日　要処理日付
		try {
			jobComponent.run(new String[] { "20190621", filePath, fileName });
			Assert.fail("JSON形式のmapping失敗エラーが発生しなかった。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_拡張項目繰返でJSONマッピングエラー_数量減分() throws IOException {
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
		// 2019/06/21 月末営業日-5日　要処理日付
		try {
			jobComponent.run(new String[] { "20190621", filePath, fileName });
			Assert.fail("JSON形式のmapping失敗エラーが発生しなかった。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_JOB_パラメーター数不一致() {
		try {
			// パラメータ無し
			jobComponent.run(new String[] {});
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}

		try {
			// パラメータ1つ
			jobComponent.run(new String[] { "20190621" });
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}

		try {
			// パラメータ2つ
			jobComponent.run(new String[] { "20190621", filePath });
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}

		try {
			// パラメータ4つ
			jobComponent.run(new String[] { "20190621", filePath, fileName, "dummy" });
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_JOB_日付変換失敗() {
		try {
			jobComponent.run(new String[] { "2019/06/21", filePath, fileName });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_JOB_ファイルが既に存在() throws IOException {
		// ファイルを事前に作成する
		if (!csvFile.exists()) {
			csvFile.createNewFile();
		}

		try {
			jobComponent.run(new String[] { "20190621", filePath, fileName });
			Assert.fail("ファイルが存在する状態で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_JOB_一時ファイルが既に存在() throws IOException {
		// 一時ファイルを事前に作成する
		if (!tmpFile.exists()) {
			tmpFile.createNewFile();
		}

		try {
			jobComponent.run(new String[] { "20190621", filePath, fileName });
			Assert.fail("ファイルが存在する状態で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		} finally {
			// 作成した一時ファイルを削除
			Files.deleteIfExists(tmpFile.toPath());
		}
	}

	@Test
	public void 異常系_JOB_ディレクトリが存在しない() throws IOException {
		// 出力ファイルパス　※テスト環境に存在しないこと
		String filePath = "hoge12345678999";

		try {
			jobComponent.run(new String[] { "20190621", filePath, fileName });
			Assert.fail("ディレクトリが存在しない状態で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}
	}
}
