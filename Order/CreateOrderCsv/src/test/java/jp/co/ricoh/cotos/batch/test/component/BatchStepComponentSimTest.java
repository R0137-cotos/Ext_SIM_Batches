package jp.co.ricoh.cotos.batch.test.component;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.fasterxml.jackson.core.JsonProcessingException;

import jp.co.ricoh.cotos.batch.DBConfig;
import jp.co.ricoh.cotos.batch.TestBase;
import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract.ContractType;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractAddedEditorEmp;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractDetail;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractInstallationLocation;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractPicSaEmp;
import jp.co.ricoh.cotos.commonlib.entity.contract.CustomerContract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ProductContract;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractDetailRepository;
import jp.co.ricoh.cotos.component.BatchUtil;
import jp.co.ricoh.cotos.component.RestApiClient;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.CreateOrderCsvDataDto;
import jp.co.ricoh.cotos.dto.CreateOrderCsvDto;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchStepComponentSimTest extends TestBase {

	static ConfigurableApplicationContext context;

	final private String outputPath = "output/";

	@MockBean
	RestApiClient restApiClient;

	@SpyBean(name = "SIM")
	BatchStepComponent batchStepComponent;

	@SpyBean
	BatchUtil batchUtil;

	@SpyBean
	DBUtil dbUtil;

	@Autowired
	ContractDetailRepository contractDetailRepository;

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
	public void 正常系_オーダーCSV作成_新規_業務区登記簿コピー添付無し() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191018 ← 非営業日カレンダーマスタに存在しないことを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1L);
		createOrderCsvDataDto.setContractNumber("CC2019122400101");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/initial_one.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_業務区登記簿コピー添付あり_ワークフロー状態が作業完了でない() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191018 ← 非営業日カレンダーマスタに存在しないことを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(2L);
		createOrderCsvDataDto.setContractIdTemp(2L);
		createOrderCsvDataDto.setContractNumber("CC2019122400102");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}

		File csvFile = Paths.get(outputPath, outputFileName).toFile();
		Assert.assertFalse("オーダーCSVが出力されていないこと。", csvFile.exists());
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_業務区登記簿コピー添付あり_ワークフロー状態が作業完了() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191018 ← 非営業日カレンダーマスタに存在しないことを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(3L);
		createOrderCsvDataDto.setContractIdTemp(3L);
		createOrderCsvDataDto.setContractNumber("CC2019122400103");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/expected_Toukibo.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_処理日付以外_共通の非営業日() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191027 ← 非営業日カレンダーマスタに存在することを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191027");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1L);
		createOrderCsvDataDto.setContractNumber("CIC201912240101");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		File csvFile = Paths.get(outputPath, outputFileName).toFile();
		Assert.assertFalse("オーダーCSVが出力されていないこと。", csvFile.exists());
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_最短納期日範囲外() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// ベンダ共通の営業日
		// 20191017 最短納期日範囲外のため、CSV出力されない
		// 20191018 サービス利用希望日 - 8営業日
		// 20191019 非営業日
		// 20191020 非営業日
		// 20191021 サービス利用希望日 - 7営業日
		// 20191022 非営業日
		// 20191023 サービス利用希望日 - 6営業日
		// 20191024 サービス利用希望日 - 5営業日
		// 20191025 サービス利用希望日 - 4営業日
		// 20191026 非営業日
		// 20191027 非営業日
		// 20191028 サービス利用希望日 - 3営業日
		// 20191029 サービス利用希望日 - 2営業日
		// 20191030 サービス利用希望日 - 1営業日
		// 20191031 サービス利用希望日
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191017");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1L);
		createOrderCsvDataDto.setContractNumber("CIC201912240101");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		// ベンダー固有の非営業日無しとする
		createOrderCsvDataDto.setVendorShortName("ZZ");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		File csvFile = Paths.get(outputPath, outputFileName).toFile();
		Assert.assertFalse("オーダーCSVが出力されていないこと。", csvFile.exists());
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_最短納期日範囲確認_SBのみ非営業日を含む() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// SBの営業日
		// 20191017 サービス利用希望日 - 8営業日 ※共通の非営業日マスタだと最短納期日範囲外
		// 20191018 サービス利用希望日 - 7営業日
		// 20191019 非営業日
		// 20191020 非営業日
		// 20191021 サービス利用希望日 - 6営業日
		// 20191022 非営業日
		// 20191023 サービス利用希望日 - 5営業日
		// 20191024 サービス利用希望日 - 4営業日
		// 20191025 SBのみ非営業日
		// 20191026 非営業日
		// 20191027 非営業日
		// 20191028 サービス利用希望日 - 3営業日
		// 20191029 サービス利用希望日 - 2営業日
		// 20191030 サービス利用希望日 - 1営業日
		// 20191031 サービス利用希望日
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191017");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1L);
		createOrderCsvDataDto.setContractNumber("CIC201912240101");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/expected_ShortestDeliveryDate_SB.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_最短納期日範囲確認_複数ベンダー非営業日を含む() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// SCの営業日
		// 20191016 サービス利用希望日 - 8営業日 ※共通の非営業日マスタだと最短納期日範囲外
		// 20191017 サービス利用希望日 - 7営業日
		// 20191018 サービス利用希望日 - 6営業日
		// 20191019 非営業日
		// 20191020 非営業日
		// 20191021 サービス利用希望日 - 5営業日
		// 20191022 非営業日
		// 20191023 SC,SDのみ非営業日
		// 20191024 SC,SDのみ非営業日
		// 20191025 サービス利用希望日 - 4営業日
		// 20191026 非営業日
		// 20191027 非営業日
		// 20191028 サービス利用希望日 - 3営業日
		// 20191029 サービス利用希望日 - 2営業日
		// 20191030 サービス利用希望日 - 1営業日
		// 20191031 サービス利用希望日
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191016");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1L);
		createOrderCsvDataDto.setContractNumber("CIC201912240101");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SC");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/expected_ShortestDeliveryDate_MultiVendor.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_容量変更() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessDataCapacityChange.sql");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191031 ← 処理日当月最終営業日
		// 20191030 ← 処理日当月最終営業日 - 1営業日
		// 20191029 ← 処理日当月最終営業日 - 2営業日(容量変更オーダー対象日)
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191029");
		dto.setType("2");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1);
		createOrderCsvDataDto.setContractNumber("CC2019122400101");
		createOrderCsvDataDto.setContractBranchNumber(1);
		createOrderCsvDataDto.setQuantity("1");
		createOrderCsvDataDto.setRicohItemCode("SI0001");
		createOrderCsvDataDto.setItemContractName("データSIM Type-C 2GB");
		createOrderCsvDataDto.setConclusionPreferredDate(sdFormat.parse("2019-11-01 00:00:00"));
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
		createOrderCsvDataDto.setExtendsParameterIterance("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"容量変更\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"2\",\"serialNumber\":\"22\",\"device\":\"\",\"invoiceNumber\":\"\"}]}");
		createOrderCsvDataDto.setContractDetailId(11L);
		createOrderCsvDataDto.setUpdatedAt(sdFormat.parse("2019-09-27 12:09:10"));
		createOrderCsvDataDto.setContractType(ContractType.契約変更);
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/initial_one_capacity_change.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_容量変更_処理日当月最終営業日マイナス2営業日以内にSBのみ非営業日が含まれる() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessDataCapacityChange.sql");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 納入希望日 = 20191201
		// 20191130 ← 処理日当月最終営業日
		// 20191129 ← 処理日当月最終営業日 - 1営業日 ベンダー略称=SBのみ非営業日
		// 20191128 ← 処理日当月最終営業日 - 2営業日(容量変更オーダー対象日)
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191128");
		dto.setType("2");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1);
		createOrderCsvDataDto.setContractNumber("CC2019122400101");
		createOrderCsvDataDto.setContractBranchNumber(1);
		createOrderCsvDataDto.setQuantity("1");
		createOrderCsvDataDto.setRicohItemCode("SI0001");
		createOrderCsvDataDto.setItemContractName("データSIM Type-C 2GB");
		createOrderCsvDataDto.setConclusionPreferredDate(sdFormat.parse("2019-12-01 00:00:00"));
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
		createOrderCsvDataDto.setExtendsParameterIterance("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"容量変更\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"2\",\"serialNumber\":\"22\",\"device\":\"\",\"invoiceNumber\":\"\"}]}");
		createOrderCsvDataDto.setContractDetailId(11L);
		createOrderCsvDataDto.setUpdatedAt(sdFormat.parse("2019-09-27 12:09:10"));
		createOrderCsvDataDto.setContractType(ContractType.契約変更);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/initial_one_capacity_change_SB.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_容量変更_処理日付以外() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";
		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessDataCapacityChange.sql");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191031 ← 処理日当月最終営業日
		// 20191030 ← 処理日当月最終営業日 - 1営業日
		// 20191029 ← 処理日当月最終営業日 - 2営業日(容量変更オーダー対象日)
		// 20191028 ← 処理日当月最終営業日 - 3営業日
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191028");
		dto.setType("2");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1);
		createOrderCsvDataDto.setContractNumber("CIC201912240101");
		createOrderCsvDataDto.setContractBranchNumber(1);
		createOrderCsvDataDto.setQuantity("1");
		createOrderCsvDataDto.setRicohItemCode("SI0001");
		createOrderCsvDataDto.setItemContractName("データSIM Type-C 2GB");
		createOrderCsvDataDto.setConclusionPreferredDate(sdFormat.parse("2019-11-01 00:00:00"));
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
		createOrderCsvDataDto.setExtendsParameterIterance("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"容量変更\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"2\",\"serialNumber\":\"22\",\"device\":\"\",\"invoiceNumber\":\"\"}]}");
		createOrderCsvDataDto.setContractDetailId(11L);
		createOrderCsvDataDto.setUpdatedAt(sdFormat.parse("2019-09-27 12:09:10"));
		createOrderCsvDataDto.setContractType(ContractType.契約変更);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		File csvFile = Paths.get(outputPath, outputFileName).toFile();
		Assert.assertFalse("オーダーCSVが出力されていないこと。", csvFile.exists());
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_有償交換() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessDataPaidExchange.sql");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191018 ← 非営業日カレンダーマスタに存在しないことを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("3");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1);
		createOrderCsvDataDto.setContractNumber("CC2019122400101");
		createOrderCsvDataDto.setContractBranchNumber(1);
		createOrderCsvDataDto.setQuantity("1");
		createOrderCsvDataDto.setRicohItemCode("SI0001");
		createOrderCsvDataDto.setItemContractName("データSIM Type-C 2GB");
		createOrderCsvDataDto.setConclusionPreferredDate(sdFormat.parse("2019-10-15 00:00:00"));
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
		createOrderCsvDataDto.setExtendsParameterIterance("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"有償交換\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"2\",\"serialNumber\":\"22\",\"device\":\"\",\"invoiceNumber\":\"\"}]}");
		createOrderCsvDataDto.setContractDetailId(11L);
		createOrderCsvDataDto.setUpdatedAt(sdFormat.parse("2018-09-19 12:09:10"));
		createOrderCsvDataDto.setContractType(ContractType.契約変更);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/initial_one_paid_exchange.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_有償交換_SBのみ非営業日() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessDataPaidExchange.sql");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191017 ← 非営業日カレンダーマスタにベンダー略称=SBで存在することを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191017");
		dto.setType("3");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1);
		createOrderCsvDataDto.setContractNumber("CC2019122400101");
		createOrderCsvDataDto.setContractBranchNumber(1);
		createOrderCsvDataDto.setQuantity("1");
		createOrderCsvDataDto.setRicohItemCode("SI0001");
		createOrderCsvDataDto.setItemContractName("データSIM Type-C 2GB");
		createOrderCsvDataDto.setConclusionPreferredDate(sdFormat.parse("2019-10-11 00:00:00"));
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
		createOrderCsvDataDto.setExtendsParameterIterance("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"有償交換\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"2\",\"serialNumber\":\"22\",\"device\":\"\",\"invoiceNumber\":\"\"}]}");
		createOrderCsvDataDto.setContractDetailId(11L);
		createOrderCsvDataDto.setUpdatedAt(sdFormat.parse("2018-09-19 12:09:10"));
		createOrderCsvDataDto.setContractType(ContractType.契約変更);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/initial_one_paid_exchange_SB.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_有償交換_処理日付以外() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessDataPaidExchange.sql");

		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191027 ← 非営業日カレンダーマスタに存在することを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191027");
		dto.setType("3");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1);
		createOrderCsvDataDto.setContractNumber("CIC201912240101");
		createOrderCsvDataDto.setContractBranchNumber(1);
		createOrderCsvDataDto.setQuantity("1");
		createOrderCsvDataDto.setRicohItemCode("SI0001");
		createOrderCsvDataDto.setItemContractName("データSIM Type-C 2GB");
		createOrderCsvDataDto.setConclusionPreferredDate(sdFormat.parse("2019-10-15 00:00:00"));
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
		createOrderCsvDataDto.setExtendsParameterIterance("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"有償交換\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"2\",\"serialNumber\":\"22\",\"device\":\"\",\"invoiceNumber\":\"\"}]}");
		createOrderCsvDataDto.setContractDetailId(11L);
		createOrderCsvDataDto.setUpdatedAt(sdFormat.parse("2018-09-19 12:09:10"));
		createOrderCsvDataDto.setContractType(ContractType.契約変更);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		File csvFile = Paths.get(outputPath, outputFileName).toFile();
		Assert.assertFalse("オーダーCSVが出力されていないこと。", csvFile.exists());
		fileDeleate(outputPath + outputFileName);
	}

	@Ignore
	@Test
	// csv書き込み処理(ネストした処理)をmock化することができなかったため、次の行に「throw new
	// Exception()」を書いて、Exceptionを発生させて動作検証を行った。
	// 検証のためには記載の通り実装を一部改変する必要があるためIgnoreしている。
	public void 異常系_csv出力エラーデータ有_エラーデータの後処理正常終了() throws ParseException, IOException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");

		// バッチ起動引数
		// 20191018 ← 非営業日カレンダーマスタに存在しないことを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		List<CreateOrderCsvDataDto> orderDataList = テストデータ作成();

		boolean success = false;
		try {
			success = batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			Assert.fail("異常終了");
		}
		if (success) {
			Assert.fail("処理が正常に終了してしまった。");
		} else {
			Assert.assertTrue("意図した通りエラーが発生した。", true);
			ContractDetail contractDetail = contractDetailRepository.findOne(11L);
			Assert.assertEquals("拡張項目が更新されていること", "{\"orderCsvCreationStatus\":\"2\",\"orderCsvCreationDate\":\"\"}", contractDetail.getExtendsParameter());
		}
	}

	@Ignore
	@Test
	// csv書き込み処理(ネストした処理)をmock化することができなかったため、次の行に「throw new
	// Exception()」を書いて、Exceptionを発生させて動作検証を行った。
	// 検証のためには記載の通り実装を一部改変する必要があるためIgnoreしている。
	public void 異常系_csv出力エラーデータ有_エラーデータの後処理異常終了() throws ParseException, IOException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");

		// モック
		Mockito.doThrow(new RuntimeException()).when(dbUtil).execute(Mockito.any(), Mockito.any());

		// バッチ起動引数
		// 20191018 ← 非営業日カレンダーマスタに存在しないことを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		List<CreateOrderCsvDataDto> orderDataList = テストデータ作成();

		boolean success = false;
		try {
			success = batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			Assert.fail("異常終了");
		}
		if (success) {
			Assert.fail("処理が正常に終了してしまった。");
		} else {
			Assert.assertTrue("意図した通りエラーが発生した。", true);
		}
	}

	@Test
	public void 異常系_オーダーCSV作成状態取得処理失敗() throws JsonProcessingException, IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");

		// モック
		Mockito.doThrow(new IOException()).when(batchUtil).getOrderCsvCreationStatus(Mockito.any());

		// バッチ起動引数
		// 20191018 ← 非営業日カレンダーマスタに存在しないことを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		List<CreateOrderCsvDataDto> orderDataList = テストデータ作成();

		boolean success = false;
		try {
			success = batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			Assert.fail("異常終了");
		}
		if (success) {
			Assert.fail("処理が正常に終了してしまった。");
		} else {
			Assert.assertTrue("意図した通りエラーが発生した。", true);
		}
	}

	@Test
	public void 異常系_正常データの後処理失敗_契約更新エラー() throws ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");

		// モック
		doNothing().when(restApiClient).callAssignWorker(Mockito.any());
		doNothing().when(restApiClient).callAcceptWorkApi(Mockito.any());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenThrow(new RuntimeException());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191018 ← 非営業日カレンダーマスタに存在しないことを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		List<CreateOrderCsvDataDto> orderDataList = テストデータ作成();

		boolean success = false;
		try {
			success = batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		if (success) {
			Assert.fail("処理が正常に終了してしまった。");
		} else {
			Assert.assertTrue("意図した通りエラーが発生した。", true);
		}
	}

	@Test
	public void 異常系_正常データの後処理失敗_契約詳細取得エラー() throws ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");

		// モック
		doNothing().when(restApiClient).callAssignWorker(Mockito.any());
		doNothing().when(restApiClient).callAcceptWorkApi(Mockito.any());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		Mockito.doThrow(new RuntimeException()).when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191018 ← 非営業日カレンダーマスタに存在しないことを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		List<CreateOrderCsvDataDto> orderDataList = テストデータ作成();

		boolean success = false;
		try {
			success = batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		if (success) {
			Assert.fail("処理が正常に終了してしまった。");
		} else {
			Assert.assertTrue("意図した通りエラーが発生した。", true);
		}
	}

	@Test
	public void 異常系_正常データの後処理失敗_手配関連項目更新エラー() throws ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");

		// モック
		Mockito.doThrow(new RuntimeException()).when(restApiClient).callAssignWorker(Mockito.any());
		Mockito.doThrow(new RuntimeException()).when(restApiClient).callAcceptWorkApi(Mockito.any());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("2", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191018 ← 非営業日カレンダーマスタに存在しないことを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		List<CreateOrderCsvDataDto> orderDataList = テストデータ作成();

		boolean success = false;
		try {
			success = batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		if (success) {
			Assert.fail("処理が正常に終了してしまった。");
		} else {
			Assert.assertTrue("意図した通りエラーが発生した。", true);
		}
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_業務区登記簿コピー添付あり_ワークフロー状態が作業完了_取得項目値が1() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("1", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191018 ← 非営業日カレンダーマスタに存在しないことを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(3L);
		createOrderCsvDataDto.setContractIdTemp(3L);
		createOrderCsvDataDto.setContractNumber("CC2019122400103");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/expected_Toukibo.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_業務区登記簿コピー添付あり_ワークフロー状態が作業完了_取得項目値が3日以上() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("3日以上", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191018 ← 非営業日カレンダーマスタに存在しないことを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(3L);
		createOrderCsvDataDto.setContractIdTemp(3L);
		createOrderCsvDataDto.setContractNumber("CC2019122400103");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/expected_Toukibo.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_業務区登記簿コピー添付あり_ワークフロー状態が作業完了_取得項目値が不能() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("不能", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191018 ← 非営業日カレンダーマスタに存在しないことを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(3L);
		createOrderCsvDataDto.setContractIdTemp(3L);
		createOrderCsvDataDto.setContractNumber("CC2019122400103");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/expected_Toukibo.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_業務区登記簿コピー添付あり_ワークフロー状態が作業完了_取得項目値が離島は問合せ() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("離島は問合せ", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191018 ← 非営業日カレンダーマスタに存在しないことを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(3L);
		createOrderCsvDataDto.setContractIdTemp(3L);
		createOrderCsvDataDto.setContractNumber("CC2019122400103");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/expected_Toukibo.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_業務区登記簿コピー添付あり_ワークフロー状態が作業完了_手入力が存在した場合() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("1", true)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// バッチ起動引数
		// 20191018 ← 非営業日カレンダーマスタに存在しないことを想定
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(3L);
		createOrderCsvDataDto.setContractIdTemp(3L);
		createOrderCsvDataDto.setContractNumber("CC2019122400103");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/expected_Toukibo.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_最短納期日範囲外_取得項目値が1() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("1", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// ベンダ共通の営業日
		// 20191018 最短納期日範囲外のため、CSV出力されない
		// 20191019 非営業日
		// 20191020 非営業日
		// 20191021 サービス利用希望日 - 7営業日(8営業日 -1)
		// 20191022 非営業日
		// 20191023 サービス利用希望日 - 6営業日
		// 20191024 サービス利用希望日 - 5営業日
		// 20191025 サービス利用希望日 - 4営業日
		// 20191026 非営業日
		// 20191027 非営業日
		// 20191028 サービス利用希望日 - 3営業日
		// 20191029 サービス利用希望日 - 2営業日
		// 20191030 サービス利用希望日 - 1営業日
		// 20191031 サービス利用希望日
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1L);
		createOrderCsvDataDto.setContractNumber("CIC201912240101");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		// ベンダー固有の非営業日無しとする
		createOrderCsvDataDto.setVendorShortName("ZZ");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		File csvFile = Paths.get(outputPath, outputFileName).toFile();
		Assert.assertFalse("オーダーCSVが出力されていないこと。", csvFile.exists());
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_最短納期日範囲外_取得項目値が3日以上() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("3日以上", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// ベンダ共通の営業日
		// 20191016 最短納期日範囲外のため、CSV出力されない
		// 20191017 サービス利用希望日 - 8営業日 +1
		// 20191018 サービス利用希望日 - 8営業日
		// 20191019 非営業日
		// 20191020 非営業日
		// 20191021 サービス利用希望日 - 7営業日
		// 20191022 非営業日
		// 20191023 サービス利用希望日 - 6営業日
		// 20191024 サービス利用希望日 - 5営業日
		// 20191025 サービス利用希望日 - 4営業日
		// 20191026 非営業日
		// 20191027 非営業日
		// 20191028 サービス利用希望日 - 3営業日
		// 20191029 サービス利用希望日 - 2営業日
		// 20191030 サービス利用希望日 - 1営業日
		// 20191031 サービス利用希望日
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191016");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1L);
		createOrderCsvDataDto.setContractNumber("CIC201912240101");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		// ベンダー固有の非営業日無しとする
		createOrderCsvDataDto.setVendorShortName("ZZ");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		File csvFile = Paths.get(outputPath, outputFileName).toFile();
		Assert.assertFalse("オーダーCSVが出力されていないこと。", csvFile.exists());
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_最短納期日範囲外_取得項目値が不能() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("不能", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// ベンダ共通の営業日
		// 20191016 最短納期日範囲外のため、CSV出力されない
		// 20191017 サービス利用希望日 - 8営業日 +1
		// 20191018 サービス利用希望日 - 8営業日
		// 20191019 非営業日
		// 20191020 非営業日
		// 20191021 サービス利用希望日 - 7営業日
		// 20191022 非営業日
		// 20191023 サービス利用希望日 - 6営業日
		// 20191024 サービス利用希望日 - 5営業日
		// 20191025 サービス利用希望日 - 4営業日
		// 20191026 非営業日
		// 20191027 非営業日
		// 20191028 サービス利用希望日 - 3営業日
		// 20191029 サービス利用希望日 - 2営業日
		// 20191030 サービス利用希望日 - 1営業日
		// 20191031 サービス利用希望日
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191016");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1L);
		createOrderCsvDataDto.setContractNumber("CIC201912240101");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		// ベンダー固有の非営業日無しとする
		createOrderCsvDataDto.setVendorShortName("ZZ");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		File csvFile = Paths.get(outputPath, outputFileName).toFile();
		Assert.assertFalse("オーダーCSVが出力されていないこと。", csvFile.exists());
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_最短納期日範囲外_取得項目値が離島は問合せ() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("離島は問合せ", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// ベンダ共通の営業日
		// 20191016 最短納期日範囲外のため、CSV出力されない
		// 20191017 サービス利用希望日 - 8営業日 +1
		// 20191018 サービス利用希望日 - 8営業日
		// 20191019 非営業日
		// 20191020 非営業日
		// 20191021 サービス利用希望日 - 7営業日
		// 20191022 非営業日
		// 20191023 サービス利用希望日 - 6営業日
		// 20191024 サービス利用希望日 - 5営業日
		// 20191025 サービス利用希望日 - 4営業日
		// 20191026 非営業日
		// 20191027 非営業日
		// 20191028 サービス利用希望日 - 3営業日
		// 20191029 サービス利用希望日 - 2営業日
		// 20191030 サービス利用希望日 - 1営業日
		// 20191031 サービス利用希望日
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191016");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1L);
		createOrderCsvDataDto.setContractNumber("CIC201912240101");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		// ベンダー固有の非営業日無しとする
		createOrderCsvDataDto.setVendorShortName("ZZ");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		File csvFile = Paths.get(outputPath, outputFileName).toFile();
		Assert.assertFalse("オーダーCSVが出力されていないこと。", csvFile.exists());
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_最短納期日範囲確認_SBのみ非営業日を含む_取得項目値が1() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("1", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// SBの営業日
		// 20191018 サービス利用希望日 - 7営業日 (8営業日 -1)※共通の非営業日マスタだと最短納期日範囲外
		// 20191019 非営業日
		// 20191020 非営業日
		// 20191021 サービス利用希望日 - 6営業日
		// 20191022 非営業日
		// 20191023 サービス利用希望日 - 5営業日
		// 20191024 サービス利用希望日 - 4営業日
		// 20191025 SBのみ非営業日
		// 20191026 非営業日
		// 20191027 非営業日
		// 20191028 サービス利用希望日 - 3営業日
		// 20191029 サービス利用希望日 - 2営業日
		// 20191030 サービス利用希望日 - 1営業日
		// 20191031 サービス利用希望日
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191018");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1L);
		createOrderCsvDataDto.setContractNumber("CIC201912240101");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/expected_ShortestDeliveryDate_SB-1.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_最短納期日範囲確認_SBのみ非営業日を含む_取得項目値が3日以上() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("3日以上", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// SBの営業日
		// 20191016 サービス利用希望日 - 8営業日 +1 ※共通の非営業日マスタだと最短納期日範囲外
		// 20191017 サービス利用希望日 - 8営業日
		// 20191018 サービス利用希望日 - 7営業日
		// 20191019 非営業日
		// 20191020 非営業日
		// 20191021 サービス利用希望日 - 6営業日
		// 20191022 非営業日
		// 20191023 サービス利用希望日 - 5営業日
		// 20191024 サービス利用希望日 - 4営業日
		// 20191025 SBのみ非営業日
		// 20191026 非営業日
		// 20191027 非営業日
		// 20191028 サービス利用希望日 - 3営業日
		// 20191029 サービス利用希望日 - 2営業日
		// 20191030 サービス利用希望日 - 1営業日
		// 20191031 サービス利用希望日
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191016");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1L);
		createOrderCsvDataDto.setContractNumber("CIC201912240101");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/expected_ShortestDeliveryDate_SB+1.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_最短納期日範囲確認_SBのみ非営業日を含む_取得項目値が不能() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("不能", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// SBの営業日
		// 20191016 サービス利用希望日 - 8営業日 +1 ※共通の非営業日マスタだと最短納期日範囲外
		// 20191017 サービス利用希望日 - 8営業日
		// 20191018 サービス利用希望日 - 7営業日
		// 20191019 非営業日
		// 20191020 非営業日
		// 20191021 サービス利用希望日 - 6営業日
		// 20191022 非営業日
		// 20191023 サービス利用希望日 - 5営業日
		// 20191024 サービス利用希望日 - 4営業日
		// 20191025 SBのみ非営業日
		// 20191026 非営業日
		// 20191027 非営業日
		// 20191028 サービス利用希望日 - 3営業日
		// 20191029 サービス利用希望日 - 2営業日
		// 20191030 サービス利用希望日 - 1営業日
		// 20191031 サービス利用希望日
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191016");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1L);
		createOrderCsvDataDto.setContractNumber("CIC201912240101");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/expected_ShortestDeliveryDate_SB+1.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	@Test
	public void 正常系_オーダーCSV作成_新規_最短納期日範囲確認_SBのみ非営業日を含む_取得項目値が離島は問合せ() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());
		Mockito.doReturn(ContractInstallationLocationMock("離島は問合せ", false)).when(batchUtil).findContractInstallationLocation(Mockito.anyLong());

		// SBの営業日
		// 20191016 サービス利用希望日 - 8営業日 +1 ※共通の非営業日マスタだと最短納期日範囲外
		// 20191017 サービス利用希望日 - 8営業日
		// 20191018 サービス利用希望日 - 7営業日
		// 20191019 非営業日
		// 20191020 非営業日
		// 20191021 サービス利用希望日 - 6営業日
		// 20191022 非営業日
		// 20191023 サービス利用希望日 - 5営業日
		// 20191024 サービス利用希望日 - 4営業日
		// 20191025 SBのみ非営業日
		// 20191026 非営業日
		// 20191027 非営業日
		// 20191028 サービス利用希望日 - 3営業日
		// 20191029 サービス利用希望日 - 2営業日
		// 20191030 サービス利用希望日 - 1営業日
		// 20191031 サービス利用希望日
		CreateOrderCsvDto dto = new CreateOrderCsvDto();
		dto.setCsvFile(Paths.get(outputPath + outputFileName).toFile());
		dto.setTmpFile(Paths.get(outputPath + tempFileName).toFile());
		dto.setOperationDate("20191016");
		dto.setType("1");

		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1L);
		createOrderCsvDataDto.setContractNumber("CIC201912240101");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);
		try {
			batchStepComponent.process(dto, orderDataList);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("異常終了");
		}
		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + outputFileName));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/expected/expected_ShortestDeliveryDate_SB+1.csv"));
		Assert.assertArrayEquals(expected, actuals);
		fileDeleate(outputPath + outputFileName);
	}

	private List<ContractDetail> getContractDetailList() {
		List<ContractDetail> contractDetailList = new ArrayList<ContractDetail>();
		ContractDetail contractDetail = new ContractDetail();
		contractDetail.setId(1L);
		contractDetailList.add(contractDetail);
		return contractDetailList;
	}

	private Contract dummyContract() {
		Contract contract = new Contract();
		contract.setId(1L);
		contract.setContractDetailList(getContractDetailList());
		contract.setEstimationNumber("testEstimationNumber");
		ContractPicSaEmp contractPicSaEmp = new ContractPicSaEmp();
		contractPicSaEmp.setMailAddress("testSaEmp@example.com");
		contract.setContractPicSaEmp(contractPicSaEmp);
		ContractAddedEditorEmp contractAddedEditorEmp = new ContractAddedEditorEmp();
		contractAddedEditorEmp.setMailAddress("testAddedEditor@example.com");
		contract.setContractAddedEditorEmpList(Arrays.asList(contractAddedEditorEmp));
		ProductContract productContract = new ProductContract();
		productContract.setProductContractName("testProductContractName");
		contract.setProductContractList(Arrays.asList(productContract));
		CustomerContract customerContract = new CustomerContract();
		customerContract.setCompanyName("testCompanyName");
		contract.setCustomerContract(customerContract);
		return contract;
	}

	private List<CreateOrderCsvDataDto> テストデータ作成() throws ParseException {
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		CreateOrderCsvDataDto createOrderCsvDataDto = new CreateOrderCsvDataDto();
		createOrderCsvDataDto.setId(1L);
		createOrderCsvDataDto.setContractIdTemp(1L);
		createOrderCsvDataDto.setContractNumber("CC2019122400101");
		createOrderCsvDataDto.setContractBranchNumber(1);
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
		createOrderCsvDataDto.setContractType(ContractType.新規);
		createOrderCsvDataDto.setVendorShortName("SB");
		List<CreateOrderCsvDataDto> orderDataList = new ArrayList<CreateOrderCsvDataDto>();
		orderDataList.add(createOrderCsvDataDto);

		return orderDataList;
	}

	/**
	 * 設置先（契約用）モック
	 * @param wantSagawaCodeColumnF
	 * @param isInputPostNumber
	 * @return
	 */
	private ContractInstallationLocation ContractInstallationLocationMock(String wantSagawaCodeColumnF, boolean isInputPostNumber) {
		ContractInstallationLocation contractInstallationLocation = new ContractInstallationLocation();
		contractInstallationLocation.setId(1);

		// 9633602 1
		// 6890535 2
		// 9020067 3日以上
		// 1000104 離島は問合せ
		// 9750000 不能
		switch (wantSagawaCodeColumnF) {
		case "1":
			contractInstallationLocation.setPostNumber("9633602");
			break;
		case "2":
			contractInstallationLocation.setPostNumber("6890535");
			break;
		case "3日以上":
			contractInstallationLocation.setPostNumber("9020067");
			break;
		case "離島は問合せ":
			contractInstallationLocation.setPostNumber("1000104");
			break;
		case "不能":
			contractInstallationLocation.setPostNumber("9750000");
			break;
		}
		// 0070834 2
		if (isInputPostNumber) {
			contractInstallationLocation.setInputPostNumber("007-0834");
		}

		return contractInstallationLocation;
	}
}