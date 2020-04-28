package jp.co.ricoh.cotos.batch.test.logic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.CreateOrderCsvDataDto;
import jp.co.ricoh.cotos.dto.CreateOrderCsvDto;
import jp.co.ricoh.cotos.logic.BatchComponent;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchComponentTest extends TestBase {

	static ConfigurableApplicationContext context;

	final private String outputPath = "output/";

	@SpyBean(name = "BASE")
	BatchStepComponent batchStepComponent;

	@Autowired
	BatchComponent batchComponent;

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
	public void 正常系_データ取得テスト() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		String contractType = "'$?(@.contractType == \"新規\")'";
		try {
			List<CreateOrderCsvDataDto> serchMailTargetDtoList = batchStepComponent.getDataList(contractType);
			Assert.assertEquals(9, serchMailTargetDtoList.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void 正常系_データ取得_取得無し() throws IOException {
		String contractType = "'$?(@.contractType == \"新規\")'";
		try {
			List<CreateOrderCsvDataDto> serchMailTargetDtoList = batchStepComponent.getDataList(contractType);
			Assert.assertEquals(0, serchMailTargetDtoList.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void 正常系_オーダーCSV作成() throws IOException, ParseException {
		fileDeleate(outputPath + "result_initial.csv");
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get("output\\result_initial.csv").toFile());
		dto.setTmpFile(Paths.get("output\\temp.csv").toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1);
		createOrderCsvDataDto.setContractNumber("CIC201912240101");
		createOrderCsvDataDto.setQuantity("1");
		createOrderCsvDataDto.setRicohItemCode("SI0001");
		createOrderCsvDataDto.setItemContractName("データSIM Type-C 2GB");
		createOrderCsvDataDto.setConclusionPreferredDate(sdFormat.parse("2019-10-31 00:00:00"));
		createOrderCsvDataDto.setShortestDeliveryDate(8);
		createOrderCsvDataDto.setPicName("dummy_pic_name_location");
		createOrderCsvDataDto.setPicNameKana("dummy_pic_name_kana_location");
		createOrderCsvDataDto.setPostNumber("dummy_post_number_location");
		createOrderCsvDataDto.setAddress("dummy_address_location");
		createOrderCsvDataDto.setCompanyName("dummy_company_name_location");
		createOrderCsvDataDto.setOfficeName("dummy_office_name_locationdummy_pic_dept_name_location");
		createOrderCsvDataDto.setPicPhoneNumber("dummy_pic_phone_number_location");
		createOrderCsvDataDto.setPicFaxNumber("dummy_pic_fax_number_location");
		createOrderCsvDataDto.setPicMailAddress("dummy_mail_address@xx.xx");
		createOrderCsvDataDto.setExtendsParameter("{\"orderCsvCreationStatus\":\"0\",\"orderCsvCreationDate\":\"\"}");
		createOrderCsvDataDto.setContractDetailId(11L);
		createOrderCsvDataDto.setUpdatedAt(sdFormat.parse("2018-09-19 12:09:10"));
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fileDeleate(outputPath + "result_initial.csv");
	}
}