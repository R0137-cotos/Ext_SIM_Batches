package jp.co.ricoh.cotos.batch.test.component;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import jp.co.ricoh.cotos.commonlib.logic.businessday.BusinessDayUtil;
import jp.co.ricoh.cotos.component.RestApiClient;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.CreateOrderCsvDataDto;
import jp.co.ricoh.cotos.util.OperationDateException;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class BatchStepComponentTest extends TestBase {

	static ConfigurableApplicationContext context;

	final private String outputPath = "output/";

	@MockBean
	RestApiClient restApiClient;

	@SpyBean(name = "BASE")
	BatchStepComponent batchStepComponent;

	@SpyBean
	BusinessDayUtil businessDayUtil;

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

	@Test
	public void 異常系_パラメータチェックテスト_パラメーター数不一致() throws Exception {
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
	}

	@Test
	public void 異常系_パラメータチェックテスト_業務日付不正() throws Exception {
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
	}

	@Test
	public void 異常系_パラメータチェックテスト_パス不正() throws Exception {
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
	}

	@Test
	public void 異常系_パラメータチェックテスト_種別不正() throws Exception {
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
	}

	@Test
	public void 正常系_パラメーターチェックテスト() {
		try {
			batchStepComponent.paramCheck(new String[] { "20191018", outputPath, "result_initial.csv", "1" });
		} catch (Exception e) {
			Assert.fail("エラー");
		}
	}

	@Test
	public void 異常系_マスタに存在しない営業日が設定された() {
		Mockito.doReturn(null).when(businessDayUtil).getLastBusinessDayOfTheMonthFromNonBusinessCalendarMaster(Mockito.any());
		try {
			batchStepComponent.paramCheck(new String[] { "20990627", outputPath, "result_initial.csv", "2" });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (ErrorCheckException e) {
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals("RBA00009", messageInfo.get(0).getErrorId());
			Assert.assertEquals("営業日APIによる月末最終営業日取得の取得に失敗しました。", messageInfo.get(0).getErrorMessage());
		} catch (Exception e) {
			Assert.fail("意図しないエラーが発生した。");
		}
	}

	@Test
	public void 異常系_実施日例外発生() {
		try {
			batchStepComponent.paramCheck(new String[] { "20190627", outputPath, "result_initial.csv", "2" });
			Assert.fail("処理日不正で処理が実行された。");
		} catch (OperationDateException e) {
			Assert.assertTrue("意図した通りエラーが発生した。", true);
		} catch (Exception e) {
			Assert.fail("意図しないエラーが発生した。");
		}
	}

	@Ignore
	@Test
	// 一時的にoutputディレクトリにresult_initial.csvを用意して実行
	public void 異常系_ファイルが既に存在するエラー_result_initial() {
		try {
			batchStepComponent.paramCheck(new String[] { "20191018", outputPath, "result_initial.csv", "1" });
		} catch (FileAlreadyExistsException e) {
			Assert.assertTrue("意図した通りエラーが発生した。", true);
		} catch (Exception e) {
			Assert.fail("意図しないエラーが発生した。");
		}
	}

	@Ignore
	@Test
	// 一時的にoutputディレクトリにtemp.csvを用意して実行
	public void 異常系_ファイルが既に存在するエラー_temp() {
		try {
			batchStepComponent.paramCheck(new String[] { "20191018", outputPath, "result_initial.csv", "1" });
		} catch (FileAlreadyExistsException e) {
			Assert.assertTrue("意図した通りエラーが発生した。", true);
		} catch (Exception e) {
			Assert.fail("意図しないエラーが発生した。");
		}
	}

	@Test
	public void 正常系_データ取得テスト_新規() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		String contractType = "'$.extendsParameterList?(@.contractType == \"新規\")'";
		try {
			List<CreateOrderCsvDataDto> csvOrderList = batchStepComponent.getDataList(contractType);
			Assert.assertEquals(9, csvOrderList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
	}

	@Test
	public void 正常系_データ取得テスト_新規_合計数量16を超える() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("create17Orders.sql");
		String contractType = "'$.extendsParameterList?(@.contractType == \"新規\")'";
		try {
			List<CreateOrderCsvDataDto> csvOrderList = batchStepComponent.getDataList(contractType);
			Assert.assertEquals(2, csvOrderList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
	}

	@Test
	public void 正常系_データ取得テスト_容量変更() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessDataCapacityChange.sql");
		String contractType = "'$.extendsParameterList?(@.contractType == \"容量変更\")'";
		try {
			List<CreateOrderCsvDataDto> csvOrderList = batchStepComponent.getDataList(contractType);
			Assert.assertEquals(8, csvOrderList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
	}

	@Test
	public void 正常系_データ取得テスト_容量変更_合計数量16を超える() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("create17Orders.sql");
		String contractType = "'$.extendsParameterList?(@.contractType == \"容量変更\")'";
		try {
			List<CreateOrderCsvDataDto> csvOrderList = batchStepComponent.getDataList(contractType);
			Assert.assertEquals(2, csvOrderList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
	}

	@Test
	public void 正常系_データ取得テスト_有償交換() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessDataPaidExchange.sql");
		String contractType = "'$.extendsParameterList?(@.contractType == \"有償交換\")'";
		try {
			List<CreateOrderCsvDataDto> csvOrderList = batchStepComponent.getDataList(contractType);
			Assert.assertEquals(8, csvOrderList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
	}

	@Test
	public void 正常系_データ取得テスト_有償交換_合計数量16を超える() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("create17Orders.sql");
		String contractType = "'$.extendsParameterList?(@.contractType == \"有償交換\")'";
		try {
			List<CreateOrderCsvDataDto> csvOrderList = batchStepComponent.getDataList(contractType);
			Assert.assertEquals(2, csvOrderList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
	}

	@Test
	public void 正常系_データ取得_取得無し() throws IOException {
		String contractType = "'$.extendsParameterList?(@.contractType == \"新規\")'";
		try {
			List<CreateOrderCsvDataDto> csvOrderList = batchStepComponent.getDataList(contractType);
			Assert.assertEquals(0, csvOrderList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
	}
}