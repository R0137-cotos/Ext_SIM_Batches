package jp.co.ricoh.cotos.component.sim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.entity.arrangement.Arrangement;
import jp.co.ricoh.cotos.commonlib.entity.arrangement.ArrangementWork;
import jp.co.ricoh.cotos.commonlib.entity.arrangement.ArrangementWork.WorkflowStatus;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ProductContract;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.commonlib.logic.mail.CommonSendMail;
import jp.co.ricoh.cotos.commonlib.repository.arrangement.ArrangementRepository;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractRepository;
import jp.co.ricoh.cotos.component.BatchUtil;
import jp.co.ricoh.cotos.component.base.BatchStepComponent;
import jp.co.ricoh.cotos.dto.ExtendsParameterDto;
import jp.co.ricoh.cotos.dto.ReplyOrderDto;
import lombok.extern.log4j.Log4j;

@Component("SIM")
@Log4j
public class BatchStepComponentSim extends BatchStepComponent {

	@Autowired
	CommonSendMail commonSendMail;

	@Autowired
	BatchUtil batchUtil;

	@Autowired
	CheckUtil checkUtil;

	@Autowired
	DBUtil dbUtil;

	@Autowired
	ObjectMapper om;

	@Autowired
	ArrangementRepository arrangementRepository;

	@Autowired
	ContractRepository contractRepository;

	@Autowired
	EntityManager em;

	@Override
	public List<ReplyOrderDto> beforeProcess(String[] args) throws IOException {
		log.info("SIM独自処理");

		// バッチパラメーターのチェックを実施
		if (null == args || args.length != 2) {
			return null;
		}

		File csvFile = Paths.get(args[0], args[1]).toFile();

		if (csvFile == null || !csvFile.exists()) {
			return null;
		}

		// リプライCSV読込
		CsvMapper mapper = new CsvMapper();
		mapper.setTimeZone(om.getDeserializationConfig().getTimeZone());
		CsvSchema schema = CsvSchema.emptySchema().withoutHeader();
		CsvSchema quoteSchema = mapper.schemaFor(ReplyOrderDto.class).withoutQuoteChar();
		MappingIterator<ReplyOrderDto> it = mapper.readerFor(ReplyOrderDto.class).with(schema).with(quoteSchema).readValues(new InputStreamReader(new FileInputStream(csvFile), Charset.forName("Shift_JIS")));
		List<ReplyOrderDto> csvlist = it.readAll();

		return csvlist;
	}

