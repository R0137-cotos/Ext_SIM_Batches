package jp.co.ricoh.cotos.component.sim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
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
import java.util.stream.IntStream;

import org.apache.commons.io.IOUtils;
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
import jp.co.ricoh.cotos.commonlib.entity.arrangement.ArrangementWork.WorkflowStatus;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.logic.businessday.BusinessDayUtil;
import jp.co.ricoh.cotos.commonlib.logic.message.MessageUtil;
import jp.co.ricoh.cotos.commonlib.repository.arrangement.ArrangementRepository;
import jp.co.ricoh.cotos.commonlib.repository.arrangement.ArrangementWorkRepository;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractRepository;
import jp.co.ricoh.cotos.commonlib.repository.master.NonBusinessDayCalendarMasterRepository;
import jp.co.ricoh.cotos.component.BatchUtil;
import jp.co.ricoh.cotos.component.RestApiClient;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.CreateOrderCsvDataDto;
import jp.co.ricoh.cotos.dto.CreateOrderCsvDto;
import jp.co.ricoh.cotos.dto.FindCreateOrderCsvDataDto;
import lombok.extern.log4j.Log4j;

@Component("SIM")
@Log4j
public class BatchStepComponentSim extends BatchStepComponent {

	@Autowired
	ArrangementRepository arrangementRepository;

	@Autowired
	ArrangementWorkRepository arrangementWorkRepository;

	@Autowired
	NonBusinessDayCalendarMasterRepository nonBusinessDayCalendarMasterRepository;

	@Autowired
	ContractRepository contractRepository;

	@Autowired
	BatchUtil batchUtil;

	@Autowired
	RestApiClient restApiClient;

	@Autowired
	MessageUtil messageUtil;

	@Autowired
	DBUtil dbUtil;

	@Autowired
	BusinessDayUtil businessDayUtil;

	private static final String headerFilePath = "file/header.csv";

