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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.logic.businessday.BusinessDayUtil;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.commonlib.logic.message.MessageUtil;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractRepository;
import jp.co.ricoh.cotos.commonlib.repository.master.NonBusinessDayCalendarMasterRepository;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.CancelOrderCsvDto;
import jp.co.ricoh.cotos.dto.CancelOrderEntity;
import jp.co.ricoh.cotos.dto.CreateOrderCsvParameter;
import jp.co.ricoh.cotos.dto.ExtendsParameterDtoComparator;
import jp.co.ricoh.cotos.dto.SIMExtendsParameterIteranceDto;
import lombok.extern.log4j.Log4j;

@Component("SIM")
@Log4j
public class BatchStepComponentSim extends BatchStepComponent {

	@Autowired
	NonBusinessDayCalendarMasterRepository nonBusinessDayCalendarMasterRepository;

	@Autowired
	ContractRepository contractRepository;

	@Autowired
	MessageUtil messageUtil;

	@Autowired
	DBUtil dbUtil;

	@Autowired
	BusinessDayUtil businessDayUtil;

	@Autowired
	CheckUtil checkUtil;

	@Autowired
	ObjectMapper om;

	// CSVヘッダファイル
	private static final String headerFilePath = "file/header.csv";

	/**
	 * 解約オーダー取得
	 * @return 解約オーダーリスト
	 */
	@Override
	public List<CancelOrderEntity> getDataList() {
		return dbUtil.loadFromSQLFile("sql/findCancelOrder.sql", CancelOrderEntity.class);
	}

	/**
	 * 解約手配CSV作成処理
	 * @param param バッチ処理引数
	 * @param cancelOrderList 解約オーダーリスト
	 */
	@Override
	public void process(CreateOrderCsvParameter param, List<CancelOrderEntity> cancelOrderList) throws ParseException, JsonProcessingException, IOException {
		log.info("SIM独自処理");

		// 解約オーダーリスト・処理実行日が存在しない場合は処理を行わない
		if (!CollectionUtils.isEmpty(cancelOrderList) && param.getOperationDate() != null) {
			// yyyyMMフォーマッター
			DateTimeFormatter yyyyMMFormatter = DateTimeFormatter.ofPattern("yyyyMM");
			DateTimeFormatter yyyyMMddFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

			// オーダー日 処理年月日yyyyMMdd
			String orderDate = param.getOperationDate().format(yyyyMMddFormatter);
			// 解約月 処理年月yyyyMM
			String cancelMonth = param.getOperationDate().format(yyyyMMFormatter);

			// 処理年月日当月最終営業日
			Date lastBusinessDayOfTheMonth = businessDayUtil.getLastBusinessDayOfTheMonthFromNonBusinessCalendarMaster(cancelMonth);
			if (lastBusinessDayOfTheMonth == null) {
				log.info(messageUtil.createMessageInfo("BatchTargetNoDataInfo", new String[] { "解約手配CSV作成" }).getMsg());
				return;
			}
			// 処理年月日当月末日
			LocalDate lastDayOfTheMonth = LocalDate.of(param.getOperationDate().getYear(), param.getOperationDate().getMonthValue(), LocalDate.of(param.getOperationDate().getYear(), param.getOperationDate().getMonthValue(), 1).lengthOfMonth());

			// 全解約分の抽出
			// filter:ライフサイクル=解約予定日待ち
			// filter:解約予定日=処理年月日当月末日
			List<CancelOrderEntity> allCancelList = cancelOrderList.stream().filter(e -> Contract.LifecycleStatus.解約予定日待ち.toString().equals(e.getLifecycleStatus())).filter(e -> e.getCancelScheduledDate() != null).filter(e -> e.getCancelScheduledDate().equals(lastDayOfTheMonth)).collect(Collectors.toList());

			// 数量減分の抽出
			// filter:ライフサイクル=作成完了
			// filter:契約種別=契約変更
			// filter:サービス利用希望日=処理年月日当月末日
			List<CancelOrderEntity> partCancelList = cancelOrderList.stream().filter(e -> Contract.LifecycleStatus.作成完了.toString().equals(e.getLifecycleStatus())).filter(e -> Contract.ContractType.契約変更.toString().equals(e.getContractType())).filter(e -> e.getConclusionPreferredDate() != null).filter(e -> e.getConclusionPreferredDate().equals(lastDayOfTheMonth)).collect(Collectors.toList());

			// CSV出力用DTOリスト
			List<CancelOrderCsvDto> csvDtoList = new ArrayList<>();

			// 契約番号でグルーピングしたマップ(全解約分)
			Map<String, List<CancelOrderEntity>> allCancelListContractNumberGroupingMap = allCancelList.stream().collect(Collectors.groupingBy(order -> order.getContractNumber(), Collectors.mapping(order -> order, Collectors.toList())));

			// 契約番号でグルーピングしたマップ(数量減分)
			Map<String, List<CancelOrderEntity>> partCancelListContractNumberGroupingMap = partCancelList.stream().collect(Collectors.groupingBy(order -> order.getContractNumber(), Collectors.mapping(order -> order, Collectors.toList())));

			// 枝番用インデックス ラムダ式の内部で利用するため配列にする
			int[] branchNumberIndex = { 0 };

			// 全解約分のマップを回す
			allCancelListContractNumberGroupingMap.entrySet().stream().forEach(orderDataMap -> {
				IntStream.range(0, orderDataMap.getValue().size()).forEach(i -> {

					CancelOrderEntity orderData = orderDataMap.getValue().get(i);
					List<SIMExtendsParameterIteranceDto> extendsParameterIteranceDtoList = new ArrayList<>();
					// 拡張項目繰返を読み込み
					if (orderData.getExtendsParameterIterance() != null) {
						try {
							extendsParameterIteranceDtoList = readJson(orderData.getExtendsParameterIterance());
						} catch (JsonParseException e) {
							e.printStackTrace();
							throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "FileMappingFailed", new String[] { "JSONデータ" }));
						} catch (JsonMappingException e) {
							e.printStackTrace();
							throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "FileMappingFailed", new String[] { "JSONデータ" }));
						} catch (IOException e) {
							e.printStackTrace();
							throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "FileMappingFailed", new String[] { "JSONデータ" }));
						}
					}

