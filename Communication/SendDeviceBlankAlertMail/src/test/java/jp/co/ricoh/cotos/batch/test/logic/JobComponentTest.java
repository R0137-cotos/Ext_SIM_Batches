package jp.co.ricoh.cotos.batch.test.logic;

import java.util.Calendar;

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
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.logic.JobComponent;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JobComponentTest extends TestBase {

	static ConfigurableApplicationContext context;

	@Autowired
	JobComponent jobComponent;

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
	public void 正常系_JOB_メール送信できること() {
		context.getBean(DBConfig.class).initTargetTestData("sql/SendDeviceBlankAlertMailTests.sql");
		jobComponent.run(new String[] { "20200203" });
	}

	@Test
	public void 正常系_JOB_メール送信できること_パラメータ無し() {
		context.getBean(DBConfig.class).initTargetTestData("sql/NoParamSendDeviceBlankAlertMailTests.sql");
		Calendar cal = Calendar.getInstance();
		cal.set(2020, 2, 16, 11, 59, 59);
		Mockito.doReturn(cal.getTime()).when(batchStepComponent).getSysdate();
		jobComponent.run(new String[] {});
	}

	@Test
	public void 異常系_JOB_パラメーター数不一致() {
		try {
			jobComponent.run(new String[] { "dummy", "dummy" });
			Assert.fail("パラメータ数不一致なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ステータス", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_JOB_日付不正() {
		try {
			jobComponent.run(new String[] { "dummy" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ステータス", 1, e.getStatus());
		}
	}

	@Test
	public void 正常系_JOB_日付空文字() {
		context.getBean(DBConfig.class).initTargetTestData("sql/NoParamSendDeviceBlankAlertMailTests.sql");
		Calendar cal = Calendar.getInstance();
		cal.set(2020, 2, 16, 11, 59, 59);
		Mockito.doReturn(cal.getTime()).when(batchStepComponent).getSysdate();
		jobComponent.run(new String[] { "" });
	}
}
