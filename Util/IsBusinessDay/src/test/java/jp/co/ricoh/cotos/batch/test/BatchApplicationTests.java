package jp.co.ricoh.cotos.batch.test;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import jp.co.ricoh.cotos.ApplicationContextProvider;
import jp.co.ricoh.cotos.BatchApplication;
import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.entity.EnumType.ServiceCategory;
import jp.co.ricoh.cotos.commonlib.entity.communication.Communication;
import jp.co.ricoh.cotos.commonlib.repository.communication.CommunicationRepository;

public class BatchApplicationTests extends TestBase {

	@Test
	public void 正常系_メイン処理テスト() {

		DBConfig dbConfig = new DBConfig();
		dbConfig.clearData();
		dbConfig.initTargetTestData("insertData.sql");
		try {
			BatchApplication.main(new String[] { "dummy" });
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		CommunicationRepository comRepo = context.getBean(CommunicationRepository.class);
		Communication com1 = comRepo.findOne(1L);
		Communication com2 = comRepo.findOne(2L);
		Communication com3 = comRepo.findOne(3L);
		Communication com4 = comRepo.findOne(4L);

		Assert.assertEquals("サービスカテゴリーが更新されていること", ServiceCategory.電力_契約, com1.getServiceCategory());
		Assert.assertEquals("サービスカテゴリーが更新されていること", ServiceCategory.電力_契約, com2.getServiceCategory());
		Assert.assertEquals("サービスカテゴリーが更新されていること", ServiceCategory.電力_見積, com3.getServiceCategory());
		Assert.assertEquals("サービスカテゴリーが更新されていること", ServiceCategory.電力_見積, com4.getServiceCategory());
	}

	@Test
	public void 異常系_パラメーター数不一致() {
		try {
			BatchApplication.main(new String[] { "dummy", "dummy" });
			Assert.fail("パラメータ不正なのにエラーにならない");
		} catch (ExitException e) {
		}
	}

	@Test
	public void トランザクションテスト() {

		DBConfig dbConfig = new DBConfig();
		dbConfig.clearData();
		dbConfig.initTargetTestData("insertData_NG.sql");

		try {
			BatchApplication.main(new String[] { "dummy" });
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		CommunicationRepository comRepo = context.getBean(CommunicationRepository.class);
		Communication com1 = comRepo.findOne(1L);
		Communication com3 = comRepo.findOne(3L);

		Assert.assertEquals("サービスカテゴリーが更新されていないこと", ServiceCategory.見積, com1.getServiceCategory());
		Assert.assertEquals("サービスカテゴリーが更新されていないこと", ServiceCategory.見積, com3.getServiceCategory());
	}

}
