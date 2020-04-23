package jp.co.ricoh.cotos.batch.test.logic;

import java.util.Calendar;

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


	/*
	@Autowired
	CreateJwt createJwt;
	
	@Autowired
	BatchMomInfoProperties batchProperty;
	
	@Autowired
	public void injectContext(ConfigurableApplicationContext injectContext) {
		String jwt = createJwt.execute();
		CotosAuthenticationDetails principal = new CotosAuthenticationDetails(batchProperty.getMomEmpId(), "sid", null, null, jwt, true, true, null);
		Authentication auth = new PreAuthenticatedAuthenticationToken(principal, null, null);
		SecurityContextHolder.getContext().setAuthentication(auth);
		context = injectContext;
		context.getBean(DBConfig.class).clearData();
	}
	*/

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
		// Mockito.doReturn(cal.getTime()).when(batchStepComponent).getSysdate();
		jobComponent.run(new String[] {});
	}

	@Test
	public void 異常系_JOB_パラメーター数不一致() {
		try {
			jobComponent.run(new String[] {});
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}

		try {
			jobComponent.run(new String[] { "dummy", "dummy" });
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}

		try {
			jobComponent.run(new String[] { "dummy", "dummy", "dummy", "dummy" });
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_JOB_日付変換失敗() {
		try {
			jobComponent.run(new String[] { "2019/06/26", "dummy", "dummy" });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}
	}

	@Test
	public void 正常系_JOB_月末営業日マイナス2営業日以外() {
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
			jobComponent.run(new String[] { "20190627", "dummy", "dummy" });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が2であること", 2, e.getStatus());
		}

		// 処理不要日付　営業日 月末営業日-2日以前 2019/06/25
		try {
			jobComponent.run(new String[] { "20190625", "dummy", "dummy" });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が2であること", 2, e.getStatus());
		}

		// 処理不要日付　非営業日 月末営業日-2日以降 2019/06/29
		try {
			jobComponent.run(new String[] { "20190629", "dummy", "dummy" });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が2であること", 2, e.getStatus());
		}

		// 処理不要日付　非営業日 月末営業日-2日以前 2019/06/23
		try {
			jobComponent.run(new String[] { "20190623", "dummy", "dummy" });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が2であること", 2, e.getStatus());
		}
	}

	@Test
	public void 正常系() {
		context.getBean(DBConfig.class).initTargetTestData("jobComponentTestData.sql");
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
			jobComponent.run(new String[] { "20190626", "output", "test.csv" });
		} catch (ExitException e) {
			Assert.fail("エラーが発生した。");
		}
	}
}
