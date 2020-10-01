package jp.co.ricoh.cotos.batch.test;


import org.junit.Assert;
import org.junit.Test;

import jp.co.ricoh.cotos.BatchApplication;
import jp.co.ricoh.cotos.batch.TestBase;

public class BatchApplicationTests extends TestBase {

	@Test
	public void 正常系_メイン処理テスト() {

		try {
			BatchApplication.main(new String[] { "20191028" });
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void 異常系_パラメーター数不一致() {
		try {
			BatchApplication.main(new String[] { "dummy", "dummy" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ExitException e) {
		}
	}
}
