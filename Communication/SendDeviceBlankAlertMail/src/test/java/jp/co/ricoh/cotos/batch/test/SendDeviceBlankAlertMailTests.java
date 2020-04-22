package jp.co.ricoh.cotos.batch.test;

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
public class SendDeviceBlankAlertMailTests extends TestBase {

	static ConfigurableApplicationContext context;

	@Autowired
	public void injectContext(ConfigurableApplicationContext injectContext) {
		context = injectContext;
		context.getBean(DBConfig.class).clearData();
		context.getBean(DBConfig.class).initTargetTestData("sql/SendDeviceBlankAlertMailTests.sql");
	}

	@AfterClass
	public static void exit() throws Exception {
		if (null != context) {
			context.getBean(DBConfig.class).clearData();
			context.stop();
		}
	}

	@Test
	public void 正常系_ジョブテスト() {
		BatchApplication.main(new String[] { "20200203" });
	}

	@Test
	public void 正常系_ジョブテスト_パラメータ無し() {
		BatchApplication.main(new String[] {});
	}

	@Test
	public void 正常系_パラメーター数不正() {
		try {
			BatchApplication.main(new String[] { "dummy" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals(1, e.getStatus());
		}
	}

	@Test
	public void 異常系_パラメーター数不一致() {
		try {
			BatchApplication.main(new String[] { "dummy", "dummy" });
			Assert.fail("パラメータ数不一致なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals(1, e.getStatus());
		}
	}
}