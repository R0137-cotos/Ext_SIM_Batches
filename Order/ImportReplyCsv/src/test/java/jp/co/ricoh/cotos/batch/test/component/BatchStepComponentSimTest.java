package jp.co.ricoh.cotos.batch.test.component;

import java.io.IOException;
import java.util.ArrayList;
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
import jp.co.ricoh.cotos.commonlib.entity.arrangement.Arrangement;
import jp.co.ricoh.cotos.commonlib.entity.arrangement.ArrangementWork;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ProductContract;
import jp.co.ricoh.cotos.commonlib.repository.arrangement.ArrangementRepository;
import jp.co.ricoh.cotos.component.RestApiClient;
import jp.co.ricoh.cotos.component.sim.BatchStepComponentSim;
import jp.co.ricoh.cotos.dto.ReplyOrderDto;
import jp.co.ricoh.cotos.util.DeliveryExpectedDateException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchStepComponentSimTest extends TestBase {

	static ConfigurableApplicationContext context;

	@MockBean
	ArrangementRepository arrangementRepository;

	@MockBean
	RestApiClient restApiClient;

	@SpyBean(name = "SIM")
	BatchStepComponentSim batchStepComponentSim;

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

	// 以下beforeProcessのテスト
	@Test
	public void 正常系_リプライCSV取込が行えること() {
		List<ReplyOrderDto> result = new ArrayList<>();
		try {
			result = batchStepComponentSim.beforeProcess(new String[] { filePath, fileName });
		} catch (IOException e) {
			Assert.fail("意図しないエラーが発生した");
		}
		if (result == null) {
			Assert.fail("nullが返らなかった");
		} else {
			Assert.assertEquals("リプライCSVが作成されていること", 3, result.size());
		}
	}

	@Test
	public void 異常系_引き数チェックでnullが返ること() {
		List<ReplyOrderDto> result = new ArrayList<>();
		try {
			result = batchStepComponentSim.beforeProcess(new String[] { filePath, fileName, "test" });
		} catch (IOException e) {
			Assert.fail("意図しないエラーが発生した");
		}
		if (result == null) {
			Assert.assertTrue("意図した通りnullがreturnされた", true);
		} else {
			Assert.fail("nullが返らなかった");
		}
	}

	@Test
	public void 異常系_ファイルチェックでnullが返ること() {
		List<ReplyOrderDto> result = new ArrayList<>();
		try {
			result = batchStepComponentSim.beforeProcess(new String[] { filePath, "noSuchFile.csv" });
		} catch (IOException e) {
			Assert.fail("意図しないエラーが発生した");
		}
		if (result == null) {
			Assert.assertTrue("意図した通りnullがreturnされた", true);
		} else {
			Assert.fail("nullが返らなかった");
		}
	}

	// 以下processのテスト
	@Test
	public void 正常系_新規() {
		Mockito.when(restApiClient.callFindTargetContractList(Mockito.anyObject())).thenReturn(契約検索結果作成());
		Mockito.when(restApiClient.callFindContract(Mockito.anyLong())).thenReturn(契約詳細作成("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"新規\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"\"},{\"id\":2,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"\"},{\"id\":3,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"追加1\",\"invoiceNumber\":\"\"}]}"));
		Mockito.doNothing().when(restApiClient).callUpdateContract(Mockito.any());
		Mockito.doReturn(手配詳細作成()).when(arrangementRepository).findByContractIdAndDisengagementFlg(Mockito.anyLong(), Mockito.anyInt());
		Mockito.doNothing().when(restApiClient).callCompleteArrangement(Mockito.anyLong());
		
		boolean successFlg = false;
		try {
			successFlg = batchStepComponentSim.process(入力テストデータ作成(false));
		} catch (IOException e) {
			Assert.fail("意図しないエラーが発生した");
		}
		if (successFlg == true) {
			Assert.assertTrue("正常終了した", true);
		} else {
			Assert.fail("エラーが発生した");
		}
	}

	@Test
	public void 正常系_容量変更() {
		Mockito.when(restApiClient.callFindTargetContractList(Mockito.anyObject())).thenReturn(契約検索結果作成());
		Mockito.when(restApiClient.callFindContract(Mockito.anyLong())).thenReturn(契約詳細作成("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"容量変更\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"08012345670\",\"serialNumber\":\"\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"\"},{\"id\":2,\"contractType\":\"容量変更\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345671\",\"serialNumber\":\"\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"\"},{\"id\":3,\"contractType\":\"容量変更\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345672\",\"serialNumber\":\"\",\"device\":\"追加1\",\"invoiceNumber\":\"\"}]}"));
		Mockito.doNothing().when(restApiClient).callUpdateContract(Mockito.any());
		Mockito.doReturn(手配詳細作成()).when(arrangementRepository).findByContractIdAndDisengagementFlg(Mockito.anyLong(), Mockito.anyInt());
		Mockito.doNothing().when(restApiClient).callCompleteArrangement(Mockito.anyLong());
		
		boolean successFlg = false;
		try {
			successFlg = batchStepComponentSim.process(入力テストデータ作成(false));
		} catch (IOException e) {
			Assert.fail("意図しないエラーが発生した");
		}
		if (successFlg == true) {
			Assert.assertTrue("正常終了した", true);
		} else {
			Assert.fail("エラーが発生した");
		}
	}

	@Test
	public void 正常系_有償交換() {
		Mockito.when(restApiClient.callFindTargetContractList(Mockito.anyObject())).thenReturn(契約検索結果作成());
		Mockito.when(restApiClient.callFindContract(Mockito.anyLong())).thenReturn(契約詳細作成("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"新規\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"0000000000\",\"serialNumber\":\"\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"\"},{\"id\":2,\"contractType\":\"有償交換\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"08012345670\",\"serialNumber\":\"\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"123456\"},{\"id\":3,\"contractType\":\"有償交換\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345671\",\"serialNumber\":\"\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"123457\"},{\"id\":4,\"contractType\":\"有償交換\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"08012345672\",\"serialNumber\":\"\",\"device\":\"追加1\",\"invoiceNumber\":\"123458\"}]}"));
		Mockito.doNothing().when(restApiClient).callUpdateContract(Mockito.any());
		Mockito.doReturn(手配詳細作成()).when(arrangementRepository).findByContractIdAndDisengagementFlg(Mockito.anyLong(), Mockito.anyInt());
		Mockito.doNothing().when(restApiClient).callCompleteArrangement(Mockito.anyLong());
		
		boolean successFlg = false;
		try {
			successFlg = batchStepComponentSim.process(入力テストデータ作成(false));
		} catch (IOException e) {
			Assert.fail("意図しないエラーが発生した");
		}
		if (successFlg == true) {
			Assert.assertTrue("正常終了した", true);
		} else {
			Assert.fail("エラーが発生した");
		}
	}

	@Test
	public void 異常系_CSVに納入予定日無し() {
		Mockito.when(restApiClient.callFindTargetContractList(Mockito.anyObject())).thenReturn(契約検索結果作成());
		Mockito.when(restApiClient.callFindContract(Mockito.anyLong())).thenReturn(契約詳細作成(""));

		try {
			batchStepComponentSim.process(入力テストデータ作成(true));
		} catch (IOException e) {
			Assert.fail("意図しないエラーが発生した");
		} catch (DeliveryExpectedDateException e) {
			Assert.assertTrue("意図した通りエラーが発生した", true);
		} catch (Exception e) {
			Assert.fail("意図しないエラーが発生した");
		}
	}

	@Test
	public void 異常系_契約検索APIエラー() {
		Mockito.when(restApiClient.callFindTargetContractList(Mockito.anyObject())).thenThrow(new RuntimeException());

		boolean successFlg = false;
		try {
			successFlg = batchStepComponentSim.process(入力テストデータ作成(false));
		} catch (IOException e) {
			Assert.fail("意図しないエラーが発生した");
		}
		if (successFlg == false) {
			Assert.assertTrue("意図した通りエラーが発生した", true);
		} else {
			Assert.fail("エラーが発生しなかった");
		}
	}

	@Test
	public void 異常系_契約詳細取得APIエラー() {
		Mockito.when(restApiClient.callFindTargetContractList(Mockito.anyObject())).thenReturn(契約検索結果作成());
		Mockito.when(restApiClient.callFindContract(Mockito.anyLong())).thenThrow(new RuntimeException());

		boolean successFlg = false;
		try {
			successFlg = batchStepComponentSim.process(入力テストデータ作成(false));
		} catch (IOException e) {
			Assert.fail("意図しないエラーが発生した");
		}
		if (successFlg == false) {
			Assert.assertTrue("意図した通りエラーが発生した", true);
		} else {
			Assert.fail("エラーが発生しなかった");
		}
	}

	@Test
	public void 異常系_拡張項目取込エラー() {
		Mockito.when(restApiClient.callFindTargetContractList(Mockito.anyObject())).thenReturn(契約検索結果作成());
		Mockito.when(restApiClient.callFindContract(Mockito.anyLong())).thenReturn(契約詳細作成("{test}"));

		boolean successFlg = false;
		try {
			successFlg = batchStepComponentSim.process(入力テストデータ作成(false));
		} catch (IOException e) {
			Assert.fail("意図しないエラーが発生した");
		}
		if (successFlg == false) {
			Assert.assertTrue("意図した通りエラーが発生した", true);
		} else {
			Assert.fail("エラーが発生しなかった");
		}
	}

	@Test
	public void 異常系_契約更新APIエラー() {
		Mockito.when(restApiClient.callFindTargetContractList(Mockito.anyObject())).thenReturn(契約検索結果作成());
		Mockito.when(restApiClient.callFindContract(Mockito.anyLong())).thenReturn(契約詳細作成("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"新規\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"\"},{\"id\":2,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"\"},{\"id\":3,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"追加1\",\"invoiceNumber\":\"\"}]}"));
		Mockito.doThrow(new RuntimeException()).when(restApiClient).callUpdateContract(Mockito.any());

		boolean successFlg = false;
		try {
			successFlg = batchStepComponentSim.process(入力テストデータ作成(false));
		} catch (IOException e) {
			Assert.fail("意図しないエラーが発生した");
		}
		if (successFlg == false) {
			Assert.assertTrue("意図した通りエラーが発生した", true);
		} else {
			Assert.fail("エラーが発生しなかった");
		}
	}

	@Test
	public void 異常系_手配情報取得エラー() {
		Mockito.when(restApiClient.callFindTargetContractList(Mockito.anyObject())).thenReturn(契約検索結果作成());
		Mockito.when(restApiClient.callFindContract(Mockito.anyLong())).thenReturn(契約詳細作成("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"新規\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"\"},{\"id\":2,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"\"},{\"id\":3,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"追加1\",\"invoiceNumber\":\"\"}]}"));
		Mockito.doNothing().when(restApiClient).callUpdateContract(Mockito.any());
		Mockito.doReturn(null).when(arrangementRepository).findByContractIdAndDisengagementFlg(Mockito.anyLong(), Mockito.anyInt());

		boolean successFlg = false;
		try {
			successFlg = batchStepComponentSim.process(入力テストデータ作成(false));
		} catch (IOException e) {
			Assert.fail("意図しないエラーが発生した");
		}
		if (successFlg == false) {
			Assert.assertTrue("意図した通りエラーが発生した", true);
		} else {
			Assert.fail("エラーが発生しなかった");
		}
	}

	@Test
	public void 異常系_手配業務完了APIエラー() {
		Mockito.when(restApiClient.callFindTargetContractList(Mockito.anyObject())).thenReturn(契約検索結果作成());
		Mockito.when(restApiClient.callFindContract(Mockito.anyLong())).thenReturn(契約詳細作成("{\"extendsParameterList\":[{\"id\":1,\"contractType\":\"新規\",\"productCode\":\"SI0001\",\"productName\":\"データSIM Type-C 2GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"TESTDATA\",\"invoiceNumber\":\"\"},{\"id\":2,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"qqANDROID\",\"invoiceNumber\":\"\"},{\"id\":3,\"contractType\":\"新規\",\"productCode\":\"SI0002\",\"productName\":\"データSIM Type-C 5GB\",\"lineNumber\":\"\",\"serialNumber\":\"\",\"device\":\"追加1\",\"invoiceNumber\":\"\"}]}"));
		Mockito.doNothing().when(restApiClient).callUpdateContract(Mockito.any());
		Mockito.doReturn(手配詳細作成()).when(arrangementRepository).findByContractIdAndDisengagementFlg(Mockito.anyLong(), Mockito.anyInt());
		Mockito.doThrow(new RuntimeException()).when(restApiClient).callCompleteArrangement(Mockito.anyLong());

		boolean successFlg = false;
		try {
			successFlg = batchStepComponentSim.process(入力テストデータ作成(false));
		} catch (IOException e) {
			Assert.fail("意図しないエラーが発生した");
		}
		if (successFlg == false) {
			Assert.assertTrue("意図した通りエラーが発生した", true);
		} else {
			Assert.fail("エラーが発生しなかった");
		}
	}

	private List<ReplyOrderDto> 入力テストデータ作成(boolean IsEmpyDeliveryExpectedDate) {
		List<ReplyOrderDto> testDataList = new ArrayList<>();

		ReplyOrderDto testData = new ReplyOrderDto();
		testData.setContractId("CC202011040000001");
		testData.setRicohItemCode("SI0001");
		testData.setLineNumber("08012345670");
		testData.setDevice("test");
		if (!IsEmpyDeliveryExpectedDate) {
			testData.setDeliveryExpectedDate("20201104");
		}
		testDataList.add(testData);

		return testDataList;
	}

	// 同じIDの契約をreturnしてしまうため1データのみ作成
	private List<Contract> 契約検索結果作成() {
		List<Contract> contractList = new ArrayList<Contract>();

		Contract contract = new Contract();
		contract.setId(1L);
		contractList.add(contract);

		return contractList;
	}

	private Contract 契約詳細作成(String extendsParameterIterance) {
		Contract contract = new Contract();
		contract.setId(1L);
		contract.setContractNumber("CC2020110400000");
		contract.setContractBranchNumber(1);

		List<ProductContract> productContractList = new ArrayList<ProductContract>();
		ProductContract productContract = new ProductContract();
		productContract.setId(1L);
		productContract.setExtendsParameterIterance(extendsParameterIterance);
		productContract.setContract(contract);
		productContractList.add(productContract);

		contract.setProductContractList(productContractList);

		return contract;
	}

	private Arrangement 手配詳細作成() {
		Arrangement arrangement = new Arrangement();
		arrangement.setId(1L);
		arrangement.setContractId(1L);

		List<ArrangementWork> arrangementWorkList = new ArrayList<>();
		ArrangementWork arrangementWork = new ArrangementWork();
		arrangementWork.setId(1L);
		arrangementWork.setArrangement(arrangement);
		arrangementWorkList.add(arrangementWork);

		arrangement.setArrangementWorkList(arrangementWorkList);

		return arrangement;
	}
}
