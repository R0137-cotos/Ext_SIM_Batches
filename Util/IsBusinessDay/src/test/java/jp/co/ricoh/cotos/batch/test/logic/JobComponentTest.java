package jp.co.ricoh.cotos.batch.test.logic;

import org.junit.Assert;
import org.junit.Test;

import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.logic.JobComponent;

public class JobComponentTest extends TestBase {

	@Test
	public void 正常系_標準_ジョブテスト() {

		// 認証処理
		auth();

		try {
			JobComponent jobComponent = new JobComponent();
			jobComponent.run(new String[] { "20191028" });
		} catch (ExitException e) {
			Assert.fail("非営業日として判定された。");
		}
	}

	@Test
	public void 異常系_標準_パラメーター数不一致() {
		try {
			JobComponent jobComponent = new JobComponent();
			jobComponent.run(new String[] { "dummy", "dummy" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ステータス", 1, e.getStatus());
		}
	}
}
