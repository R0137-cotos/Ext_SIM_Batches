package jp.co.ricoh.cotos.batch.test;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.BatchApplication;
import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.security.CotosAuthenticationDetails;
import jp.co.ricoh.cotos.commonlib.util.BatchMomInfoProperties;
import jp.co.ricoh.cotos.component.BatchUtil;
import jp.co.ricoh.cotos.logic.JobComponent;
import jp.co.ricoh.cotos.security.CreateJwt;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ImportCancelReplyCsvTests extends TestBase {

	static ConfigurableApplicationContext context;

	@SpyBean
	BatchUtil batchUtil;

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

	@AfterClass
	public static void exit() throws Exception {
		if (null != context) {
			context.getBean(DBConfig.class).clearData();
			context.stop();
		}
	}

	// main実行時にspringを再起動することに起因してAPIのモック化ができない。APIのモック化ができないことでエラーが発生するためスキップする。
	// バッチ処理内容のテストはJobComponent側でテストをしている。
	@SuppressWarnings("unchecked")
	@Test
	@Ignore
	public void 正常系() throws IOException {
		// 契約情報更新APIを無効にする
		Mockito.doNothing().when(batchUtil).callUpdateContract(Mockito.any(Contract.class));
		// 手配担当者登録APIを無効にする
		Mockito.doNothing().when(batchUtil).callAssignWorker(Mockito.anyList());
		// 手配業務受付APIを無効にする
		Mockito.doNothing().when(batchUtil).callAcceptWorkApi(Mockito.anyList());
		// 手配情報完了APIを無効にする
		Mockito.doNothing().when(batchUtil).callCompleteArrangement(Mockito.anyLong());
		テストデータ作成("sql/insertCancelReplySuccessTestData.sql");
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			BatchApplication.main(new String[] { filePath, fileName });
		} catch (Exception e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void 異常系_JOB_パラメーター数不一致() {
		try {
			JobComponent.setExitHandler(new TestExitHandler());
			// パラメータ無し
			BatchApplication.main(new String[] {});
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}

		try {
			JobComponent.setExitHandler(new TestExitHandler());
			// パラメータ1つ
			BatchApplication.main(new String[] { filePath });
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}

		try {
			JobComponent.setExitHandler(new TestExitHandler());
			// パラメータ3つ
			BatchApplication.main(new String[] { filePath, fileName, "dummy" });
			Assert.fail("パラメータ数不一致で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_JOB_ディレクトリが存在しない() throws IOException {
		// 出力ファイルパス　※テスト環境に存在しないこと
		String filePath = "hoge12345678999";

		try {
			JobComponent.setExitHandler(new TestExitHandler());
			BatchApplication.main(new String[] { filePath, fileName });
			Assert.fail("ディレクトリが存在しない状態で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_JOB_ファイルが存在しない() throws IOException {
		// 出力ファイル名　※テスト環境に存在しないこと
		String fileName = "hoge12345678999.csv";

		try {
			JobComponent.setExitHandler(new TestExitHandler());
			BatchApplication.main(new String[] { filePath, fileName });
			Assert.fail("ファイルが存在しない状態で処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ジョブの戻り値が1であること", 1, e.getStatus());
		}
	}

	private void テストデータ作成(String sql) {
		context.getBean(DBConfig.class).clearData();
		context.getBean(DBConfig.class).initTargetTestData(sql);
	}

}
