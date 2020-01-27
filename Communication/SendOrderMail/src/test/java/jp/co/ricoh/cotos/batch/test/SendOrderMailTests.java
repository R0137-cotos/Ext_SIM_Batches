package jp.co.ricoh.cotos.batch.test;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.util.BatchMomInfoProperties;
import jp.co.ricoh.cotos.logic.JobComponent;
import jp.co.ricoh.cotos.security.CreateJwt;

@RunWith(SpringRunner.class)
@SpringBootTest
@org.junit.Ignore
public class SendOrderMailTests extends TestBase {

	static ConfigurableApplicationContext context;

	@Autowired
	CreateJwt createJwt;

	@Autowired
	BatchMomInfoProperties batchProperty;

	@Autowired
	JobComponent jobComponent;

	@Autowired
	public void injectContext(ConfigurableApplicationContext injectContext) {
		context = injectContext;
	}

	@AfterClass
	public static void exit() throws Exception {
		if (null != context) {
			context.stop();
		}
	}

	@Test
	public void 正常系_JOB_メール送信できること() {
		jobComponent.run(new String[] { "src/test/resources/csv", "test.csv", "12", "test@example.com" });
	}

	@Test
	public void 正常系_JOB_メール送信できること_複数宛先() {
		jobComponent.run(new String[] { "src/test/resources/csv", "test.csv", "12", "test@example.com,test2@example.com" });
	}

	@Test
	public void 異常系_JOB_パラメーター数不一致() {
		try {
			jobComponent.run(new String[] { "dummy", "dummy" });
			Assert.fail("パラメータがないのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ステータス", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_JOB_存在しないファイル() {
		try {
			jobComponent.run(new String[] { "src/test/resources/csv", "dummy.csv", "12", "test@example.com" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ステータス", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_JOB_商品グループマスタID不正() {
		try {
			jobComponent.run(new String[] { "src/test/resources/csv", "test.csv", "dummy", "test@example.com" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ステータス", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_JOB_メールアドレス未設定() {
		try {
			jobComponent.run(new String[] { "src/test/resources/csv", "test.csv", "12", "" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ステータス", 1, e.getStatus());
		}
	}
}
