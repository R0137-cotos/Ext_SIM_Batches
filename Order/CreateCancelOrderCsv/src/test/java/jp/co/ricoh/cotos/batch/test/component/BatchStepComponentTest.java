package jp.co.ricoh.cotos.batch.test.component;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
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

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchStepComponentTest extends TestBase {

	static ConfigurableApplicationContext context;

	@SpyBean(name = "BASE")
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
		// 出力ファイルを事前に作成する
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
}
