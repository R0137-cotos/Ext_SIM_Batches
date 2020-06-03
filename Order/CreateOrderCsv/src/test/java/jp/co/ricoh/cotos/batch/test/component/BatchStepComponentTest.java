package jp.co.ricoh.cotos.batch.test.component;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.component.RestApiClient;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.CreateOrderCsvDataDto;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchStepComponentTest extends TestBase {

	static ConfigurableApplicationContext context;

	final private String outputPath = "output/";

	@MockBean
	RestApiClient restApiClient;

	@SpyBean(name = "BASE")
	BatchStepComponent batchStepComponent;

	@Autowired
	public void injectContext(ConfigurableApplicationContext injectContext) {
		context = injectContext;
		context.getBean(DBConfig.class).clearData();
	}

	@AfterClass
	public static void exit() throws Exception {
		if (null != context) {
			context.getBean(DBConfig.class).clearData();
			context.stop();
		}
	}

	private void fileDeleate(String pathname) {
		File file = new File(pathname);

		if (!file.exists()) {
			System.out.println("ファイル:[" + pathname + "]が存在しません");
			return;
		}
		if (file.delete()) {
			System.out.println("ファイル:[" + pathname + "]の削除に成功しました");
			return;
		}
		System.out.println("ファイル:[" + pathname + "]の削除に失敗しました");
	}

	@Test
	public void 異常系_パラメータチェックテスト_パラメーター数不一致() throws Exception {
		fileDeleate(outputPath + "result_initial.csv");
		try {
			batchStepComponent.paramCheck(new String[] { "dummy", "dummy" });
			Assert.fail("正常終了");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00001", messageInfo.get(0).getErrorId());
			Assert.assertEquals("パラメータ「処理年月日/ディレクトリ名/ファイル名/種別」が設定されていません。", messageInfo.get(0).getErrorMessage());
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 異常系_パラメータチェックテスト_業務日付不正() throws Exception {
		fileDeleate(outputPath + "result_initial.csv");
		try {
			batchStepComponent.paramCheck(new String[] { "dummy", outputPath, "result_initial.csv", "1" });
			Assert.fail("正常終了");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("RBA00001", messageInfo.get(0).getErrorId());
			Assert.assertEquals("業務日付のフォーマットはyyyyMMddです。", messageInfo.get(0).getErrorMessage());
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 異常系_パラメータチェックテスト_パス不正() throws Exception {
		fileDeleate(outputPath + "result_initial.csv");
		try {
			batchStepComponent.paramCheck(new String[] { "20191018", "dummy", "result_initial.csv", "1" });
			Assert.fail("正常終了");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00110", messageInfo.get(0).getErrorId());
			Assert.assertEquals("指定されたディレクトリが存在しません。", messageInfo.get(0).getErrorMessage());
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 異常系_パラメータチェックテスト_種別不正() throws Exception {
		fileDeleate(outputPath + "result_initial.csv");
		try {
			batchStepComponent.paramCheck(new String[] { "20191018", outputPath, "result_initial.csv", "dummy" });
			Assert.fail("正常終了");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("ROT00003", messageInfo.get(0).getErrorId());
			Assert.assertEquals("種別が特定できません。", messageInfo.get(0).getErrorMessage());
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_パラメーターチェックテスト() {
		fileDeleate(outputPath + "result_initial.csv");
		try {
			batchStepComponent.paramCheck(new String[] { "20191018", outputPath, "result_initial.csv", "1" });
		} catch (Exception e) {
			Assert.fail("エラー");
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_データ取得テスト_新規() throws IOException {
		fileDeleate(outputPath + "result_initial.csv");
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		String contractType = "'$?(@.contractType == \"新規\")'";
		try {
			List<CreateOrderCsvDataDto> csvOrderList = batchStepComponent.getDataList(contractType);
			Assert.assertEquals(9, csvOrderList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_データ取得テスト_容量変更() throws IOException {
		fileDeleate(outputPath + "result_initial.csv");
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessDataCapacityChange.sql");
		String contractType = "'$?(@.contractType == \"容量変更\")'";
		try {
			List<CreateOrderCsvDataDto> csvOrderList = batchStepComponent.getDataList(contractType);
			Assert.assertEquals(8, csvOrderList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_データ取得テスト_有償交換() throws IOException {
		fileDeleate(outputPath + "result_initial.csv");
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessDataPaidExchange.sql");
		String contractType = "'$?(@.contractType == \"有償交換\")'";
		try {
			List<CreateOrderCsvDataDto> csvOrderList = batchStepComponent.getDataList(contractType);
			Assert.assertEquals(8, csvOrderList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		fileDeleate(outputPath + "result_initial.csv");
	}

	@Test
	public void 正常系_データ取得_取得無し() throws IOException {
		fileDeleate(outputPath + "result_initial.csv");
		String contractType = "'$?(@.contractType == \"新規\")'";
		try {
			List<CreateOrderCsvDataDto> csvOrderList = batchStepComponent.getDataList(contractType);
			Assert.assertEquals(0, csvOrderList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		fileDeleate(outputPath + "result_initial.csv");
	}
}