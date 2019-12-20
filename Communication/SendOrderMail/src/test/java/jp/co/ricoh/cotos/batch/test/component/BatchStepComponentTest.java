package jp.co.ricoh.cotos.batch.test.component;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import lombok.val;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchStepComponentTest extends TestBase {

	@Autowired
	@Qualifier("BASE")
	BatchStepComponent baseComponent;

	@Autowired
	ObjectMapper mapper;

	@Test
	public void 正常系_標準_パラメーターチェックテスト() {
		baseComponent.paramCheck(new String[] {"dummy"});
	}

	@Test
	public void 正常系_標準_処理データ取得テスト() {
		val actualList = baseComponent.getDataList(null);

		Assert.assertNotNull("処理データリストが空でないこと", actualList);
	}

	@Test
	public void 正常系_標準_データチェックテスト() {
		boolean actual = baseComponent.dataCheck(null);

		Assert.assertTrue(actual);
	}

	@Test
	public void 正常系_標準_事前処理テスト() {
		boolean actual = baseComponent.beforeProcess(null);

		Assert.assertTrue(actual);
	}

	@Test
	public void 正常系_標準_プロセステスト() {
		boolean actual = baseComponent.process(null);

		Assert.assertTrue(actual);
	}

	@Test
	public void 正常系_標準_事後処理テスト() {
		boolean actual = baseComponent.afterProcess(null);

		Assert.assertTrue(actual);
	}

	@Test
	public void 異常系_標準_データチェックテスト_パラメーター数不一致() {

		try {
			baseComponent.paramCheck(new String[] { "dummy", "dummy" });
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals(messageInfo.get(0).getErrorId(), "ROT00001");
			Assert.assertEquals(messageInfo.get(0).getErrorMessage(), "パラメータ「パラメーター1/パラメーター2」が設定されていません。");
		}
	}
}
