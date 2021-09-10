package jp.co.ricoh.cotos.batch.test.component;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.AfterClass;
import org.junit.Assert;
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

import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.security.CotosAuthenticationDetails;
import jp.co.ricoh.cotos.commonlib.util.BatchMomInfoProperties;
import jp.co.ricoh.cotos.component.BatchUtil;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.ReplyOrderDto;
import jp.co.ricoh.cotos.security.CreateJwt;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchStepComponentSimTests extends TestBase {

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

	@SpyBean(name = "SIM")
	BatchStepComponent batchStepComponent;

	@AfterClass
	public static void exit() throws Exception {
		if (null != context) {
			context.getBean(DBConfig.class).clearData();
			context.stop();
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void beforeProcess_正常系() throws IOException {
		// 契約情報更新APIを無効にする
		Mockito.doNothing().when(batchUtil).callUpdateContract(Mockito.any(Contract.class));
		// 手配担当者登録APIを無効にする
		Mockito.doNothing().when(batchUtil).callAssignWorker(Mockito.anyList());
		// 手配業務受付APIを無効にする
		Mockito.doNothing().when(batchUtil).callAcceptWorkApi(Mockito.anyList());
		// 手配情報完了APIを無効にする
		Mockito.doNothing().when(batchUtil).callCompleteArrangement(Mockito.anyLong());

		try {
			List<ReplyOrderDto> csvlist = batchStepComponent.beforeProcess(new String[] { filePath, fileName });
			if (CollectionUtils.isEmpty(csvlist)) {
				Assert.fail("CSV読み込みに失敗した。");
			}
		} catch (ErrorCheckException e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void beforeProcess_正常系_引数不正() throws IOException {
		try {
			List<ReplyOrderDto> csvlist = batchStepComponent.beforeProcess(new String[] {});
			if (!CollectionUtils.isEmpty(csvlist)) {
				Assert.fail("nullでない");
			}
		} catch (ErrorCheckException e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void beforeProcess_正常系_空ファイル() throws IOException {
		try {
			List<ReplyOrderDto> csvlist = batchStepComponent.beforeProcess(new String[] { filePath, "empty.csv" });
			if (!CollectionUtils.isEmpty(csvlist)) {
				Assert.fail("nullでない");
			}
		} catch (ErrorCheckException e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void beforeProcess_正常系_ファイルが存在しない() throws IOException {
		// hoge12345678999.csvが環境に存在しないこと
		try {
			List<ReplyOrderDto> csvlist = batchStepComponent.beforeProcess(new String[] { filePath, "hoge12345678999.csv" });
			if (!CollectionUtils.isEmpty(csvlist)) {
				Assert.fail("nullでない");
			}
		} catch (ErrorCheckException e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void process_正常系() throws IOException {
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
			batchStepComponent.process(batchStepComponent.beforeProcess(new String[] { filePath, fileName }));
		} catch (ErrorCheckException e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void process_正常系_引数null() throws IOException {
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
		} catch (ErrorCheckException e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void process_異常系_契約情報更新API_エラー発生() throws IOException {
		// 契約情報更新APIでエラーを発生させる
		Mockito.doThrow(new RuntimeException()).when(batchUtil).callUpdateContract(Mockito.any(Contract.class));
		// 手配担当者登録APIを無効にする
		Mockito.doNothing().when(batchUtil).callAssignWorker(Mockito.anyList());
		// 手配業務受付APIを無効にする
		Mockito.doNothing().when(batchUtil).callAcceptWorkApi(Mockito.anyList());
		// 手配情報完了APIを無効にする
		Mockito.doNothing().when(batchUtil).callCompleteArrangement(Mockito.anyLong());
		テストデータ作成("sql/insertCancelReplySuccessTestData.sql");
		Boolean isAllSuccess = null;
		try {
			isAllSuccess = batchStepComponent.process(batchStepComponent.beforeProcess(new String[] { filePath, fileName }));
		} catch (ErrorCheckException e) {
			Assert.fail("エラーが発生した。");
		}
		assertFalse(isAllSuccess);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void process_異常系_数量減分_拡張項目繰返_なし() throws IOException {
		// 契約情報更新APIを無効にする
		Mockito.doNothing().when(batchUtil).callUpdateContract(Mockito.any(Contract.class));
		// 手配担当者登録APIを無効にする
		Mockito.doNothing().when(batchUtil).callAssignWorker(Mockito.anyList());
		// 手配業務受付APIを無効にする
		Mockito.doNothing().when(batchUtil).callAcceptWorkApi(Mockito.anyList());
		// 手配情報完了APIを無効にする
		Mockito.doNothing().when(batchUtil).callCompleteArrangement(Mockito.anyLong());
		テストデータ作成("sql/insertCancelReplySuccessTestData2.sql");
		Boolean isAllSuccess = null;
		try {
			isAllSuccess = batchStepComponent.process(batchStepComponent.beforeProcess(new String[] { filePath, fileName }));
		} catch (ErrorCheckException e) {
			Assert.fail("エラーが発生した。");
		}
		assertFalse(isAllSuccess);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void process_異常系_手配情報更新処理API_エラー発生() throws IOException {
		// 契約情報更新APIでエラーを発生させる
		Mockito.doThrow(new Exception()).when(batchUtil).callUpdateContract(Mockito.any(Contract.class));
		// 手配担当者登録APIを無効にする
		Mockito.doNothing().when(batchUtil).callAssignWorker(Mockito.anyList());
		// 手配業務受付APIを無効にする
		Mockito.doNothing().when(batchUtil).callAcceptWorkApi(Mockito.anyList());
		// 手配情報完了APIを無効にする
		Mockito.doNothing().when(batchUtil).callCompleteArrangement(Mockito.anyLong());
		テストデータ作成("sql/insertCancelReplySuccessTestData.sql");
		Boolean isAllSuccess = null;
		try {
			isAllSuccess = batchStepComponent.process(batchStepComponent.beforeProcess(new String[] { filePath, fileName }));
		} catch (ErrorCheckException e) {
			Assert.fail("エラーが発生した。");
		}
		assertFalse(isAllSuccess);
	}

	private void テストデータ作成(String sql) {
		context.getBean(DBConfig.class).clearData();
		context.getBean(DBConfig.class).initTargetTestData(sql);
	}

}
