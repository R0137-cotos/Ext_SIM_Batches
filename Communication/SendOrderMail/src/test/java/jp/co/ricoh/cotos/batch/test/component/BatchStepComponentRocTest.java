package jp.co.ricoh.cotos.batch.test.component;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.component.roc.BatchStepComponentRoc;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchStepComponentRocTest extends TestBase {

	@Autowired
	@Qualifier("ROC")
	BatchStepComponentRoc componentRoc;

	@Test
	public void 正常系_ROC_プロセステスト() {
		boolean actual = componentRoc.process(null);

		Assert.assertTrue(actual);
	}
}
