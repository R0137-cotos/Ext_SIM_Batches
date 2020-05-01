package jp.co.ricoh.cotos.batch.test.logic;

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
			// パラメータ3つ
			batchComponent.execute(new String[] { "20190626", "", "" });
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00001", messageInfo.get(0).getErrorId());
			Assert.assertEquals("パラメータ「処理年月日/ディレクトリ名/作成ファイル名」が設定されていません。", messageInfo.get(0).getErrorMessage());
		}
	}
}
