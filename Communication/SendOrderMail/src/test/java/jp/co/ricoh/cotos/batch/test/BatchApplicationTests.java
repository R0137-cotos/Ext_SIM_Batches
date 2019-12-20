package jp.co.ricoh.cotos.batch.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.BatchApplication;
import jp.co.ricoh.cotos.batch.TestBase;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchApplicationTests extends TestBase {

	@Test
	public void 正常系_メイン処理テスト() {
		BatchApplication.main(new String[] { "dummy" });
	}

	@Test
	public void 異常系_パラメーター数不一致() {
		try {
			BatchApplication.main(new String[] { "dummy", "dummy" });
			fail("パラメータ不正なのにエラーにならない");
		} catch (ExitException e) {
		}
	}
}
