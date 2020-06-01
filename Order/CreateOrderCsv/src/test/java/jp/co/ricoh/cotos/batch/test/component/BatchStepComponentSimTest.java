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
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractAddedEditorEmp;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractDetail;
import jp.co.ricoh.cotos.commonlib.entity.contract.ContractPicSaEmp;
import jp.co.ricoh.cotos.commonlib.entity.contract.CustomerContract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ProductContract;
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
	public void 正常系_オーダーCSV作成_新規() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());

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
	public void 正常系_オーダーCSV作成_新規_処理日付以外() throws IOException, ParseException {
		String outputFileName = "result_initial.csv";
		String tempFileName = "temp.csv";

		fileDeleate(outputPath + outputFileName);
		context.getBean(DBConfig.class).initTargetTestData("createOrderTestSuccessData.sql");
		// モック
		doNothing().when(restApiClient).callAssignWorker(anyList());
		doNothing().when(restApiClient).callAcceptWorkApi(anyList());
		Mockito.when(restApiClient.callFindOneContractApi(anyLong())).thenReturn(dummyContract());
		doNothing().when(restApiClient).callContractApi(anyObject());

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
		createOrderCsvDataDto.setContractDetailId(11L);
		createOrderCsvDataDto.setUpdatedAt(sdFormat.parse("2019-09-27 12:09:10"));
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
		createOrderCsvDataDto.setContractDetailId(11L);
		createOrderCsvDataDto.setUpdatedAt(sdFormat.parse("2019-09-27 12:09:10"));
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
		createOrderCsvDataDto.setContractDetailId(11L);
		createOrderCsvDataDto.setUpdatedAt(sdFormat.parse("2018-09-19 12:09:10"));
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
		createOrderCsvDataDto.setContractDetailId(11L);
		createOrderCsvDataDto.setUpdatedAt(sdFormat.parse("2018-09-19 12:09:10"));
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
}