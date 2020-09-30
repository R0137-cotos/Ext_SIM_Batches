package jp.co.ricoh.cotos.batch.test.component;

import org.junit.Assert;
import org.junit.Test;

import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.component.BatchUtil;
import jp.co.ricoh.cotos.component.roc.BatchStepComponentRoc;
import mockit.Expectations;
import mockit.Mocked;

public class BatchStepComponentRocTest extends TestBase {

	@Mocked
	private BatchUtil batchUtil;

	@Test
	public void 正常系_ROC_プロセステスト() {

		// モック
		new Expectations(BatchUtil.class) {
			{
				batchUtil.mockMethod1();
				// mockMethod1の戻り値を指定
				result = true;

				batchUtil.mockMethod2();
				// mockMethod2の戻り値を指定
				result = true;
			}
		};

		// 認証処理
		auth();

		BatchStepComponentRoc componentRoc = new BatchStepComponentRoc();
		boolean actual = componentRoc.process(null);

		Assert.assertTrue(actual);
	}
}