					// 追加行有無判定のためCSVのリストサイズを保存
					int csvListSize = 0;
					if (csvDtoList != null) {
						csvListSize = csvDtoList.size();
					}

					// 契約と商品(契約用)は1:1
					// 契約に紐づく複数の契約明細それぞれの商品を全て商品(契約用).拡張項目繰返にまとめて持ってる
					// よって、CSVの各行データを作成する際には、拡張項目繰返の内同じ品種の行だけ抽出する
					// filter:契約.リコー品種コード=拡張項目繰返.商品コード
					extendsParameterIteranceDtoList.stream().filter(epi -> orderData.getRicohItemCode().equals(epi.getProductCode())).forEach(j -> {
						CancelOrderCsvDto orderCsvEntity = new CancelOrderCsvDto();
						orderCsvEntity.setContractIdTemp(orderData.getContractIdTemp());
						orderCsvEntity.setContractId(orderData.getContractNumber() + String.format("%02d", orderData.getContractBranchNumber()) + String.format("%03d", branchNumberIndex[0] + 1));
						orderCsvEntity.setRicohItemCode(orderData.getRicohItemCode());
						orderCsvEntity.setItemContractName(orderData.getItemContractName());
						orderCsvEntity.setOrderDate(orderDate);
						orderCsvEntity.setCancelMonth(cancelMonth);
						orderCsvEntity.setLineNumber(j.getLineNumber());
						orderCsvEntity.setSerialNumber(j.getSerialNumber());

						csvDtoList.add(orderCsvEntity);
					});

