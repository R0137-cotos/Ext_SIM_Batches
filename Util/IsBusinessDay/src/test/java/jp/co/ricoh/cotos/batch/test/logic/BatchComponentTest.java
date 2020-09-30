package jp.co.ricoh.cotos.batch.test.logic;

import org.junit.Assert;
import org.junit.Test;

import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.logic.BatchComponent;

public class BatchComponentTest extends TestBase {

	@Test
	public void 正常系_標準_バッチテスト() {
		// TODO 商材切替が発生するデータを用意

		// 認証処理
		auth();

		BatchComponent batchComponent = new BatchComponent();
		boolean actual = batchComponent.execute(new String[] { "dummy" });

		Assert.assertTrue(actual);
	}

	@Test
	public void 正常系_ROC_バッチテスト() {
		// TODO 商材切替が発生するデータを用意

		// 認証処理
		auth();

		BatchComponent batchComponent = new BatchComponent();
		boolean actual = batchComponent.execute(new String[] { "dummy" });

		Assert.assertTrue(actual);
	}
}
