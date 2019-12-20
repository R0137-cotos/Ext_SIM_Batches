package jp.co.ricoh.cotos.batch.test.logic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.logic.JobComponent;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JobComponentTest extends TestBase {

	@Autowired
	JobComponent jobComponent;

	@Test
	public void 正常系_標準_ジョブテスト() {
		jobComponent.run(new String[] { "dummy" });
	}

	@Test
	public void 異常系_標準_パラメーター数不一致() {
		try {
			jobComponent.run(new String[] { "dummy", "dummy" });
		} catch (ExitException e) {

		}
	}
}
