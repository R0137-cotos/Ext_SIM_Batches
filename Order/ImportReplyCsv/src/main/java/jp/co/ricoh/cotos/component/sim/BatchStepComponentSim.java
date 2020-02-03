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
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.entity.arrangement.Arrangement;
import jp.co.ricoh.cotos.commonlib.entity.arrangement.ArrangementWork;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.entity.contract.ProductContract;
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
	ObjectMapper objectMapper;

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
	@Transactional
	public void process(String[] args) throws JsonProcessingException, FileNotFoundException, IOException {
		log.info("SIM独自処理");

		File csvFile = Paths.get(args[0], args[1]).toFile();

		//CSV読込
		CsvMapper mapper = new CsvMapper();
		mapper.setTimeZone(objectMapper.getDeserializationConfig().getTimeZone());
		CsvSchema schema = CsvSchema.emptySchema().withoutHeader();
		CsvSchema quoteSchema = mapper.schemaFor(ReplyOrderDto.class).withoutQuoteChar();
		MappingIterator<ReplyOrderDto> it = mapper.readerFor(ReplyOrderDto.class).with(schema).with(quoteSchema).readValues(new InputStreamReader(new FileInputStream(csvFile), Charset.forName("Shift_JIS")));
		List<ReplyOrderDto> csvlist = it.readAll();

		if (CollectionUtils.isEmpty(csvlist)) {
			log.info("取込データが0件のため処理を終了します");
			return;
		}

		//枝番削除した契約番号をキーとしたMap
		Map<String, List<ReplyOrderDto>> contractNumberGroupingMap = csvlist.stream().collect(Collectors.groupingBy(dto -> substringContractNumber(dto.getContractId()), Collectors.mapping(dto -> dto, Collectors.toList())));
		//枝番削除した契約番号のリスト
		List<String> contractNumberList = contractNumberGroupingMap.entrySet().stream().map(map -> map.getKey()).map(c -> substringContractNumber(c)).collect(Collectors.toList());

		//対象契約取得
		Map<String, Object> queryParams = new HashMap<>();
		StringJoiner joiner = new StringJoiner("','", "'", "'").setEmptyValue("");
		contractNumberList.stream().forEach(conNumLst -> joiner.add(conNumLst));
		queryParams.put("contractNumberList", joiner.toString());
		List<Contract> contractList = dbUtil.loadFromSQLFile("sql/findTargetContract.sql", Contract.class, queryParams);
		Map<String, Contract> contractMapByContractNumber = contractList.stream().collect(Collectors.toMap(Contract::getImmutableContIdentNumber, con -> con));

		contractMapByContractNumber.entrySet().stream().forEach(contractMap -> {
			Contract contract = contractMap.getValue();
			List<ProductContract> productContractList = contractMap.getValue().getProductContractList();
			List<ReplyOrderDto> replyOrderList = contractNumberGroupingMap.get(contractMap.getKey());
			//サービス開始希望日を設定
			contract.setServiceTermStart(batchUtil.changeDate(replyOrderList.get(0).getDeliveryExpectedDate()));

			//商品コードでグルーピング
			Map<String, List<ReplyOrderDto>> replyOrderProductGroupingMap = replyOrderList.stream().collect(Collectors.groupingBy(dto -> dto.getRicohItemCode(), Collectors.mapping(dto -> dto, Collectors.toList())));

			//拡張項目繰り返しを設定
			for (ProductContract p : productContractList) {
				String extendsParameterIterance = p.getExtendsParameterIterance();
				Function<String, List<ExtendsParameterDto>> readJsonFunc = batchUtil.Try(x -> batchUtil.readJson(x), (error, x) -> null);
				List<ExtendsParameterDto> extendsParameterList = readJsonFunc.apply(extendsParameterIterance);
				if (CollectionUtils.isEmpty(extendsParameterList)) {
					log.fatal(String.format("契約ID=%dの商品拡張項目読込に失敗しました。", contract.getId()));
					return;
				}

				List<ExtendsParameterDto> updatedExtendsParameterList = new ArrayList<>();
				replyOrderProductGroupingMap.entrySet().stream().forEach(replyMap -> {
					List<ReplyOrderDto> dtoList = replyMap.getValue();
					List<ExtendsParameterDto> targetList = extendsParameterList.stream().filter(e -> e.getProductCode().equals(replyMap.getKey())).collect(Collectors.toList());
					IntStream.range(0, dtoList.size()).forEach(i -> {
						targetList.get(i).setLineNumber(dtoList.get(i).getLineNumber());
						targetList.get(i).setSerialNumber(dtoList.get(i).getSerialNumber());
						targetList.get(i).setInvoiceNumber(dtoList.get(i).getInvoiceNumber());
						updatedExtendsParameterList.add(targetList.get(i));
					});
				});
				
				// 拡張項目繰返への設定値をIDの昇順でソート
				if(updatedExtendsParameterList != null) {
					updatedExtendsParameterList.sort((a, b) -> (int)a.getId() - (int)b.getId());
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

			//契約更新
			try {
				batchUtil.callUpdateContract(contract);
			} catch (Exception updateError) {
				log.fatal(String.format("契約ID=%dの契約更新に失敗しました。", contract.getId()));
				updateError.printStackTrace();
				return;
			}
			//手配完了
			Arrangement arrangement = arrangementRepository.findByContractIdAndDisengagementFlg(contract.getId(), 0);
			List<ArrangementWork> arrangementWorkList = arrangement.getArrangementWorkList();
			arrangementWorkList.stream().forEach(work -> {
				try {
					batchUtil.callCompleteArrangement(work.getId());
				} catch (Exception arrangementError) {
					log.fatal(String.format("契約ID=%dの手配完了に失敗しました。", contract.getId()));
					arrangementError.printStackTrace();
				}

			});
		});
		//エンティティ(contract)に対して値を更新すると、エンティティマネージャーが更新対象とみなしてしまい、排他制御に引っかかる
		em.clear();
	}

	private String substringContractNumber(String number) {
		return number.substring(0, 15);
	}

}
