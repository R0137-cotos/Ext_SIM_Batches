package jp.co.ricoh.cotos.batch.test.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import jp.co.ricoh.cotos.batch.test.mock.WithMockCustomUser;
import jp.co.ricoh.cotos.commonlib.entity.common.MailSendHistory;
import jp.co.ricoh.cotos.commonlib.entity.common.MailSendHistory.MailSendType;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.repository.common.MailSendHistoryRepository;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.SearchMailTargetDto;
import jp.co.ricoh.cotos.logic.BatchComponent;
import jp.co.ricoh.cotos.logic.JobComponent;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchStepComponentSimTest extends TestBase {

	static ConfigurableApplicationContext context;

	@Autowired
	JobComponent jobComponent;

	@Autowired
	MailSendHistoryRepository mailSendHistoryRepository;

	@SpyBean(name = "SIM")
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

	@Test
	public void 異常系_パラメータチェックテスト_パラメーター数不一致() throws Exception {
		try {
			batchStepComponent.paramCheck(new String[] { "dummy", "dummy" });
			Assert.fail("正常終了");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals(messageInfo.get(0).getErrorId(), "ROT00001");
			Assert.assertEquals(messageInfo.get(0).getErrorMessage(), "パラメータ「処理日」が設定されていません。");
		}
	}

	@Test
	public void 異常系_パラメータチェックテスト_日付不正() throws Exception {
		try {
			batchStepComponent.paramCheck(new String[] { "dummy" });
			Assert.fail("正常終了");
		} catch (ErrorCheckException e) {
			// エラーメッセージ取得
			List<ErrorInfo> messageInfo = e.getErrorInfoList();
			Assert.assertEquals(1, messageInfo.size());
			Assert.assertEquals(messageInfo.get(0).getErrorId(), "ROT00032");
			Assert.assertEquals(messageInfo.get(0).getErrorMessage(), "日付のフォーマットはyyyyMMddです。");
		}
	}

	@Test
	public void 正常系_パラメーターチェックテスト() {
		try {
			batchStepComponent.paramCheck(new String[] { "20200203" });
		} catch (Exception e) {
			Assert.fail("エラー");
		}
	}

	@Test
	public void 正常系_データ取得テスト() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("sql/SendDeviceBlankAlertMailTests.sql");
		String serviceTermStart = "20200203";
		try {
			List<SearchMailTargetDto> serchMailTargetDtoList = batchStepComponent.getDataList(serviceTermStart);
			Assert.assertEquals(1, serchMailTargetDtoList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("エラー");
		}
	}

	@Test
	public void 正常系_データ取得_取得無し() throws IOException {
		String serviceTermStart = "20200803";
		try {
			List<SearchMailTargetDto> serchMailTargetDtoList = batchStepComponent.getDataList(serviceTermStart);
			Assert.assertEquals(0, serchMailTargetDtoList.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void 正常系_データ取得テスト_合計数量16を超える() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("sql/SendDeviceBlankAlertMailTestsOver16.sql");
		String serviceTermStart = "20200203";
		try {
			List<SearchMailTargetDto> serchMailTargetDtoList = batchStepComponent.getDataList(serviceTermStart);
			Assert.assertEquals(1, serchMailTargetDtoList.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("エラー");
		}
	}

	@Test
	@WithMockCustomUser
	public void 正常系_メール送信テスト() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("sql/SendDeviceBlankAlertMailTests.sql");
		List<SearchMailTargetDto> serchMailTargetDtoList = new ArrayList<SearchMailTargetDto>();
		SearchMailTargetDto serchMailTargetDto = new SearchMailTargetDto();
		serchMailTargetDto.setSeqNo(1L);
		serchMailTargetDto.setProductGrpMasterId(300L);
		serchMailTargetDto.setMailAddress("test@example.com");
		serchMailTargetDto.setContractId(140L);
		serchMailTargetDtoList.add(serchMailTargetDto);
		long controlId = 3100;
		try {
			batchStepComponent.process(serchMailTargetDtoList, controlId);
			mailSendHistoryRepository.count();
			List<MailSendHistory> mailHistorytList = (List<MailSendHistory>) mailSendHistoryRepository.findAll();
			List<MailSendHistory> mailHistorytTargetList = mailHistorytList.stream().filter(m -> controlId == (m.getMailControlMaster().getId())).collect(Collectors.toList());
			Assert.assertEquals("履歴が登録されていること：全数", 1, mailHistorytTargetList.size());
			Assert.assertEquals("履歴が登録されていること：完了", 1, mailHistorytTargetList.stream().filter(m -> MailSendType.完了 == m.getMailSendType()).count());
			Assert.assertEquals("履歴が登録されていること：エラーのみ", 0, mailHistorytTargetList.stream().filter(m -> MailSendType.エラー == m.getMailSendType()).count());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("エラー");
		}
	}

	@Test
	@WithMockCustomUser
	public void 正常系_メール送信テスト_エラー() throws IOException {
		List<SearchMailTargetDto> serchMailTargetDtoList = new ArrayList<SearchMailTargetDto>();
		SearchMailTargetDto serchMailTargetDto = new SearchMailTargetDto();
		serchMailTargetDto.setSeqNo(1L);
		serchMailTargetDto.setProductGrpMasterId(300L);
		serchMailTargetDto.setContractId(10L);
		serchMailTargetDtoList.add(serchMailTargetDto);
		long controlId = 3100;
		try {
			batchStepComponent.process(serchMailTargetDtoList, controlId);
			mailSendHistoryRepository.count();
			List<MailSendHistory> mailHistorytList = (List<MailSendHistory>) mailSendHistoryRepository.findAll();
			List<MailSendHistory> mailHistorytTargetList = mailHistorytList.stream().filter(m -> controlId == (m.getMailControlMaster().getId())).collect(Collectors.toList());
			Assert.assertEquals("履歴が登録されていること：全数", 1, mailHistorytTargetList.size());
			Assert.assertEquals("履歴が登録されていること：完了", 0, mailHistorytTargetList.stream().filter(m -> MailSendType.完了 == m.getMailSendType()).count());
			Assert.assertEquals("履歴が登録されていること：エラーのみ", 1, mailHistorytTargetList.stream().filter(m -> MailSendType.エラー == m.getMailSendType()).count());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("エラー");
		}
	}

	@Test
	@WithMockCustomUser
	public void 正常系_メール送信テスト_正常アンドエラー() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("sql/SendDeviceBlankAlertMailTests_正常アンドエラー.sql");
		List<SearchMailTargetDto> serchMailTargetDtoList = new ArrayList<SearchMailTargetDto>();
		SearchMailTargetDto serchMailTargetDto = new SearchMailTargetDto();
		serchMailTargetDto.setSeqNo(1L);
		serchMailTargetDto.setProductGrpMasterId(300L);
		serchMailTargetDto.setContractId(140L);
		serchMailTargetDtoList.add(serchMailTargetDto);
		serchMailTargetDto = new SearchMailTargetDto();
		serchMailTargetDto.setSeqNo(2L);
		serchMailTargetDto.setProductGrpMasterId(300L);
		serchMailTargetDto.setMailAddress("test@example.com");
		serchMailTargetDto.setContractId(200L);
		serchMailTargetDtoList.add(serchMailTargetDto);
		long controlId = 3100;
		try {
			batchStepComponent.process(serchMailTargetDtoList, controlId);
			mailSendHistoryRepository.count();
			List<MailSendHistory> mailHistorytList = (List<MailSendHistory>) mailSendHistoryRepository.findAll();
			List<MailSendHistory> mailHistorytTargetList = mailHistorytList.stream().filter(m -> controlId == (m.getMailControlMaster().getId())).collect(Collectors.toList());
			Assert.assertEquals("履歴が登録されていること：全数", 2, mailHistorytTargetList.size());
			Assert.assertEquals("履歴が登録されていること：完了", 1, mailHistorytTargetList.stream().filter(m -> MailSendType.完了 == m.getMailSendType()).count());
			Assert.assertEquals("履歴が登録されていること：エラーのみ", 1, mailHistorytTargetList.stream().filter(m -> MailSendType.エラー == m.getMailSendType()).count());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("エラー");
		}
	}
}