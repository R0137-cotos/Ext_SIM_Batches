package jp.co.ricoh.cotos.batch.test.logic;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.batch.test.mock.WithMockCustomUser;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.logic.BatchComponent;
import jp.co.ricoh.cotos.logic.JobComponent;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchComponentTest extends TestBase {

	static ConfigurableApplicationContext context;

	@Autowired
	JobComponent jobComponent;

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
	public void 異常系_パラメーター数不一致() throws Exception {
		try {
			batchComponent.execute(new String[] { "dummy", "dummy" });
			Assert.fail("正常終了");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals(messageInfo.get(0).getErrorId(), "ROT00001");
			Assert.assertEquals(messageInfo.get(0).getErrorMessage(), "パラメータ「処理日」が設定されていません。");
		}
	}

	@Test
	public void 異常系_日付不正() throws Exception {
		try {
			batchComponent.execute(new String[] { "dummy" });
			Assert.fail("正常終了");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals(messageInfo.get(0).getErrorId(), "ROT00032");
			Assert.assertEquals(messageInfo.get(0).getErrorMessage(), "日付のフォーマットはyyyyMMddです。");
		}
	}

	@Test
	@WithMockCustomUser
	public void 正常系_メール送信できること() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("sql/SendDeviceBlankAlertMailTests.sql");
		try {
			batchComponent.execute(new String[] { "20200203" });
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("エラー");
		}
	}

	@Test
	@WithMockCustomUser
	public void 正常系_メール送信できること_パラメータ無し() throws Exception {
		context.getBean(DBConfig.class).initTargetTestData("sql/NoParamSendDeviceBlankAlertMailTests.sql");
		Calendar cal = Calendar.getInstance();
		cal.set(2020, 2, 16, 11, 59, 59);
		Mockito.doReturn(cal.getTime()).when(batchStepComponent).getSysdate();
		try {
			batchComponent.execute(new String[] {});
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("エラー");
		}
	}
}