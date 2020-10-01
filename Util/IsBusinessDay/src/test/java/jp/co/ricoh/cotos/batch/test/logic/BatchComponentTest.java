package jp.co.ricoh.cotos.batch.test.logic;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.logic.BatchComponent;

public class BatchComponentTest extends TestBase {

	@Test
	public void 正常系_営業日_バッチテスト() {
		BatchComponent batchComponent = new BatchComponent();
		// 認証処理
		auth();

		try {
			// 非営業日カレンダーマスタに20191028が共通の非営業日として定義されていないこと
			if (!batchComponent.execute(new String[] { "20191028" })) {
				Assert.fail("非営業日として判定された。");
			}
		} catch (Exception e) {
			Assert.fail("エラーが発生した。");
		}

		try {
			// スラッシュ区切りでも営業日判定可能であること
			if (!batchComponent.execute(new String[] { "2019/10/28" })) {
				Assert.fail("非営業日として判定された。");
			}
		} catch (Exception e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void 正常系_非営業日_共通_バッチテスト() {
		BatchComponent batchComponent = new BatchComponent();
		// 認証処理
		auth();

		try {
			// 非営業日カレンダーマスタに20191027が共通の非営業日として定義されていること
			if (batchComponent.execute(new String[] { "20191027" })) {
				Assert.fail("営業日として判定された。");
			}
		} catch (Exception e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void 正常系_非営業日_ベンダ固有_バッチテスト() {
		BatchComponent batchComponent = new BatchComponent();
		// 認証処理
		auth();

		try {
			// 非営業日カレンダーマスタに20191025がベンダ固有の非営業日として定義されていること
			if (!batchComponent.execute(new String[] { "20191025" })) {
				Assert.fail("非営業日として判定された。");
			}
		} catch (Exception e) {
			Assert.fail("エラーが発生した。");
		}

		try {
			// スラッシュ区切りでも営業日判定可能であること
			if (!batchComponent.execute(new String[] { "2019/10/25" })) {
				Assert.fail("非営業日として判定された。");
			}
		} catch (Exception e) {
			Assert.fail("エラーが発生した。");
		}
	}

	@Test
	public void 異常系_標準_パラメーター不正() {
		BatchComponent batchComponent = new BatchComponent();

		try {
			batchComponent.execute(new String[] { "dummy" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00032", messageInfo.get(0).getErrorId());
			Assert.assertEquals("処理年月日のフォーマットはyyyyMMddです。", messageInfo.get(0).getErrorMessage());
		}

		try {
			batchComponent.execute(new String[] { "a20201028" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00032", messageInfo.get(0).getErrorId());
			Assert.assertEquals("処理年月日のフォーマットはyyyyMMddです。", messageInfo.get(0).getErrorMessage());
		}
	}
}
