package jp.co.ricoh.cotos.batch.test.component;

import java.io.IOException;

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

	@Test
	public void リプライCSV取込解約_正常系() throws IOException {
		// 契約情報更新APIを無効にする
		Mockito.doNothing().when(batchUtil).callUpdateContract(Mockito.any(Contract.class));
		// 手配情報完了APIを無効にする
		Mockito.doNothing().when(batchUtil).callCompleteArrangement(Mockito.anyLong());
		テストデータ作成("sql/insertCancelReplySuccessTestData.sql");

		try {
			batchStepComponent.process(new String[] { filePath, fileName });
		} catch (ErrorCheckException e) {
			Assert.fail("エラーが発生した。");
		}
	}

	private void テストデータ作成(String sql) {
		context.getBean(DBConfig.class).clearData();
		context.getBean(DBConfig.class).initTargetTestData(sql);
	}

}