	@Override
	public void process(CreateOrderCsvDto dto, List<CreateOrderCsvDataDto> orderDataList) throws ParseException, JsonProcessingException, IOException {
		log.info("SIM独自処理");
		// 取得したデータを出力データのみに設定
		Date operationDate = batchUtil.changeDate(dto.getOperationDate());
		Date changeOperationDate = null;
		if ("2".equals(dto.getType())) {
			changeOperationDate = businessDayUtil.getLastBusinessDayOfTheMonth(new SimpleDateFormat("YYYYMM").format(operationDate));
			changeOperationDate = businessDayUtil.findShortestBusinessDay(DateUtils.truncate(changeOperationDate, Calendar.DAY_OF_MONTH), 3, true);
		}
		if ((("1".equals(dto.getType()) || "3".equals(dto.getType())) && nonBusinessDayCalendarMasterRepository.findOne(operationDate) == null) || ("2".equals(dto.getType()) && changeOperationDate.compareTo(operationDate) == 0)) {
			orderDataList = orderDataList.stream().filter(o -> {
				int orderCsvCreationStatus = 1;
				try {
					orderCsvCreationStatus = batchUtil.getOrderCsvCreationStatus(o.getExtendsParameter());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return orderCsvCreationStatus == 0;
			}).filter(o -> {
				Date shortBusinessDay = null;
				if ("1".equals(dto.getType())) {
					// 処理年月日 + 最短納期日を取得
					shortBusinessDay = businessDayUtil.findShortestBusinessDay(DateUtils.truncate(operationDate, Calendar.DAY_OF_MONTH), o.getShortestDeliveryDate(), false);
					return shortBusinessDay.compareTo(o.getConclusionPreferredDate()) > -1;
				} else if ("2".equals(dto.getType())) {
					// 処理年月日の次月1日
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(operationDate.getTime());
					cal.add(Calendar.MONTH, 1);
					cal.set(Calendar.DAY_OF_MONTH, 1);
					shortBusinessDay = DateUtils.truncate(cal.getTime(), Calendar.DAY_OF_MONTH);
					return shortBusinessDay.compareTo(o.getConclusionPreferredDate()) == 0;
				} else if ("3".equals(dto.getType())) {
					// 処理年月日 + 3営業日
					shortBusinessDay = businessDayUtil.findShortestBusinessDay(DateUtils.truncate(operationDate, Calendar.DAY_OF_MONTH), 3, false);
					return shortBusinessDay.compareTo(o.getConclusionPreferredDate()) > -1;
				}
				return false;
			}).collect(Collectors.toList());

			if ("2".equals(dto.getType())) {
				// 契約.更新日時 >= 処理年月日前月末日営業日 - 3
				orderDataList = orderDataList.stream().filter(o -> {
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(operationDate.getTime());
					cal.add(Calendar.MONTH, -1);
					Date lastBusinessDay = DateUtils.truncate(cal.getTime(), Calendar.DAY_OF_MONTH);
					lastBusinessDay = businessDayUtil.getLastBusinessDayOfTheMonth(new SimpleDateFormat("YYYYMM").format(lastBusinessDay));
					lastBusinessDay = businessDayUtil.findShortestBusinessDay(DateUtils.truncate(lastBusinessDay, Calendar.DAY_OF_MONTH), 2, true);
					return lastBusinessDay.compareTo(DateUtils.truncate(o.getUpdatedAt(), Calendar.DAY_OF_MONTH)) <= 0;
				}).collect(Collectors.toList());
			}

			if (0 == orderDataList.size()) {
				log.info(messageUtil.createMessageInfo("BatchTargetNoDataInfo", new String[] { "オーダーCSV作成" }).getMsg());
			} else {
				List<FindCreateOrderCsvDataDto> findOrderDataList = new ArrayList<>();
				Map<String, List<CreateOrderCsvDataDto>> contractNumberGroupingMap = orderDataList.stream().collect(Collectors.groupingBy(order -> order.getContractNumber(), Collectors.mapping(order -> order, Collectors.toList())));

				if ("1".equals(dto.getType())) {
					contractNumberGroupingMap.entrySet().stream().forEach(orderDataMap -> {
						IntStream.range(0, orderDataMap.getValue().size()).forEach(i -> {
							CreateOrderCsvDataDto orderData = orderDataMap.getValue().get(i);
							int itemQuantity = Integer.parseInt(orderData.getQuantity());
							IntStream.range(0, itemQuantity).forEach(k -> {
								findOrderDataList.add(getOrderCsvEntity(orderData, operationDate, i));
							});
						});
					});
				} else {
					contractNumberGroupingMap.entrySet().stream().forEach(orderDataMap -> {
						IntStream.range(0, orderDataMap.getValue().size()).forEach(i -> {
							CreateOrderCsvDataDto orderData = orderDataMap.getValue().get(i);
							findOrderDataList.add(getOrderCsvEntity(orderData, operationDate, i));
						});
					});
				}

				List<Long> successIdList = new ArrayList<>();
				List<Long> failedIdList = new ArrayList<>();

				Map<Long, List<FindCreateOrderCsvDataDto>> OrderDataIdGroupingMap = findOrderDataList.stream().collect(Collectors.groupingBy(findOrderData -> findOrderData.getContractIdTemp(), Collectors.mapping(findOrderData -> findOrderData, Collectors.toList())));
				CsvMapper mapper = new CsvMapper();
				CsvSchema schemaWithOutHeader = mapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true).schemaFor(FindCreateOrderCsvDataDto.class).withoutHeader().withColumnSeparator(',').withLineSeparator("\r\n").withNullValue("\"\"");

				// CSV出力
				OrderDataIdGroupingMap.entrySet().stream().sorted(Entry.comparingByKey()).forEach(map -> {
					try {
						mapper.writer(schemaWithOutHeader).writeValues(Files.newBufferedWriter(dto.getTmpFile().toPath(), Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND)).write(map.getValue());
						successIdList.add(map.getKey());
					} catch (Exception e) {
						log.error(messageUtil.createMessageInfo("BatchCannotCreateFiles", new String[] { String.format("オーダーCSV作成") }).getMsg(), e);
						failedIdList.add(map.getKey());
					}
				});
				// ヘッダーファイルとのマージ
				List<String> outputList = Files.readAllLines(dto.getTmpFile().toPath(), Charset.forName("UTF-8"));
				List<String> headerList = new ArrayList<>();
				InputStream in = this.getClass().getClassLoader().getResourceAsStream(headerFilePath);
				String header = IOUtils.toString(in, "UTF-8");
				headerList.add(header);
				headerList.addAll(outputList);
				try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(dto.getCsvFile())))) {
					headerList.stream().forEach(s -> pw.print(s + "\r\n"));
				}
				Files.deleteIfExists(dto.getTmpFile().toPath());