	/**
	 * @return true: 処理成功、false: 処理失敗
	 */
	@Override
	@Transactional
	public boolean process(List<ReplyOrderDto> csvlist) throws JsonProcessingException, FileNotFoundException, IOException {
		log.info("SIM独自処理");

		// ラムダ式内でラムダ式外で設定された変数が変更できないのでエラーリストを作成する。
		List<Boolean> errorList = new ArrayList();

		if (CollectionUtils.isEmpty(csvlist)) {
			log.info("取込データが0件のため処理を終了します");
			return true;
		}

		// 枝番削除した契約番号をキーとしたMap
		// リプライCSVの枝番削除した契約番号(契約番号の上位17桁)が更新対象契約番号
		Map<String, List<ReplyOrderDto>> contractNumberGroupingMapFromCsv = csvlist.stream().collect(Collectors.groupingBy(dto -> substringContractNumber(dto.getContractId()), Collectors.mapping(dto -> dto, Collectors.toList())));
		// 枝番削除した契約番号のリスト
		List<String> contractNumberListFromCsv = contractNumberGroupingMapFromCsv.entrySet().stream().map(map -> map.getKey()).map(c -> substringContractNumber(c)).collect(Collectors.toList());

		// 対象契約取得
		Map<String, Object> queryParams = new HashMap<>();
		StringJoiner joiner = new StringJoiner("','", "'", "'").setEmptyValue("");
		contractNumberListFromCsv.stream().forEach(conNumLst -> joiner.add(conNumLst));
		queryParams.put("contractNumberList", joiner.toString());
		List<Contract> contractList = dbUtil.loadFromSQLFile("sql/findTargetContract.sql", Contract.class, queryParams);

		// 契約の数が0件だった場合、異常終了する
		if (contractList.isEmpty()) {
			log.error("対象契約データが0件のため、異常終了しました。");
			return false;
		}
		// csvから取得した契約番号の数と契約の数が一致していなければ、異常終了する
		// 取得できた契約は後続の処理を実行する
		if (contractList.size() != contractNumberListFromCsv.size()) {
			log.error("対象契約データが一部存在しないため、異常終了しました。");
			errorList.add(false);
		}

		// 全解約分の抽出
		// filter:ライフサイクル=解約予定日待ち
		List<Contract> allCancelList = contractList.stream().filter(e -> Contract.LifecycleStatus.解約予定日待ち.toString().equals(e.getLifecycleStatus().toString())).collect(Collectors.toList());

		// 数量減分の抽出
		// filter:ライフサイクル=作成完了
		List<Contract> partCancelList = contractList.stream().filter(e -> Contract.LifecycleStatus.作成完了.toString().equals(e.getLifecycleStatus().toString())).collect(Collectors.toList());

		// 契約番号でグルーピングしたマップ(全解約分)
		Map<String, List<Contract>> allCancelListContractNumberGroupingMap = allCancelList.stream().collect(Collectors.groupingBy(order -> order.getContractNumber(), Collectors.mapping(order -> order, Collectors.toList())));

		// 契約番号でグルーピングしたマップ(数量減分)
		Map<String, List<Contract>> partCancelListContractNumberGroupingMap = partCancelList.stream().collect(Collectors.groupingBy(order -> order.getContractNumber(), Collectors.mapping(order -> order, Collectors.toList())));

		// 全解約分のマップを回す
		allCancelListContractNumberGroupingMap.entrySet().stream().forEach(contractMap -> {
			IntStream.range(0, contractMap.getValue().size()).forEach(i -> {
				Contract contract = contractMap.getValue().get(i);

				// ロールバック用に更新元の契約情報を退避
				Contract originalContract = null;
				try {
					// 契約をディープコピー
					originalContract = om.readValue(om.writeValueAsString(contract), Contract.class);
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// 解約確定日を設定
				contract.setCancelDecisionDate(contract.getCancelScheduledDate());
				// ライフサイクル状態を設定
				contract.setLifecycleStatus(Contract.LifecycleStatus.解約予定日待ち);

				// 手配情報業務完了エラーか
				boolean hasNoArrangementError = false;

				// 契約情報更新処理を実施
				if (callUpdateContractApi(contract)) {
					// 成功した場合 手配情報業務完了処理を実施
					hasNoArrangementError = callCompleteArrangementApi(contract, true);
				} else {
					errorList.add(false);
					// 失敗した場合スキップする
					return;
				}
				// 手配情報業務完了処理がエラーの場合、元の契約情報で更新した契約情報を再更新する
				if (!hasNoArrangementError) {
					if (!callUpdateContractApi(originalContract)) {
						// 再更新に失敗した場合手動リカバリが必要となる
						log.fatal(String.format("契約ID=%dの契約再更新に失敗しました。リカバリが必要となります。", originalContract.getId()));
						errorList.add(false);
					}
				}
			});
		});

		// 数量減分のマップを回す
		partCancelListContractNumberGroupingMap.entrySet().stream().forEach(contractMap -> {
			IntStream.range(0, contractMap.getValue().size()).forEach(i -> {
				Contract contract = contractMap.getValue().get(i);

				// JSON読み込みエラーか
				boolean hasJsonError = false;

				List<ProductContract> productContractList = contract.getProductContractList();
				// ワークフロー状態を設定
				contract.setWorkflowStatus(Contract.WorkflowStatus.売上可能);
				// サービス開始日にサービス利用希望日の翌月1日を設定
				contract.setServiceTermStart(getNextMonthFirstDay(contract.getConclusionPreferredDate()));

				// 拡張項目繰り返しを設定
				for (ProductContract p : productContractList) {
					// 商品(契約用)から拡張項目繰返を取得
					String extendsParameterIterance = p.getExtendsParameterIterance();
					Function<String, List<ExtendsParameterDto>> readJsonFunc = batchUtil.Try(x -> batchUtil.readJson(x), (error, x) -> null);
					// 取得した拡張項目繰返をDtoのListに変換
					List<ExtendsParameterDto> extendsParameterList = readJsonFunc.apply(extendsParameterIterance);
					if (CollectionUtils.isEmpty(extendsParameterList)) {
						log.fatal(String.format("契約ID=%dの商品拡張項目繰返読込に失敗しました。", contract.getId()));
						hasJsonError = true;
						errorList.add(false);
						return;
					}

					// 拡張項目繰返更新用リスト
					List<ExtendsParameterDto> updatedExtendsParameterList = new ArrayList<>();

					// 拡張項目繰返の内、更新対象であるリスト
					List<ExtendsParameterDto> targetList = extendsParameterList.stream().filter(e -> "解約".equals(trimDoubleQuote(e.getContractType()))).collect(Collectors.toList());
					// 拡張項目繰返の内、更新対象でないリスト
					List<ExtendsParameterDto> notTargetList = extendsParameterList.stream().filter(e -> !"解約".equals(trimDoubleQuote(e.getContractType()))).collect(Collectors.toList());
					if (targetList != null) {
						// 拡張項目繰返の内、更新対象行の解約日にサービス利用希望日を設定して拡張項目繰返更新用リストに格納
						IntStream.range(0, targetList.size()).forEach(j -> {
							targetList.get(j).setCancelDate(contract.getConclusionPreferredDate().toString());
							updatedExtendsParameterList.add(targetList.get(j));
						});
					}
					if (notTargetList != null) {
						// 拡張項目繰返の内、更新対象でないリストは更新を行わず拡張項目繰返更新用リストに格納
						IntStream.range(0, notTargetList.size()).forEach(j -> {
							updatedExtendsParameterList.add(notTargetList.get(j));
						});
					}

					// 拡拡張項目繰返更新用リストをIDの昇順でソート
					if (!CollectionUtils.isEmpty(updatedExtendsParameterList)) {
						updatedExtendsParameterList.sort((a, b) -> (int) a.getId() - (int) b.getId());
					}

					Map<String, List<ExtendsParameterDto>> extendsParameterMap = new HashMap<>();
					extendsParameterMap.put("extendsParameterList", updatedExtendsParameterList);
					try {
						p.setExtendsParameterIterance(om.writeValueAsString(extendsParameterMap));
					} catch (JsonProcessingException e) {
						e.printStackTrace();
						log.fatal(String.format("契約ID=%dの商品拡張項目登録に失敗しました。", contract.getId()));
						hasJsonError = true;
						errorList.add(false);
						return;
					}
				}
				contract.setProductContractList(productContractList);

				// JSONエラーが存在する場合は契約情報更新・手配情報業務完了をスキップする
				if (hasJsonError) {
					return;
				}

				// 手配情報更新処理を実施
				// 数量減の場合はワークフロー状態を売上可能に更新するため、手配情報更新→契約情報更新の順に処理する必要がある
				if (callCompleteArrangementApi(contract, false)) {
					// 成功した場合 契約情報更新処理を実施
					if (!callUpdateContractApi(contract)) {
						// 失敗した場合エラーログを出力しスキップする
						log.fatal(String.format("契約ID=%dの契約更新に失敗しました。リカバリが必要となります。", contract.getId()));
						errorList.add(false);
						return;
					}
				} else {
					log.fatal(String.format("契約ID=%dの手配情報更新に失敗しました。", contract.getId()));
					errorList.add(false);
				}
			});
		});
		// エンティティ(contract)に対して値を更新すると、エンティティマネージャーが更新対象とみなしてしまい、排他制御に引っかかる
		em.clear();

		return errorList.isEmpty();
	}

	/**
	 * 契約情報更新API呼び出し
	 * @param contract 契約情報
	 * @return true:API実行結果エラー無し false:API実行結果エラー有り
	 */
	private boolean callUpdateContractApi(Contract contract) {
		if (contract == null) {
			return false;
		}

		try {
			batchUtil.callUpdateContract(contract);
			return true;
		} catch (Exception updateError) {
			// 失敗した場合エラーログを出力
			log.fatal(String.format("契約ID=%dの契約情報更新に失敗しました。", contract.getId()), updateError);
			return false;
		}
	}

	/**
	 * 手配情報業務完了API呼び出し
	 * @param contract 契約情報
	 * @param allCancelFlg 全解約フラグ
	 * @return true:API実行結果エラー無し false:API実行結果エラー有り
	 */
	private boolean callCompleteArrangementApi(Contract contract, boolean allCancelFlg) {
		if (contract == null) {
			return false;
		}

		Arrangement arrangement = arrangementRepository.findByContractIdAndDisengagementFlg(contract.getId(), allCancelFlg ? 1 : 0);
		if (arrangement == null) {
			log.fatal(String.format("契約ID=%dの手配情報が存在しません。", contract.getId()));
			return false;
		}

		// エラー無しか ラムダ式で利用するため配列とする
		boolean[] hasNoError = { true };

		List<ArrangementWork> arrangementWorkList = arrangement.getArrangementWorkList();
		arrangementWorkList.stream().forEach(work -> {
			List<Long> arrangementWorkIdList = new ArrayList<>();
			if (work.getArrangementPicWorkerEmp() == null && work.getWorkflowStatus() == WorkflowStatus.受付待ち) {
				arrangementWorkIdList.add(work.getId());
			}

			try {
				// 手配担当者登録APIを実行
				batchUtil.callAssignWorker(arrangementWorkIdList);
				// 手配業務受付APIを実行
				batchUtil.callAcceptWorkApi(arrangementWorkIdList);
				// 手配情報業務完了APIを実行
				batchUtil.callCompleteArrangement(work.getId());
			} catch (Exception arrangementError) {
				log.fatal(String.format("契約ID=%dの手配情報業務完了に失敗したため、処理をスキップします。", contract.getId()), arrangementError);
				hasNoError[0] = false;
			}
		});

		return hasNoError[0];
	}

	/**
	 * 枝番削除した契約番号(契約番号の上位17桁)を返す
	 * @param number 契約番号
	 * @return 枝番削除した契約番号
	 */
	private String substringContractNumber(String number) {
		if (number == null) {
			return null;
		}
		number = trimDoubleQuote(number);
		if (number.length() < 17) {
			return null;
		}
		return number.substring(0, 17);
	}

	/**
	 * 文字列からダブルクオートを削除する
	 * @param str 文字列
	 * @return ダブルクオート削除した文字列
	 */
	private String trimDoubleQuote(String str) {
		if (str == null) {
			return null;
		}
		return str.replaceAll("\"", "");
	}

	/**
	 * 翌月月初日を取得
	 * @param date 対象日付
	 * @return 対象日付の翌月月初日
	 */
	private Date getNextMonthFirstDay(Date date) {
		if (date == null) {
			return null;
		}
		Date safeDate = new Date(date.getTime());
		LocalDate localDate = safeDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate nextMonthFirstDay = LocalDate.of(localDate.getYear(), localDate.getMonth().plus(1), 1);
		return Date.from(nextMonthFirstDay.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

}
