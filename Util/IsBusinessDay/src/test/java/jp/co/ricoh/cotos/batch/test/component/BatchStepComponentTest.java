package jp.co.ricoh.cotos.batch.test.component;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import lombok.val;

public class BatchStepComponentTest extends TestBase {

	@Test
	public void 正常系_標準_パラメーターチェックテスト() {
		BatchStepComponent baseComponent = new BatchStepComponent();
		baseComponent.paramCheck(new String[] {"dummy"});
	}

	@Test
	public void 正常系_標準_処理データ取得テスト() {
		BatchStepComponent baseComponent = new BatchStepComponent();
		val actualList = baseComponent.getDataList(null);

		Assert.assertNotNull("処理データリストが空でないこと", actualList);
	}

	@Test
	public void 正常系_標準_データチェックテスト() {
		BatchStepComponent baseComponent = new BatchStepComponent();
		boolean actual = baseComponent.dataCheck(null);

		Assert.assertTrue(actual);
	}

	@Test
	public void 正常系_標準_事前処理テスト() {
		BatchStepComponent baseComponent = new BatchStepComponent();
		boolean actual = baseComponent.beforeProcess(null);

		Assert.assertTrue(actual);
	}

	@Test
	public void 正常系_標準_プロセステスト() {
		BatchStepComponent baseComponent = new BatchStepComponent();
		boolean actual = baseComponent.process(null);

		Assert.assertTrue(actual);
	}

	@Test
	public void 正常系_標準_事後処理テスト() {
		BatchStepComponent baseComponent = new BatchStepComponent();
		boolean actual = baseComponent.afterProcess(null);

		Assert.assertTrue(actual);
	}

	@Test
	public void 異常系_標準_データチェックテスト_パラメーター数不一致() {

		try {
			BatchStepComponent baseComponent = new BatchStepComponent();
			baseComponent.paramCheck(new String[] { "dummy", "dummy" });
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00001", messageInfo.get(0).getErrorId());
			Assert.assertEquals("パラメータ「パラメーター1/パラメーター2」が設定されていません。", messageInfo.get(0).getErrorMessage());
		}
	}
}
