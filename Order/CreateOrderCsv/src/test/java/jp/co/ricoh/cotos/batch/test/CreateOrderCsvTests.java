package jp.co.ricoh.cotos.batch.test;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.logic.JobComponent;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CreateOrderCsvTests extends TestBase {

	static ConfigurableApplicationContext context;

	@Autowired
	JobComponent jobComponent;

	@Autowired
	public void injectContext(ConfigurableApplicationContext injectContext) {
		context = injectContext;
		context.getBean(DBConfig.class).clearData();
	}

	private void テストデータ作成(String filePath) {
		context.getBean(DBConfig.class).clearData();
		context.getBean(DBConfig.class).initTargetTestData(filePath);
	}

	@AfterClass
	public static void exit() throws Exception {
		if (null != context) {
			// context.getBean(DBConfig.class).clearData();
			context.stop();
		}
	}

	@Test
	public void 正常系_test() {
		テストデータ作成("insertContractData.sql");
		jobComponent.run(new String[] { "C:/Users/yamazaki-takumi/Desktop/sql", "test.csv", "20181019" });
	}
}