					// CSVリストに追加が発生した場合枝番用インデックスを加算する
					if (csvDtoList != null && csvListSize != csvDtoList.size()) {
						branchNumberIndex[0] = branchNumberIndex[0] + 1;
					}
				});
			});



			// 数量減分のマップを回す
			partCancelListContractNumberGroupingMap.entrySet().stream().forEach(orderDataMap -> {
				// 契約番号ごとに1から始めるので、枝番用インデックスを初期化
				branchNumberIndex[0] = 0;

				// 契約番号に紐づく商品(契約用).拡張項目繰返は一種類しか存在しないので、一つ目を参照すれば良い
				CancelOrderEntity orderData = orderDataMap.getValue().get(0);
				List<SIMExtendsParameterIteranceDto> extendsParameterIteranceDtoList = new ArrayList<>();
				// 拡張項目繰返を読み込み
				if (orderData.getExtendsParameterIterance() != null) {
					try {
						// 数量減はSQLでデータ取得する段階で拡張項目繰返を読み込んで検索しているので、拡張項目繰返がJSONでない場合に発生するエラーは基本的に発生しない
						extendsParameterIteranceDtoList = readJson(orderData.getExtendsParameterIterance());
					} catch (JsonParseException e) {
						e.printStackTrace();
						throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "FileMappingFailed", new String[] { "JSONデータ" }));
					} catch (JsonMappingException e) {
						e.printStackTrace();
						throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "FileMappingFailed", new String[] { "JSONデータ" }));
					} catch (IOException e) {
						e.printStackTrace();
						throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "FileMappingFailed", new String[] { "JSONデータ" }));
					}
				}

				// 拡張項目繰返リストを契約番号の昇順にソート
				Collections.sort(extendsParameterIteranceDtoList, new ExtendsParameterDtoComparator());

				// 最後に追加した商品コード
				String[] lastProductCode = { "" };

				// 数量減の場合は種別が解約の行のみ出力対象
				// filter:拡張項目繰返.種別=解約
				extendsParameterIteranceDtoList.stream().filter(epi -> "解約".equals(epi.getContractType())).forEach(j -> {
					// 最後に追加した商品コードでない場合は、契約番号枝番をインクリメント
					if (!lastProductCode[0].equals(j.getProductCode())) {
						branchNumberIndex[0] = branchNumberIndex[0] + 1;
					}
					CancelOrderCsvDto orderCsvEntity = new CancelOrderCsvDto();
					orderCsvEntity.setContractIdTemp(orderData.getContractIdTemp());
					orderCsvEntity.setContractId(orderData.getContractNumber() + String.format("%02d", orderData.getContractBranchNumber()) + String.format("%03d", branchNumberIndex[0]));
					orderCsvEntity.setRicohItemCode(j.getProductCode());
					orderCsvEntity.setItemContractName(j.getProductName());
					orderCsvEntity.setOrderDate(orderDate);
					orderCsvEntity.setCancelMonth(cancelMonth);
					orderCsvEntity.setLineNumber(j.getLineNumber());
					orderCsvEntity.setSerialNumber(j.getSerialNumber());

					csvDtoList.add(orderCsvEntity);

					// 最後に追加した商品コードを更新
					if (j.getProductCode() != null) {
						lastProductCode[0] = j.getProductCode();
					} else {
						lastProductCode[0] = "";
					}
				});
			});

			CsvMapper mapper = new CsvMapper();
			CsvSchema schemaWithOutHeader = mapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true).schemaFor(CancelOrderCsvDto.class).withoutHeader().withColumnSeparator(',').withLineSeparator("\r\n").withNullValue("\"\"");

			if (CollectionUtils.isEmpty(csvDtoList)) {
				// 処理対象データ無し
				log.info(messageUtil.createMessageInfo("BatchTargetNoDataInfo", new String[] { "解約手配CSV作成" }).getMsg());
				return;
			}

			csvDtoList.stream().forEach(dto -> {
				try {
					mapper.writer(schemaWithOutHeader).writeValues(Files.newBufferedWriter(param.getTmpFile().toPath(), Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND)).write(dto);
				} catch (Exception e) {
					log.error(messageUtil.createMessageInfo("BatchCannotCreateFiles", new String[] { String.format("解約手配CSV作成") }).getMsg(), e);
				}
			});
			try {
				// CSVヘッダは固定なので、事前に用意したヘッダファイルとマージする
				List<String> outputList = Files.readAllLines(param.getTmpFile().toPath(), Charset.forName("UTF-8"));
				List<String> headerList = new ArrayList<>();
				InputStream in = this.getClass().getClassLoader().getResourceAsStream(headerFilePath);
				String header = IOUtils.toString(in, "UTF-8");
				headerList.add(header);
				headerList.addAll(outputList);
				try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(param.getCsvFile())))) {
					headerList.stream().forEach(s -> pw.print(s + "\r\n"));
				}
				Files.deleteIfExists(param.getTmpFile().toPath());
			} catch (IOException e) {
				log.error(messageUtil.createMessageInfo("BatchCannotCreateFiles", new String[] { String.format("解約手配CSV作成") }).getMsg(), e);
			}
		} else {
			// 処理対象データ無し
			log.info(messageUtil.createMessageInfo("BatchTargetNoDataInfo", new String[] { "解約手配CSV作成" }).getMsg());
		}
	}

	/**
	 * 拡張項目文字列をオブジェクトに変換する
	 * @param extendsParameterIterance 拡張項目繰返(文字列)
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
