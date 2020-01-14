package jp.co.ricoh.cotos.component.sim;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
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
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.entity.arrangement.Arrangement;
import jp.co.ricoh.cotos.commonlib.entity.arrangement.ArrangementWork;
import jp.co.ricoh.cotos.commonlib.logic.businessday.BusinessDayUtil;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.commonlib.logic.mail.CommonSendMail;
import jp.co.ricoh.cotos.commonlib.logic.message.MessageUtil;
import jp.co.ricoh.cotos.commonlib.repository.accounting.AccountingRepository;
import jp.co.ricoh.cotos.commonlib.repository.arrangement.ArrangementRepository;
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
	ArrangementRepository arrangementRepository;

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
	public void process(CreateOrderCsvDto dto, List<CreateOrderCsvDataDto> orderDataList) throws ParseException, JsonProcessingException, IOException {
		log.info("SIM独自処理");
		if (dto.getCsvFile().exists()) {
			throw new FileAlreadyExistsException(dto.getCsvFile().getAbsolutePath());
		}

		List<FindCreateOrderCsvDataDto> findOrderDataList = new ArrayList<>();
		Date operationDate = batchUtil.changeDate(dto.getOperationDate());
		int branchNumber = 1;
		for (int i = 0; i < orderDataList.size(); i++) {
			Date shortBusinessDay = businessDayUtil.findShortestBusinessDay(DateUtils.truncate(operationDate, Calendar.DAY_OF_MONTH), orderDataList.get(i).getShortestDeliveryDate(), false);
			int orderCsvCreationStatus = batchUtil.getOrderCsvCreationStatus(orderDataList.get(i).getExtends_parameter());
			if (shortBusinessDay.compareTo(orderDataList.get(i).getConclusionPreferredDate()) < 1 && orderCsvCreationStatus == 0) {
				int itemQuantity = Integer.parseInt(orderDataList.get(i).getQuantity());
				for (int k = 0; k < itemQuantity; k++) {
					FindCreateOrderCsvDataDto orderCsvEntity = new FindCreateOrderCsvDataDto();
					orderCsvEntity.setContractIdTemp(orderDataList.get(i).getContractIdTemp());
					orderCsvEntity.setContractId(orderDataList.get(i).getContractNumber() + String.format("%03d", branchNumber));
					orderCsvEntity.setRicohItemCode(orderDataList.get(i).getRicohItemCode());
					orderCsvEntity.setItemContractName(orderDataList.get(i).getItemContractName());
					orderCsvEntity.setOrderDate(operationDate);
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

				if (i + 1 < orderDataList.size() && orderDataList.get(i).getContractNumber().equals(orderDataList.get(i + 1).getContractNumber())) {
					branchNumber++;
				} else {
					branchNumber = 1;
				}

			}
		}
		List<Long> successIdList = new ArrayList<>();
		List<Long> failedIdList = new ArrayList<>();
		if (0 == orderDataList.size()) {
			log.info(messageUtil.createMessageInfo("BatchTargetNoDataInfo", new String[] { "オーダーCSV作成" }).getMsg());
		} else {
			Map<Long, List<FindCreateOrderCsvDataDto>> OrderDataIdGroupingMap = findOrderDataList.stream().collect(Collectors.groupingBy(findOrderData -> findOrderData.getContractIdTemp(), Collectors.mapping(findOrderData -> findOrderData, Collectors.toList())));
			CsvMapper mapper = new CsvMapper();
			CsvSchema schemaWithHeader = mapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true).schemaFor(FindCreateOrderCsvDataDto.class).withHeader().withColumnSeparator(',').withLineSeparator("\r\n").withNullValue("\"\"");
			CsvSchema schemaWithOutHeader = mapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true).schemaFor(FindCreateOrderCsvDataDto.class).withoutHeader().withColumnSeparator(',').withLineSeparator("\r\n").withNullValue("\"\"");

			OrderDataIdGroupingMap.entrySet().stream().sorted(Entry.comparingByKey()).forEach(map -> {
				try {
					if (successIdList.isEmpty()) {
						mapper.writer(schemaWithHeader).writeValues(Files.newBufferedWriter(dto.getCsvFile().toPath(), Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND)).write(map.getValue());
					} else {
						mapper.writer(schemaWithOutHeader).writeValues(Files.newBufferedWriter(dto.getCsvFile().toPath(), Charset.forName("UTF-8"), StandardOpenOption.APPEND)).write(map.getValue());
					}
					successIdList.add(map.getKey());
				} catch (Exception e) {
					log.error(messageUtil.createMessageInfo("BatchCannotCreateFiles", new String[] { String.format("オーダーCSV作成") }).getMsg(), e);
					failedIdList.add(map.getKey());
				}
			});
			if (!successIdList.isEmpty()) {
				Map<String, Object> successMap = new HashMap<>();
				String successExtendsParameter = "{\"orderCsvCreationStatus\":\"1\",\"orderCsvCreationDate\":\"" + dto.getOperationDate() + "\"}";

				successMap.put("extendsParam", successExtendsParameter);
				successMap.put("idList", successIdList);
				dbUtil.execute("sql/updateExtendsParameter.sql", successMap);
				List<Long> arrangementWorkIdList = new ArrayList<>();
				successIdList.stream().forEach(ContractId -> {
					Arrangement arrangement = arrangementRepository.findByContractIdAndDisengagementFlg(ContractId, 0);
					if (arrangement != null) {
						List<ArrangementWork> arrangementWorkList = arrangement.getArrangementWorkList();
						arrangementWorkList.stream().forEach(arrangementWorkId -> arrangementWorkIdList.add(arrangementWorkId.getId()));
					}
				});
				try {
					batchUtil.callAssignWorker(arrangementWorkIdList);
				} catch (Exception arrangementError) {
					log.fatal(String.format("担当者登録に失敗しました。"));
					arrangementError.printStackTrace();
				}
				try {
					batchUtil.callAcceptWorkApi(arrangementWorkIdList);
				} catch (Exception arrangementError) {
					log.fatal(String.format("ステータスの変更に失敗しました。"));
					arrangementError.printStackTrace();
				}
			}
			if (!failedIdList.isEmpty()) {
				Map<String, Object> failedMap = new HashMap<>();
				String failedExtendsParameter = "{\"orderCsvCreationStatus\":\"2\",\"orderCsvCreationDate\":\"\"}";
				failedMap.put("extendsParam", failedExtendsParameter);
				failedMap.put("idList", failedIdList);
				dbUtil.execute("sql/updateExtendsParameter.sql", failedMap);
			}
		}
	}
}
