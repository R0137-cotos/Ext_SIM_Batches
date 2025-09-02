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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.entity.arrangement.Arrangement;
import jp.co.ricoh.cotos.commonlib.entity.arrangement.ArrangementWork;
import jp.co.ricoh.cotos.commonlib.entity.arrangement.ArrangementWork.WorkflowStatus;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract.ContractType;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.logic.businessday.BusinessDayUtil;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
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
import jp.co.ricoh.cotos.dto.ExtendsParameterDtoComparator;
import jp.co.ricoh.cotos.dto.FindCreateOrderCsvDataDto;
import jp.co.ricoh.cotos.dto.SIMExtendsParameterIteranceDto;
import jp.co.ricoh.jmo.util.StringUtil;
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

	@Autowired
	ObjectMapper om;

	@Autowired
	CheckUtil checkUtil;

	private static final String headerFilePath = "file/header.csv";

	/**
	 * 手配業務タイプ名=業務区登記簿コピー添付の手配業務タイプマスタID
	 */
	private static final long TOUKIBO_COPY_ARRANGEMENT_ID = 3002;

	private static final int NOT_DISENGAGEMENT = 0;

	private static final String SAGAWA_CODE_COULMN_F_1 = "1";

	private static final String SAGAWA_CODE_COULMN_F_2 = "2";

	private static final String SAGAWA_CODE_COULMN_F_ORVER_3 = "3日以上";

	private static final String SAGAWA_CODE_COULMN_F_IMP = "不能";

	private static final String SAGAWA_CODE_COULMN_F_ILD = "離島は問合せ";

	private static final String SAGAWA_CODE_COULMN_F_ERROR = "ERROR";

	private static final String SAGAWA_CODE_COULMN_F_NULL = "NULL";

	@Override
	@Transactional
	public boolean process(CreateOrderCsvDto dto, List<CreateOrderCsvDataDto> orderDataList) throws ParseException, JsonProcessingException, IOException {
		log.info("SIM独自処理");
		// 空かどうかでエラーの有無を確認するためIDの重複があっても問題ない
		List<Long> errorDataIdList = new ArrayList<Long>();
		// 処理日
		Date operationDate = batchUtil.toDate(dto.getOperationDate());
		// 容量変更データをオーダーCSVに載せる日付(本日付以外では容量変更データをオーダーCSVに載せない)
		Date changeOperationDate = null;
		if ("2".equals(dto.getType())) {
			changeOperationDate = businessDayUtil.getLastBusinessDayOfTheMonthFromNonBusinessCalendarMaster(new SimpleDateFormat("YYYYMM").format(operationDate));
			// 処理日当月の最終営業日-5営業日を設定する
			changeOperationDate = businessDayUtil.findShortestBusinessDay(DateUtils.truncate(changeOperationDate, Calendar.DAY_OF_MONTH), 5, true);
		}

		// 以下条件の場合オーダーCSV出力する
		// １．オーダーCSV作成状態:0(未作成)である
		// ２．契約種別：新規 かつ 処理日付が営業日である
		// または 契約種別：容量変更 かつ 処理日当月最終営業日 - 2営業日
		// または 契約種別：有償交換 かつ 処理日付が営業日である
		// ３．種別：新規 かつ 契約.契約種別が新規である
		// または 種別：容量変更 かつ 契約種別が契約変更である
		// または 種別：有償交換 かつ 契約種別が契約変更である
		// ４．種別：新規 かつ 契約に紐づく手配「業務区登記簿コピー添付」が存在する場合、手配業務.ワークフロー状態=5（作業完了)であること
		if ((("1".equals(dto.getType()) || "3".equals(dto.getType())) && nonBusinessDayCalendarMasterRepository.findOneByNonBusinessDayAndVendorShortNameIsNull(operationDate) == null) || ("2".equals(dto.getType()) && changeOperationDate.compareTo(operationDate) == 0)) {
			orderDataList = orderDataList.stream().filter(o -> {
				// オーダーCSV作成状態 0:未作成 1:作成済
				int orderCsvCreationStatus = 1;
				try {
					// 契約明細.拡張項目.オーダーCSV作成状態を取得
					orderCsvCreationStatus = batchUtil.getOrderCsvCreationStatus(o.getExtendsParameter());
				} catch (IOException e) {
					errorDataIdList.add(o.getContractIdTemp());
					log.fatal(String.format("契約明細ID=%dのオーダーCSV作成状態の取得に失敗しました。", o.getContractIdTemp()), e);
				}

				return orderCsvCreationStatus == 0;
			}).filter(o -> {
				Date shortBusinessDay = null;
				if ("1".equals(dto.getType())) {
					// 処理年月日 + 最短納期日を取得
					List<String> vendorNameList = new ArrayList<>();
					if (!StringUtils.isEmpty(o.getVendorShortName())) {
						for (String vendorShortName : o.getVendorShortName().split(",", 0)) {
							vendorNameList.add(vendorShortName);
						}
					}

					// エクセルファイル突き合わせ
					int shortestDeliveryDate = o.getShortestDeliveryDate();
					List<String> postNumber = batchUtil.getPostNumber(o.getContractIdTemp());
					// 配送にかかる日数を地域に合わせる
					String sagawaCodeColumnF = batchUtil.getSagawaCodeColumnF(postNumber);
					if (StringUtils.isBlank(sagawaCodeColumnF)) {
						sagawaCodeColumnF = "NULL";
					}
					switch (sagawaCodeColumnF) {
					case SAGAWA_CODE_COULMN_F_1:
						shortestDeliveryDate -= 1;
						break;
					case SAGAWA_CODE_COULMN_F_2:
						// 変更なし
						break;
					case SAGAWA_CODE_COULMN_F_ORVER_3:
					case SAGAWA_CODE_COULMN_F_IMP:
					case SAGAWA_CODE_COULMN_F_ILD:
					case SAGAWA_CODE_COULMN_F_NULL:
						shortestDeliveryDate += 1;
						break;
					case SAGAWA_CODE_COULMN_F_ERROR:
						errorDataIdList.add(o.getContractIdTemp());
						log.fatal(String.format("契約明細ID=%dについてのオーダーCSV出力有無判定処理実行時に佐川コード突き当てファイルの読み込みに失敗しました。", o.getContractIdTemp()));
						return false;
					}

					shortBusinessDay = businessDayUtil.findShortestBusinessDay(DateUtils.truncate(operationDate, Calendar.DAY_OF_MONTH), shortestDeliveryDate, false, vendorNameList);
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
			}).filter(o -> {
				if ("1".equals(dto.getType())) {
					return ContractType.新規 == o.getContractType();
				} else {
					return ContractType.契約変更 == o.getContractType();
				}
			}).filter(o -> {
				// 契約に紐づく手配「業務区登記簿コピー添付」が存在する場合、手配業務.ワークフロー状態=5（作業完了)であること
				if ("1".equals(dto.getType())) {
					Arrangement arrangement = arrangementRepository.findByContractIdAndDisengagementFlg(o.getContractIdTemp(), NOT_DISENGAGEMENT);
					if (arrangement != null && !CollectionUtils.isEmpty(arrangement.getArrangementWorkList())) {
						for (ArrangementWork arrangementWork : arrangement.getArrangementWorkList()) {
							if (TOUKIBO_COPY_ARRANGEMENT_ID == arrangementWork.getArrangementWorkTypeMasterId()) {
								return WorkflowStatus.作業完了 == arrangementWork.getWorkflowStatus();
							}
						}
					}
				}

				return true;
			}).collect(Collectors.toList());

			if (0 == orderDataList.size()) {
				log.info(messageUtil.createMessageInfo("BatchTargetNoDataInfo", new String[] { "オーダーCSV作成" }).getMsg());
			} else {
				List<FindCreateOrderCsvDataDto> findOrderDataList = new ArrayList<>();
				// オーダーを契約番号ごとにグルーピングしたマップ
				// 契約番号ごとに枝番(連番)を付与するため契約番号でグルーピングしてforEachで回す
				Map<String, List<CreateOrderCsvDataDto>> contractNumberGroupingMap = orderDataList.stream().collect(Collectors.groupingBy(order -> order.getContractNumber(), Collectors.mapping(order -> order, Collectors.toList())));

				if ("1".equals(dto.getType())) {
					// 契約種別：新規 の場合
					// オーダーCSVは一行あたりに一商品一つずつを出力する
					// 同じ契約内では商品コードごとに契約IDの末尾に連番を付与する(同一商品の場合同一の契約IDとなる)
					contractNumberGroupingMap.entrySet().stream().forEach(orderDataMap -> {
						// 契約IDに紐づく契約明細の数だけ回す
						IntStream.range(0, orderDataMap.getValue().size()).forEach(i -> {
							CreateOrderCsvDataDto orderData = orderDataMap.getValue().get(i);
							// 契約明細の数量分だけ行を作成する
							int itemQuantity = Integer.parseInt(orderData.getQuantity());
							// 契約明細ごとに商品コードが分かれているので、iが求める連番になる
							IntStream.range(0, itemQuantity).forEach(k -> {
								FindCreateOrderCsvDataDto csvRowData = createOrderCsvRowDataForNew(orderData, operationDate, i);
								if (csvRowData != null) {
									findOrderDataList.add(csvRowData);
								}
							});
						});
					});
				} else {
					// 契約種別：容量変更 or 有償交換 の場合
					// オーダーCSVは一行あたりに一商品一つずつを出力する
					// 同じ契約内では商品コードごとに契約IDの末尾に連番を付与する(同一商品の場合連番まで含めて同一の契約IDとなる)
					contractNumberGroupingMap.entrySet().stream().forEach(orderDataMap -> {
						IntStream.range(0, orderDataMap.getValue().size()).forEach(i -> {
							// 契約明細単位のオーダーデータを取得
							CreateOrderCsvDataDto orderData = orderDataMap.getValue().get(i);
							// 契約明細に載っている数量分だけ行を作成する
							int itemQuantity = Integer.parseInt(orderData.getQuantity());
							// 契約明細に紐づく品種コード
							String ricohItemCode = orderData.getRicohItemCode();
							// 数量分の行を一行ずつ作成する
							IntStream.range(0, itemQuantity).forEach(index -> {
								try {
									FindCreateOrderCsvDataDto csvRowData = createOrderCsvRowDataForPlanChange(orderData, operationDate, dto, i, index, ricohItemCode);
									if (csvRowData != null) {
										findOrderDataList.add(csvRowData);
									}
								} catch (JsonParseException e) {
									log.error(e.toString());
									Arrays.asList(e.getStackTrace()).stream().forEach(s -> log.error(s));
									throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "FileMappingFailed", new String[] { "JSONデータ" }));
								} catch (JsonMappingException e) {
									log.error(e.toString());
									Arrays.asList(e.getStackTrace()).stream().forEach(s -> log.error(s));
									throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "FileMappingFailed", new String[] { "JSONデータ" }));
								} catch (IOException e) {
									log.error(e.toString());
									Arrays.asList(e.getStackTrace()).stream().forEach(s -> log.error(s));
									throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "FileMappingFailed", new String[] { "JSONデータ" }));
								}
							});
						});
					});
				}

				// CSV出力に成功した契約IDのリスト
				List<Long> successIdList = new ArrayList<>();
				// CSV出力に失敗した契約IDのリスト
				List<Long> failedIdList = new ArrayList<>();

				// 出力対象オーダーを契約IDでグルーピングしたマップ
				Map<Long, List<FindCreateOrderCsvDataDto>> OrderDataIdGroupingMap = findOrderDataList.stream().collect(Collectors.groupingBy(findOrderData -> findOrderData.getContractIdTemp(), Collectors.mapping(findOrderData -> findOrderData, Collectors.toList())));
				CsvMapper mapper = new CsvMapper();
				CsvSchema schemaWithOutHeader = mapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true).schemaFor(FindCreateOrderCsvDataDto.class).withoutHeader().withColumnSeparator(',').withLineSeparator("\r\n").withNullValue("\"\"");

				// CSV出力
				OrderDataIdGroupingMap.entrySet().stream().sorted(Entry.comparingByKey()).forEach(map -> {
					try {
						mapper.writer(schemaWithOutHeader).writeValues(Files.newBufferedWriter(dto.getTmpFile().toPath(), Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND)).write(map.getValue());
						successIdList.add(map.getKey());
					} catch (Exception e) {
						log.error(messageUtil.createMessageInfo("BatchCannotCreateFiles", new String[] { String.format("契約ID=%dのオーダーCSV作成", map.getKey()) }).getMsg(), e);
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
						try {
							// 契約情報明細取得API
							Contract contract = restApiClient.callFindOneContractApi(contractId);
							contract.getContractDetailList().forEach(ContractDetail -> {
								ContractDetail.setExtendsParameter(successExtendsParameter);
							});
							try {
								restApiClient.callContractApi(contract);
							} catch (Exception e) {
								errorDataIdList.add(contractId);
								log.fatal(String.format("契約ID=%dが契約情報更新APIに失敗しました。", contractId), e);
							}
						} catch (Exception e) {
							errorDataIdList.add(contractId);
							log.fatal(String.format("契約ID=%dが契約情報明細取得APIに失敗しました。", contractId), e);
						}
					});

					// エラー発生個所
					// 事後処理（手配）
					successIdList.stream().forEach(ContractId -> {
						List<Long> arrangementWorkIdListAssign = new ArrayList<>();
						List<Long> arrangementWorkIdListAccept = new ArrayList<>();
						Arrangement arrangement = arrangementRepository.findByContractIdAndDisengagementFlg(ContractId, 0);
						if (arrangement != null) {
							List<ArrangementWork> arrangementWorkList = arrangement.getArrangementWorkList();
							arrangementWorkList.stream().forEach(arrangementWork -> {
								// 手配業務タイプ=業務区登記簿コピー添付の手配業務タイプマスタIDでない手配についてのみ、担当者登録APIと手配業務受付APIを実施する
								if (arrangementWork.getArrangementWorkTypeMasterId() != TOUKIBO_COPY_ARRANGEMENT_ID) {
									if (arrangementWork.getArrangementPicWorkerEmp() == null) {
										arrangementWorkIdListAssign.add(arrangementWork.getId());
									}
									if (arrangementWork.getWorkflowStatus() == WorkflowStatus.受付待ち) {
										arrangementWorkIdListAccept.add(arrangementWork.getId());
									}
								}
							});
						}
						// 手配担当者登録APIを実行
						try {
							restApiClient.callAssignWorker(arrangementWorkIdListAssign);
						} catch (Exception arrangementError) {
							errorDataIdList.add(ContractId);
							log.fatal(String.format("担当者登録に失敗しました。"), arrangementError);
						}
						// 手配業務受付APIを実行
						try {
							restApiClient.callAcceptWorkApi(arrangementWorkIdListAccept);
						} catch (Exception arrangementError) {
							errorDataIdList.add(ContractId);
							log.fatal(String.format("ステータスの変更に失敗しました。"), arrangementError);
						}
					});
				}

				// 出力失敗
				if (!failedIdList.isEmpty()) {
					// failedIdListはCSV作成でエラーとなったデータが入っているので、最終的にエラーの有無を判断するerrorDataIdListにadd
					errorDataIdList.addAll(failedIdList);
					// 事後処理（拡張項目）
					Map<String, Object> failedMap = new HashMap<>();
					String failedExtendsParameter = "{\"orderCsvCreationStatus\":\"2\",\"orderCsvCreationDate\":\"\"}";
					List<Long> contractDetailIdList = orderDataList.stream().filter(o -> failedIdList.contains(o.getContractIdTemp())).map(o -> o.getContractDetailId()).collect(Collectors.toList());

					failedMap.put("extendsParam", failedExtendsParameter);
					failedMap.put("idList", contractDetailIdList);
					try {
						dbUtil.execute("sql/updateExtendsParameter.sql", failedMap);
					} catch (RuntimeException e) {
						failedIdList.stream().forEach(contractId -> {
							log.fatal(String.format("契約ID=%dの更新SQLの実行に失敗しました。", contractId), e);
						});
					}
				}
			}
		}
		return errorDataIdList.isEmpty();
	}

	/**
	 * オーダーCSV行作成(契約種別：新規)
	 * 
	 * @param orderData
	 *            オーダーデータ
	 * @param operationDate
	 *            処理日
	 * @param serialCount
	 *            契約番号末尾に付与する連番
	 * @return オーダーCSV行
	 */
	public FindCreateOrderCsvDataDto createOrderCsvRowDataForNew(CreateOrderCsvDataDto orderData, Date operationDate, int serialCount) {

		if (orderData == null) {
			return null;
		}

		FindCreateOrderCsvDataDto orderCsvEntity = new FindCreateOrderCsvDataDto();
		orderCsvEntity.setContractIdTemp(orderData.getContractIdTemp());
		orderCsvEntity.setContractDetailId(orderData.getContractDetailId());
		orderCsvEntity.setContractId(orderData.getContractNumber() + String.format("%02d", orderData.getContractBranchNumber()) + String.format("%03d", serialCount + 1));
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

	/**
	 * オーダーCSV行作成(契約種別：容量変更,有償交換)
	 * 
	 * @param orderData
	 *            オーダーデータ
	 * @param operationDate
	 *            処理日
	 * @param dto
	 *            バッチ起動引数のDTO
	 * @param serialCount
	 *            契約番号末尾に付与する連番
	 * @param index
	 *            拡張項目繰返のインデックス
	 * @param targetRicohItemCode
	 *            オーダーCSVに載せる対象のリコー品種コード
	 * @return オーダーCSV行
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public FindCreateOrderCsvDataDto createOrderCsvRowDataForPlanChange(CreateOrderCsvDataDto orderData, Date operationDate, CreateOrderCsvDto dto, int serialCount, int index, String targetRicohItemCode) throws JsonParseException, JsonMappingException, IOException {

		if (orderData == null) {
			return null;
		}

		// オーダーCSV行
		FindCreateOrderCsvDataDto orderCsvRow = new FindCreateOrderCsvDataDto();

		if (!StringUtil.isEmpty(orderData.getExtendsParameterIterance())) {
			List<SIMExtendsParameterIteranceDto> extendsParameterIteranceList = readJson(orderData.getExtendsParameterIterance());

			// 商品コードの昇順にソート
			Collections.sort(extendsParameterIteranceList, new ExtendsParameterDtoComparator());

			// フィルター用契約種別(ラムダ式内部で利用するため配列で定義)
			String[] filterContractType = { "容量変更" };
			// 商品名用の接頭語
			String prefix = "【容量変更】";
			// バッチ起動引数の種別によってフィルター用契約種別を決定
			if ("3".equals(dto.getType())) {
				filterContractType[0] = "有償交換";
				prefix = "【再発行】";
			}

			// 対象商品コードで絞り込んだリスト
			// filter:商品コード=対象商品コード
			// filter:契約種別=フィルター用契約種別
			List<SIMExtendsParameterIteranceDto> targetItemCodeParameterList = extendsParameterIteranceList.stream().filter(e -> targetRicohItemCode.equals(e.getProductCode())).filter(e -> filterContractType[0].equals(e.getContractType())).collect(Collectors.toList());

			// 容量変更・有償交換のオーダーCSVは「新規」の行を含まない
			// index(明細数)より拡張項目繰返を対象商品コードで絞り込んだリストの方が少ない場合はCSV行を作成しない
			if (!CollectionUtils.isEmpty(targetItemCodeParameterList) && targetItemCodeParameterList.size() > index) {
				orderCsvRow.setContractIdTemp(orderData.getContractIdTemp());
				orderCsvRow.setContractDetailId(orderData.getContractDetailId());
				orderCsvRow.setContractId(orderData.getContractNumber() + String.format("%02d", orderData.getContractBranchNumber()) + String.format("%03d", serialCount + 1));
				orderCsvRow.setRicohItemCode(orderData.getRicohItemCode());
				orderCsvRow.setItemContractName(prefix + orderData.getItemContractName());
				orderCsvRow.setOrderDate(batchUtil.changeFormatString(operationDate));
				orderCsvRow.setConclusionPreferredDate(batchUtil.changeFormatString(orderData.getConclusionPreferredDate()));
				orderCsvRow.setPicName(orderData.getPicName());
				orderCsvRow.setPicNameKana(orderData.getPicNameKana());
				orderCsvRow.setPostNumber(orderData.getPostNumber());
				orderCsvRow.setAddress(orderData.getAddress());
				orderCsvRow.setCompanyName(orderData.getCompanyName());
				orderCsvRow.setOfficeName(orderData.getOfficeName());
				orderCsvRow.setPicPhoneNumber(orderData.getPicPhoneNumber());
				orderCsvRow.setPicFaxNumber(orderData.getPicFaxNumber());
				orderCsvRow.setPicMailAddress(orderData.getPicMailAddress());
				orderCsvRow.setLineNumber(targetItemCodeParameterList.get(index).getLineNumber());
				// ICCIDは容量変更の場合のみ設定する
				if ("2".equals(dto.getType())) {
					orderCsvRow.setSerialNumber(targetItemCodeParameterList.get(index).getSerialNumber());
				} else {
					orderCsvRow.setSerialNumber("");
				}
				orderCsvRow.setDeliveryExpectedDate("");
				orderCsvRow.setInvoiceNumber("");
				orderCsvRow.setRemarks("");

				return orderCsvRow;
			}
		}

		return null;
	}

	/**
	 * 拡張項目文字列をオブジェクトに変換する
	 * 
	 * @param extendsParameterIterance
	 *            拡張項目繰返(文字列)
	 * @return 拡張項目繰返オブジェクト
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private List<SIMExtendsParameterIteranceDto> readJson(String extendsParameterIterance) throws JsonParseException, JsonMappingException, IOException {
		HashMap<String, HashMap<String, Object>> basicContentsJsonMap = om.readValue(extendsParameterIterance, new TypeReference<Object>() {
		});

		String extendsJson = om.writeValueAsString(basicContentsJsonMap.get("extendsParameterList"));
		List<SIMExtendsParameterIteranceDto> extendsParameterList = om.readValue(extendsJson, new TypeReference<List<SIMExtendsParameterIteranceDto>>() {
		});

		return extendsParameterList;
	}
}