				// 出力成功
				if (!successIdList.isEmpty()) {
					// 事後処理（拡張項目）
					String successExtendsParameter = "{\"orderCsvCreationStatus\":\"1\",\"orderCsvCreationDate\":\"" + dto.getOperationDate() + "\"}";
					List<Long> contractIdList = orderDataList.stream().filter(o -> successIdList.contains(o.getContractIdTemp())).map(o -> o.getContractIdTemp()).collect(Collectors.toList());

					contractIdList.forEach(contractId -> {
						//Contract contract = contractRepository.findOne(contractId);
						Contract contract = restApiClient.callFindOneContractApi(contractId);
						contract.getContractDetailList().forEach(ContractDetail -> {
							ContractDetail.setExtendsParameter(successExtendsParameter);
						});
						restApiClient.callContractApi(contract);
					});

					// エラー発生個所	
					// 事後処理（手配）
					successIdList.stream().forEach(ContractId -> {
						List<Long> arrangementWorkIdListAssign = new ArrayList<>();
						List<Long> arrangementWorkIdListAccept = new ArrayList<>();
						Arrangement arrangement = arrangementRepository.findByContractIdAndDisengagementFlg(ContractId, 0);
						//Arrangement arrangement = restApiClient.callFindOneArrangement(ContractId);
						if (arrangement != null) {
							List<ArrangementWork> arrangementWorkList = arrangement.getArrangementWorkList();
							arrangementWorkList.stream().forEach(arrangementWork -> {
								if (arrangementWork.getArrangementPicWorkerEmp() == null) {
									arrangementWorkIdListAssign.add(arrangementWork.getId());
								}
								if (arrangementWork.getWorkflowStatus() == WorkflowStatus.受付待ち) {
									arrangementWorkIdListAccept.add(arrangementWork.getId());
								}
							});
						}
						// 手配担当者登録APIを実行
						try {
							restApiClient.callAssignWorker(arrangementWorkIdListAssign);
						} catch (Exception arrangementError) {
							log.fatal(String.format("担当者登録に失敗しました。"));
							arrangementError.printStackTrace();
						}
						// 手配業務受付APIを実行
						try {
							restApiClient.callAcceptWorkApi(arrangementWorkIdListAccept);
						} catch (Exception arrangementError) {
							log.fatal(String.format("ステータスの変更に失敗しました。"));
							arrangementError.printStackTrace();
						}
					});
				}
				// 出力失敗
				if (!failedIdList.isEmpty()) {
					// 事後処理（拡張項目）
					Map<String, Object> failedMap = new HashMap<>();
					String failedExtendsParameter = "{\"orderCsvCreationStatus\":\"2\",\"orderCsvCreationDate\":\"\"}";
					List<Long> contractDetailIdList = orderDataList.stream().filter(o -> failedIdList.contains(o.getContractIdTemp())).map(o -> o.getContractDetailId()).collect(Collectors.toList());

					failedMap.put("extendsParam", failedExtendsParameter);
					failedMap.put("idList", contractDetailIdList);
					dbUtil.execute("sql/updateExtendsParameter.sql", failedMap);
				}
			}
		}
	}

	public FindCreateOrderCsvDataDto getOrderCsvEntity(CreateOrderCsvDataDto orderData, Date operationDate, int i) {
		FindCreateOrderCsvDataDto orderCsvEntity = new FindCreateOrderCsvDataDto();
		orderCsvEntity.setContractIdTemp(orderData.getContractIdTemp());
		orderCsvEntity.setContractDetailId(orderData.getContractDetailId());
		orderCsvEntity.setContractId(orderData.getContractNumber() + String.format("%03d", i + 1));
		orderCsvEntity.setRicohItemCode(orderData.getRicohItemCode());
		orderCsvEntity.setItemContractName(orderData.getItemContractName());
		orderCsvEntity.setOrderDate(batchUtil.changeFormatString(operationDate));
		orderCsvEntity.setConclusionPreferredDate(batchUtil.changeFormatString(orderData.getConclusionPreferredDate()));
		orderCsvEntity.setPicName(orderData.getPicName());
		orderCsvEntity.setPicNameKana(orderData.getPicNameKana());
		orderCsvEntity.setPostNumber(orderData.getPostNumber());
		orderCsvEntity.setAddress(orderData.getAddress());
		orderCsvEntity.setCompanyName(orderData.getCompanyName());
		orderCsvEntity.setOfficeName(orderData.getOfficeName());
		orderCsvEntity.setPicPhoneNumber(orderData.getPicPhoneNumber());
		orderCsvEntity.setPicFaxNumber(orderData.getPicFaxNumber());
		orderCsvEntity.setPicMailAddress(orderData.getPicMailAddress());
		orderCsvEntity.setLineNumber("");
		orderCsvEntity.setSerialNumber("");
		orderCsvEntity.setDeliveryExpectedDate("");
		orderCsvEntity.setInvoiceNumber("");
		orderCsvEntity.setRemarks("");

		return orderCsvEntity;
	}
}
