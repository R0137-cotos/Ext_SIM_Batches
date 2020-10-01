package jp.co.ricoh.cotos.batch.test.component;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;

public class BatchStepComponentTest extends TestBase {

	@Test
	public void 正常系_標準_パラメーターチェックテスト_正常系() {
		BatchStepComponent baseComponent = new BatchStepComponent();
		try {
			baseComponent.paramCheck(new String[] { "20191028" });
		} catch (ErrorCheckException e) {
			Assert.fail("パラメータエラーとなった。");
		}
		try {
			baseComponent.paramCheck(new String[] { "2019/10/28" });
		} catch (ErrorCheckException e) {
			Assert.fail("パラメータエラーとなった。");
		}
	}

	@Test
	public void 異常系_標準_パラメーターチェックテスト_パラメーター数不一致() {
		try {
			BatchStepComponent baseComponent = new BatchStepComponent();
			baseComponent.paramCheck(new String[] { "20191028", "dummy" });
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00001", messageInfo.get(0).getErrorId());
			Assert.assertEquals("パラメータ「処理年月日」が設定されていません。", messageInfo.get(0).getErrorMessage());
		}
	}

	@Test
	public void 異常系_標準_パラメーターチェックテスト_パラメーター不正() {
		try {
			BatchStepComponent baseComponent = new BatchStepComponent();
			baseComponent.paramCheck(new String[] { "dummy" });
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00032", messageInfo.get(0).getErrorId());
			Assert.assertEquals("処理年月日のフォーマットはyyyyMMddです。", messageInfo.get(0).getErrorMessage());
		}
	}

	@Test
	public void 正常系_標準_プロセステスト() {
		BatchStepComponent baseComponent = new BatchStepComponent();
		// 非営業日カレンダーマスタに20191028が共通の非営業日として定義されていないこと
		boolean actual = baseComponent.process("20191028");
		Assert.assertTrue(actual);
		// スラッシュ区切りでも営業日判定可能であること
		actual = baseComponent.process("2019/10/28");
		Assert.assertTrue(actual);
		// 非営業日カレンダーマスタに20191027が共通の非営業日として定義されていること
		actual = baseComponent.process("20191027");
		Assert.assertFalse(actual);
		// 非営業日カレンダーマスタに20191025がベンダ固有の非営業日として定義されていること
		actual = baseComponent.process("20191025");
		Assert.assertTrue(actual);
		// スラッシュ区切りでも営業日判定可能であること
		actual = baseComponent.process("2019/10/25");
		Assert.assertTrue(actual);
	}

}
