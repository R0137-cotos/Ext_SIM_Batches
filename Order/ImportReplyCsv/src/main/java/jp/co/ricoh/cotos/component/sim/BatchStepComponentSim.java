package jp.co.ricoh.cotos.component.sim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import jp.co.ricoh.cotos.commonlib.dto.parameter.contract.ContractSearchParameter;
import jp.co.ricoh.cotos.commonlib.entity.arrangement.Arrangement;
import jp.co.ricoh.cotos.commonlib.entity.arrangement.ArrangementWork;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ProductContract;
import jp.co.ricoh.cotos.commonlib.logic.mail.CommonSendMail;
import jp.co.ricoh.cotos.commonlib.repository.arrangement.ArrangementRepository;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractRepository;
import jp.co.ricoh.cotos.component.BatchUtil;
import jp.co.ricoh.cotos.component.RestApiClient;
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
	RestApiClient restApiClient;

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

	@Override
	@Transactional
	public void process(List<ReplyOrderDto> csvlist) throws JsonProcessingException, FileNotFoundException, IOException {
		log.info("SIM独自処理");

		//枝番削除した契約番号をキーとしたMap
		Map<String, List<ReplyOrderDto>> contractNumberGroupingMap = csvlist.stream().collect(Collectors.groupingBy(dto -> substringContractNumber(dto.getContractId()), Collectors.mapping(dto -> dto, Collectors.toList())));
		//枝番削除した契約番号のリスト
		List<String> contractNumberList = contractNumberGroupingMap.entrySet().stream().map(map -> map.getKey()).map(c -> substringContractNumber(c)).collect(Collectors.toList());

		// 対象契約取得
		List<Contract> contractList = new ArrayList<>();
		contractNumberList.stream().forEach(conNumLst -> {
			// 契約情報取得 恒久契約識別番号
			try {
				ContractSearchParameter searchParam = new ContractSearchParameter();
				searchParam.setContractNumber(conNumLst.substring(0, 15));
				searchParam.setContractBranchNumber(conNumLst.substring(16, 17));
				restApiClient.callFindTargetContractList(searchParam).stream().forEach(contractTmp -> {
					contractList.add(restApiClient.callFindContract(contractTmp.getId()));
				});
			} catch (Exception updateError) {
				log.fatal(String.format("恒久契約識別番号=" + conNumLst + "の契約取得に失敗しました。", conNumLst));
				updateError.printStackTrace();
				return;
			}
		});
		Map<String, Contract> contractMapByContractNumber = contractList.stream().collect(Collectors.toMap(Contract::getContractNumber, con -> con));

		contractMapByContractNumber.entrySet().stream().forEach(contractMap -> {
			Contract contract = contractMap.getValue();

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

			List<ProductContract> productContractList = contractMap.getValue().getProductContractList();
			List<ReplyOrderDto> replyOrderList = contractNumberGroupingMap.get(contractMap.getValue().getContractNumber() + String.format("%02d", contractMap.getValue().getContractBranchNumber()));

			//サービス開始希望日を設定
			contract.setServiceTermStart(batchUtil.changeDate(replyOrderList.get(0).getDeliveryExpectedDate()));

			//拡張項目繰り返しを設定
			for (ProductContract p : productContractList) {
				String extendsParameterIterance = p.getExtendsParameterIterance();
				Function<String, List<ExtendsParameterDto>> readJsonFunc = batchUtil.Try(x -> batchUtil.readJson(x), (error, x) -> null);
				List<ExtendsParameterDto> extendsParameterList = readJsonFunc.apply(extendsParameterIterance);
				if (CollectionUtils.isEmpty(extendsParameterList)) {
					log.fatal(String.format("契約ID=%dの商品拡張項目読込に失敗しました。", contract.getId()));
					return;
				}
				long index = 1;
				List<ExtendsParameterDto> updatedExtendsParameterList = new ArrayList<>();
				for (ReplyOrderDto replyOrder : replyOrderList) {
					// デバイスが空欄の場合契約のデバイスを設定する
					if (replyOrder.getDevice() == null || replyOrder.getDevice() == "") {
						List<ExtendsParameterDto> TmpList = extendsParameterList.stream().filter(e -> e.getProductCode().equals(replyOrder.getRicohItemCode())).collect(Collectors.toList());
						for (ExtendsParameterDto Tmp : TmpList) {
							if (Tmp.getId() == index) {
								replyOrder.setDevice(Tmp.getDevice());
								System.out.println();
							}
						}
					}
					ExtendsParameterDto extendsParameterDto = null;
					// 新規:リプライCSVの商品コードが一致するかつ回線番号が存在しないデータを更新する
					List<ExtendsParameterDto> targetList = extendsParameterList.stream().filter(e -> e.getProductCode().equals(replyOrder.getRicohItemCode())).collect(Collectors.toList()).stream().filter(e -> "".equals(e.getLineNumber())).collect(Collectors.toList());
					if (!targetList.isEmpty()) {
						extendsParameterDto = addExtendsParameterDto(targetList, updatedExtendsParameterList, replyOrder);
						if (extendsParameterDto != null) {
							updatedExtendsParameterList.add(extendsParameterDto);
						}
					}

					// 容量変更:リプライCSVの商品コード、回線番号が一致するかつ送り状番号が未設定のデータを更新する
					targetList = extendsParameterList.stream().filter(e -> e.getProductCode().equals(replyOrder.getRicohItemCode())).collect(Collectors.toList()).stream().filter(e -> e.getLineNumber().equals(replyOrder.getLineNumber())).collect(Collectors.toList()).stream().filter(e -> "".equals(e.getInvoiceNumber())).collect(Collectors.toList());
					if (!targetList.isEmpty()) {
						extendsParameterDto = addExtendsParameterDto(targetList, updatedExtendsParameterList, replyOrder);
						if (extendsParameterDto != null) {
							updatedExtendsParameterList.add(extendsParameterDto);
						}
					}

					// 有償交換:リプライCSVの商品コード、回線番号が一致するかつ送り状番号が設定のデータを更新する
					targetList = extendsParameterList.stream().filter(e -> e.getProductCode().equals(replyOrder.getRicohItemCode())).collect(Collectors.toList()).stream().filter(e -> e.getLineNumber().equals(replyOrder.getLineNumber())).collect(Collectors.toList()).stream().filter(e -> !"".equals(e.getInvoiceNumber())).collect(Collectors.toList());
					if (!targetList.isEmpty()) {
						extendsParameterDto = addExtendsParameterDto(targetList, updatedExtendsParameterList, replyOrder);
						if (extendsParameterDto != null) {
							updatedExtendsParameterList.add(extendsParameterDto);
						}
					}
					index++;
				}

				// リプライCSVに存在しないデータを追加
				extendsParameterList.stream().forEach(e -> {
					if (updatedExtendsParameterList.stream().filter(f -> f.getId() == e.getId()).count() == 0) {
						updatedExtendsParameterList.add(e);
					}
				});

				// 拡張項目繰返への設定値をIDの昇順でソート
				if (updatedExtendsParameterList != null) {
					updatedExtendsParameterList.sort((a, b) -> (int) a.getId() - (int) b.getId());
				}
				Map<String, List<ExtendsParameterDto>> extendsParameterMap = new HashMap<>();
				extendsParameterMap.put("extendsParameterList", updatedExtendsParameterList);
				try {
					p.setExtendsParameterIterance(om.writeValueAsString(extendsParameterMap));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
					log.fatal(String.format("契約ID=%dの商品拡張項目登録に失敗しました。", contract.getId()));
					return;
				}
			}
			contract.setProductContractList(productContractList);

			boolean hasNoArrangementError = true;
			//契約更新
			if (callUpdateContractApi(contract)) {
				// 成功した場合 手配情報業務完了処理を実施
				hasNoArrangementError = callCompleteArrangementApi(contract);
			} else {
				// 失敗した場合エラーログを出力しスキップする
				log.fatal(String.format("契約ID=%dの契約更新に失敗しました。", contract.getId()));
				return;
			}
			// 手配情報業務完了処理がエラーの場合、元の契約情報で更新した契約情報を再更新する
			if (!hasNoArrangementError) {
				//手配完了
				if (!callUpdateContractApi(originalContract)) {
					// 再更新に失敗した場合手動リカバリが必要となる
					log.fatal(String.format("契約ID=%dの契約再更新に失敗しました。リカバリが必要となります。", originalContract.getId()));
				}
			}
		});
		//エンティティ(contract)に対して値を更新すると、エンティティマネージャーが更新対象とみなしてしまい、排他制御に引っかかる
		em.clear();
	}

	private String substringContractNumber(String number) {
		return number.substring(0, 17);
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
			restApiClient.callUpdateContract(contract);
			return true;
		} catch (Exception updateError) {
			updateError.printStackTrace();
			return false;
		}
	}

	/**
	 * 手配情報業務完了API呼び出し
	 * @param contract 契約情報
	 * @return true:API実行結果エラー無し false:API実行結果エラー有り
	 */
	private boolean callCompleteArrangementApi(Contract contract) {
		if (contract == null) {
			return false;
		}

		Arrangement arrangement = arrangementRepository.findByContractIdAndDisengagementFlg(contract.getId(), 0);
		if (arrangement == null) {
			log.fatal(String.format("契約ID=%dの手配情報が存在しません。", contract.getId()));
			return false;
		}

		// エラー無しか ラムダ式で利用するため配列とする
		boolean[] hasNoError = { true };

		List<ArrangementWork> arrangementWorkList = arrangement.getArrangementWorkList();
		arrangementWorkList.stream().forEach(work -> {
			try {
				restApiClient.callCompleteArrangement(work.getId());
			} catch (Exception arrangementError) {
				log.fatal(String.format("契約ID=%dの手配情報業務完了に失敗したため、処理をスキップします。", contract.getId()));
				arrangementError.printStackTrace();
				hasNoError[0] = false;
			}
		});

		return hasNoError[0];
	}

	private ExtendsParameterDto addExtendsParameterDto(List<ExtendsParameterDto> targetList, List<ExtendsParameterDto> updatedExtendsParameterList, ReplyOrderDto replyOrder) {
		ExtendsParameterDto extendsParameterDto = null;
		for (ExtendsParameterDto row : targetList) {
			if (updatedExtendsParameterList.stream().filter(f -> f.getId() == row.getId()).count() == 0) {
				row.setLineNumber(replyOrder.getLineNumber());
				row.setSerialNumber(replyOrder.getSerialNumber());
				row.setDevice(replyOrder.getDevice());
				row.setInvoiceNumber(replyOrder.getInvoiceNumber());
				extendsParameterDto = row;
				break;
			}
		}
		return extendsParameterDto;
	}
}
