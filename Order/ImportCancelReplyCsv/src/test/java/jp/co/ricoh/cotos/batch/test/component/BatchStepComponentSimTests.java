package jp.co.ricoh.cotos.batch.test.component;

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
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchStepComponentSimTests extends TestBase {

	static ConfigurableApplicationContext context;

	@SpyBean(name = "SIM")
	BatchStepComponent batchStepComponent;

	@Autowired
	CheckUtil checkUtil;

	/*
	@SpyBean
	BatchUtil batchUtil;
	
	@Autowired
	BatchMomInfoProperties batchProperty;
	
	@Autowired
	CreateJwt createJwt;
	
	@Autowired
	DBUtil dbUtil;
	
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
	@Autowired
	public void injectContext(ConfigurableApplicationContext injectContext) {
		context = injectContext;
		context.getBean(DBConfig.class).clearData();
	}

	@AfterClass
	public static void exit() throws Exception {
		if (null != context) {
			context.stop();
		}
	}

	private void テストデータ作成(String sql) {
		context.getBean(DBConfig.class).clearData();
		context.getBean(DBConfig.class).initTargetTestData(sql);
	}

	@Test
	public void リプライCSV取込解約処理_正常系() {
		// 契約情報更新APIを無効にする
		// Mockito.doNothing().when(batchUtil).callUpdateContract(Mockito.any(Contract.class));
		// 手配情報完了APIを無効にする
		// Mockito.doNothing().when(batchUtil).callCompleteArrangement(Mockito.anyLong());
		テストデータ作成("sql/insertCancelReplySuccessTestData.sql");
		try {
			batchStepComponent.process(new String[] { "src/test/resources/csv", "reply.csv" });
		} catch (Exception e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void パラメータチェック_正常系() {
		try {
			batchStepComponent.paramCheck(new String[] { "src/test/resources/csv", "reply.csv" });
		} catch (Exception e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void パラメータチェック_異常系_パラメータ数不正() {
		try {
			batchStepComponent.paramCheck(new String[] {});
			Assert.fail("エラーが発生しなかった。");
		} catch (ErrorCheckException e) {
			// Assert.assertEquals("ステータス", 1, e.getMessage());
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00001", messageInfo.get(0).getErrorId());
			Assert.assertEquals("パラメータ「ファイルディレクトリ/ファイル名」が設定されていません。", messageInfo.get(0).getErrorMessage());
		}
	}

}
