package jp.co.ricoh.cotos.batch.test.logic;

import org.junit.Assert;
import org.junit.Test;

import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.logic.JobComponent;

public class JobComponentTest extends TestBase {

	@Test
	public void 正常系_営業日_ジョブテスト() {
		JobComponent jobComponent = new JobComponent();
		// 認証処理
		auth();

		try {
			// 非営業日カレンダーマスタに20191028が共通の非営業日として定義されていないこと
			jobComponent.run(new String[] { "20191028" });
		} catch (ExitException e) {
			Assert.fail("非営業日として判定された。");
		}

		try {
			// スラッシュ区切りでも営業日判定可能であること
			jobComponent.run(new String[] { "2019/10/28" });
		} catch (ExitException e) {
			Assert.fail("非営業日として判定された。");
		}
	}

	@Test
	public void 正常系_非営業日_共通_ジョブテスト() {
		JobComponent jobComponent = new JobComponent();
		// 認証処理
		auth();

		try {
			// 非営業日カレンダーマスタに20191027が共通の非営業日として定義されていること
			jobComponent.run(new String[] { "20191027" });
			Assert.fail("営業日として判定された。");
		} catch (ExitException e) {
			Assert.assertEquals("ステータス", 1, e.getStatus());
		}
	}

	@Test
	public void 正常系_非営業日_ベンダ固有_ジョブテスト() {
		JobComponent jobComponent = new JobComponent();
		// 認証処理
		auth();

		try {
			// 非営業日カレンダーマスタに20191025がベンダ固有の非営業日として定義されていること
			jobComponent.run(new String[] { "20191025" });
		} catch (ExitException e) {
			Assert.fail("非営業日として判定された。");
		}

		try {
			// スラッシュ区切りでも営業日判定可能であること
			jobComponent.run(new String[] { "2019/10/25" });
		} catch (ExitException e) {
			Assert.fail("非営業日として判定された。");
		}
	}

	@Test
	public void 異常系_標準_パラメーター数不一致() {
		JobComponent jobComponent = new JobComponent();
		try {
			jobComponent.run(new String[] { "dummy", "dummy" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ステータス", 1, e.getStatus());
		}
	}

	@Test
	public void 異常系_標準_パラメーター不正() {
		JobComponent jobComponent = new JobComponent();
		try {
			jobComponent.run(new String[] { "dummy" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ステータス", 1, e.getStatus());
		}

		try {
			jobComponent.run(new String[] { "a20201028" });
			Assert.fail("パラメータが不正なのに処理が実行された。");
		} catch (ExitException e) {
			Assert.assertEquals("ステータス", 1, e.getStatus());
		}
	}
}
