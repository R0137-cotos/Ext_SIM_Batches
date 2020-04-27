package jp.co.ricoh.cotos.batch.test;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.BatchApplication;
import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CreateOrderCsvTests extends TestBase {

	static ConfigurableApplicationContext context;

	final private String outputPath = "output/";

	@Autowired
	public void injectContext(ConfigurableApplicationContext injectContext) {
		context = injectContext;
		context.getBean(DBConfig.class).clearData();
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
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
		BatchApplication.main(new String[] { "20191018", outputPath, "result_initial.csv", "1" });
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
}