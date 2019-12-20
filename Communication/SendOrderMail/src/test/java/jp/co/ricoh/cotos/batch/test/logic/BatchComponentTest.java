package jp.co.ricoh.cotos.batch.test.logic;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.logic.BatchComponent;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchComponentTest extends TestBase {

	@Autowired
	BatchComponent batchComponent;

	@Test
	public void 正常系_標準_バッチテスト() {
		// TODO 商材切替が発生するデータを用意

		boolean actual = batchComponent.execute(new String[] { "dummy" });

		Assert.assertTrue(actual);
	}

	@Test
	public void 正常系_ROC_バッチテスト() {
		// TODO 商材切替が発生するデータを用意

		boolean actual = batchComponent.execute(new String[] { "dummy" });

		Assert.assertTrue(actual);
	}
}
