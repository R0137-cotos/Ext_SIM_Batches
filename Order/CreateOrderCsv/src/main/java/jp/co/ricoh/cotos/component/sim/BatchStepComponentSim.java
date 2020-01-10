package jp.co.ricoh.cotos.component.sim;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.logic.businessday.BusinessDayUtil;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.commonlib.logic.mail.CommonSendMail;
import jp.co.ricoh.cotos.commonlib.logic.message.MessageUtil;
import jp.co.ricoh.cotos.commonlib.repository.accounting.AccountingRepository;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractRepository;
import jp.co.ricoh.cotos.component.BatchUtil;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.CreateOrderCsvDataDto;
import jp.co.ricoh.cotos.dto.CreateOrderCsvDto;
import jp.co.ricoh.cotos.dto.FindCreateOrderCsvDataDto;
import lombok.extern.log4j.Log4j;

@Component("SIM")
@Log4j
public class BatchStepComponentSim extends BatchStepComponent {

	@Autowired
	CommonSendMail commonSendMail;

	@Autowired
	AccountingRepository accountingRepository;

	@Autowired
	BatchUtil batchUtil;

	@Autowired
	MessageUtil messageUtil;

	@Autowired
	CheckUtil checkUtil;

	@Autowired
	DBUtil dbUtil;

	@Autowired
	BusinessDayUtil businessDayUtil;

	@Autowired
	ContractRepository contractRepository;

	@Override
	public boolean process(CreateOrderCsvDto dto, List<CreateOrderCsvDataDto> orderDataList) throws ParseException, JsonProcessingException, IOException {
		log.info("SIM独自処理");
		// CSV出力
		boolean createdFlg = false;

		if (dto.getCsvFile().exists()) {
			throw new FileAlreadyExistsException(dto.getCsvFile().getAbsolutePath());
		}

		List<FindCreateOrderCsvDataDto> findOrderDataList = new ArrayList<>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = dateFormat.parse(dto.getOperationDate());
		int branchNumber = 1;
		for (int i = 0; i < orderDataList.size(); i++) {
			Date shortBusinessDay = businessDayUtil.findShortestBusinessDay(DateUtils.truncate(date, Calendar.DAY_OF_MONTH), orderDataList.get(i).getShortestDeliveryDate(), false);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(orderDataList.get(i).getExtends_parameter());
			if (shortBusinessDay.compareTo(orderDataList.get(i).getConclusionPreferredDate()) < 1 && node.get("orderCsvCreationStatus").asInt() == 0) {
				int itemQuantity = Integer.parseInt(orderDataList.get(i).getQuantity());
				for (int k = 0; k < itemQuantity; k++) {
					FindCreateOrderCsvDataDto orderCsvEntity = new FindCreateOrderCsvDataDto();
					orderCsvEntity.setContractIdTemp(orderDataList.get(i).getContractIdTemp());
					orderCsvEntity.setContractId(orderDataList.get(i).getContractNumber() + String.format("%03d", branchNumber));
					orderCsvEntity.setRicohItemCode(orderDataList.get(i).getRicohItemCode());
					orderCsvEntity.setItemContractName(orderDataList.get(i).getItemContractName());
					orderCsvEntity.setOrderDate(date);
					orderCsvEntity.setConclusionPreferredDate(orderDataList.get(i).getConclusionPreferredDate());
					orderCsvEntity.setPicName(orderDataList.get(i).getPicName());
					orderCsvEntity.setPicNameKana(orderDataList.get(i).getPicNameKana());
					orderCsvEntity.setPostNumber(orderDataList.get(i).getPostNumber());
					orderCsvEntity.setAddress(orderDataList.get(i).getAddress());
					orderCsvEntity.setCompanyName(orderDataList.get(i).getCompanyName());
					orderCsvEntity.setOfficeName(orderDataList.get(i).getOfficeName());
					orderCsvEntity.setPicPhoneNumber(orderDataList.get(i).getPicPhoneNumber());
					orderCsvEntity.setPicFaxNumber(orderDataList.get(i).getPicFaxNumber());
					orderCsvEntity.setLineNumber("");
					orderCsvEntity.setSerialNumber("");
					orderCsvEntity.setDeliveryExpectedDate("");
					orderCsvEntity.setInvoiceNumber("");
					orderCsvEntity.setRemarks("");

					findOrderDataList.add(orderCsvEntity);
				}

				// 6.[外部API].(1.)で取得した手配業務に対し、手配情報担当作業者設定APIを呼び出し、呼び出しユーザを担当作業者に設定する。
				// arrangementDelegationUtil.callAssignWorker(arrangementListInfo.stream().map(e
				// -> e.getArrangementWorkId()).collect(Collectors.toList()));

				// 7.[外部API].(1.)で取得した手配業務一覧のうち「受付待ち」の手配業務に対し、手配情報業務受付APIを呼び出し、手配情報を作業中する。
				// arrangementDelegationUtil.callAcceptWorkApi(arrangementListInfo.stream().filter(e
				// -> e.getArrangementWorkStatus() == WorkflowStatus.受付待ち).map(e ->
				// e.getArrangementWorkId()).collect(Collectors.toList()));

				if (i + 1 < orderDataList.size() && orderDataList.get(i).getContractNumber().equals(orderDataList.get(i + 1).getContractNumber())) {
					branchNumber++;
				} else {
					branchNumber = 1;
				}

			}
		}
		System.out.println("★★★");
		System.out.println(findOrderDataList);
		List<String> successIdList = new ArrayList<>();
		List<String> failedIdList = new ArrayList<>();
		if (0 == orderDataList.size()) {
			log.info(messageUtil.createMessageInfo("BatchTargetNoDataInfo", new String[] { "オーダーCSV作成" }).getMsg());
			return createdFlg;
		} else {
			Map<Long, List<FindCreateOrderCsvDataDto>> OrderDataIdGroupingMap = findOrderDataList.stream().collect(Collectors.groupingBy(findOrderData -> findOrderData.getContractIdTemp(), Collectors.mapping(findOrderData -> findOrderData, Collectors.toList())));
			CsvMapper mapper = new CsvMapper();
			CsvSchema schemaWithHeader = mapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true).schemaFor(FindCreateOrderCsvDataDto.class).withHeader().withColumnSeparator(',').withLineSeparator("\r\n").withNullValue("\"\"");
			CsvSchema schemaWithOutHeader = mapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true).schemaFor(FindCreateOrderCsvDataDto.class).withoutHeader().withColumnSeparator(',').withLineSeparator("\r\n").withNullValue("\"\"");

			OrderDataIdGroupingMap.entrySet().stream().sorted(Entry.comparingByKey()).forEach(map -> {
				try {
					System.out.println("★");
					System.out.println(map);
					System.out.println("★");
					// if (successIdList.isEmpty()) {
					// mapper.writer(schemaWithHeader).writeValues(Files.newBufferedWriter(dto.getCsvFile().toPath(),
					// Charset.forName("UTF-8"), StandardOpenOption.CREATE,
					// StandardOpenOption.APPEND)).write(map.getValue());
					// } else {
					// mapper.writer(schemaWithOutHeader).writeValues(Files.newBufferedWriter(dto.getCsvFile().toPath(),
					// Charset.forName("UTF-8"), StandardOpenOption.APPEND)).write(map.getValue());
					// }
					successIdList.add(map.getKey().toString());
				} catch (Exception e) {// IOException e) {
					log.error(messageUtil.createMessageInfo("BatchCannotCreateFiles", new String[] { String.format("オーダーCSV作成") }).getMsg(), e);
					failedIdList.add(map.getKey().toString());
				}
			});
			// 拡張項目のUPDATE文
			if (!successIdList.isEmpty()) {
				Map<String, Object> successMap = new HashMap<>();
				String successExtendsParameter = "{\"orderCsvCreationStatus\":\"1\",\"orderCsvCreationDate\":\"" + dto.getOperationDate() + "\"}";

				successMap.put("extendsParam", successExtendsParameter);
				successMap.put("idList", successIdList);
				System.out.println(successExtendsParameter);
				String sql = dbUtil.loadSQLFromClasspath("sql/updateExtendsParameter.sql", successMap);
				System.out.println(sql);
				dbUtil.execute("sql/updateExtendsParameter.sql", successMap);
				System.out.println("SUCCESS");
				// 手配関係
				for (String ContractId : successIdList) {
					try {
						//batchUtil.callCompleteArrangement(ContractId);
					} catch (Exception arrangementError) {
						log.fatal(String.format("契約ID=%dの手配完了に失敗しました。", ContractId));
						arrangementError.printStackTrace();
					}
				}
			}
			if (!failedIdList.isEmpty()) {
				Map<String, Object> failedMap = new HashMap<>();
				String failedExtendsParameter = "{\"orderCsvCreationStatus\":\"2\",\"orderCsvCreationDate\":\"\"}";
				failedMap.put("extendsParam", failedExtendsParameter);
				failedMap.put("idList", failedIdList);
				dbUtil.execute("sql/updateExtendsParameter.sql", failedMap);
				System.out.println("FAILED");
			}
			return true;
		}
	}
}
