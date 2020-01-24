package jp.co.ricoh.cotos.batch.exec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import jp.co.ricoh.cotos.batch.entity.ExtendsParameterIteranceDto;
import jp.co.ricoh.cotos.batch.entity.IFSCsvDto;
import jp.co.ricoh.cotos.batch.entity.IFSDto;
import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract.IfsLinkageCsvCreateStatus;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.commonlib.logic.message.MessageUtil;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractRepository;
import lombok.extern.log4j.Log4j;

@Component
@Log4j
public class ExportCSV {

	@Autowired
	CheckUtil checkUtil;

	@Autowired
	DBUtil dbUtil;

	@Autowired
	MessageUtil messageUtil;

	@Autowired
	IFSCsvCreateUtil iFSCsvCreateUtil;

	@Autowired
	ContractRepository contractRepository;

	private static final String headerFilePath = "file/header.csv";

	public boolean execute(File tmpFile, File csvFile, String productClassDiv) throws IOException {

		boolean createdFlg = false;

		if (!csvFile.getParentFile().exists()) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "DirectoryNotFoundError"));
		}

		if (csvFile.exists()) {
			throw new FileAlreadyExistsException(csvFile.getAbsolutePath());
		}

		if (tmpFile.exists()) {
			throw new FileAlreadyExistsException(tmpFile.getAbsolutePath());
		}

		Map<String, Object> sqlParams = new HashMap<String, Object>();
		sqlParams.put("productClassDiv", productClassDiv);
		List<IFSDto> ifsDto = dbUtil.loadFromSQLFile("sql/findContractData.sql", IFSDto.class, sqlParams);

		if (0 == ifsDto.size()) {
			log.info(messageUtil.createMessageInfo("BatchTargetNoDataInfo", new String[] { "IFSその他機器情報作成CSV" }).getMsg());
			return createdFlg;
		}

		List<Long> contractIdList = ifsDto.stream().map(ifs -> Long.valueOf(ifs.getContractId())).collect(Collectors.toList());

		try {

			List<IFSCsvDto> iFSCsvDtoList = new ArrayList<>();

			for (IFSDto dto : ifsDto) {
				List<ExtendsParameterIteranceDto> extendsParameterList = null;
				if (Objects.nonNull(dto.getExtendsParameterIterance())) {
					Function<String, List<ExtendsParameterIteranceDto>> readJsonFunc = iFSCsvCreateUtil.Try(x -> iFSCsvCreateUtil.readJson(x), (error, x) -> null);
					extendsParameterList = readJsonFunc.apply(dto.getExtendsParameterIterance());
					extendsParameterList = extendsParameterList.stream().filter(o -> o.getProductCode().equals(dto.getRicohItemCode())).collect(Collectors.toList());
				}
				int npServiceNo = 1;
				if (!(CollectionUtils.isEmpty(iFSCsvDtoList)) && iFSCsvDtoList.get(iFSCsvDtoList.size() - 1).getContractId().equals(dto.getContractNoHeader() + iFSCsvCreateUtil.paddingZero(dto.getContractId()))) {
					npServiceNo = Integer.parseInt(iFSCsvDtoList.get(iFSCsvDtoList.size() - 1).getNpServiceNo()) + 1;
				}
				for (int i = 0; i < dto.getQuantity(); i++) {
					IFSCsvDto csvDto = new IFSCsvDto();
					BeanUtils.copyProperties(dto, csvDto);

					csvDto.setContractId(dto.getContractNoHeader() + iFSCsvCreateUtil.paddingZero(dto.getContractId()));

					csvDto.setNothMechLineNo(String.valueOf(Long.valueOf(dto.getContractDetailId()) * (-1)));
					csvDto.setNendUserPerson(csvDto.getNendUserPerson().replaceAll(" ", "　"));
					csvDto.setNuserPerson(csvDto.getNuserPerson().replaceAll(" ", "　"));

					if (!CollectionUtils.isEmpty(extendsParameterList)) {
						csvDto.setNinvPartNo(extendsParameterList.get(i).getLineNumber());
						csvDto.setNserialNo(extendsParameterList.get(i).getSerialNumber());
						csvDto.setNnote(extendsParameterList.get(i).getDevice());
					}
					csvDto.setNpServiceNo(Integer.toString(npServiceNo));
					npServiceNo++;

					iFSCsvDtoList.add(csvDto);
				}
			}

			CsvMapper mapper = new CsvMapper();
			CsvSchema schema = mapper.schemaFor(IFSCsvDto.class).withoutHeader().withColumnSeparator(',').withLineSeparator("\r\n").withoutQuoteChar();
			mapper.writer(schema).writeValues(Files.newBufferedWriter(tmpFile.toPath(), Charset.forName("UTF-8"), StandardOpenOption.CREATE)).writeAll(iFSCsvDtoList).close();

			// ヘッダーファイルとのマージ
			List<String> outputList = Files.readAllLines(tmpFile.toPath(), Charset.forName("UTF-8"));
			List<String> headerList = new ArrayList<>();
			InputStream in = this.getClass().getClassLoader().getResourceAsStream(headerFilePath);
			String header = IOUtils.toString(in, "UTF-8");
			headerList.add(header);
			headerList.addAll(outputList);
			try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(csvFile)))) {
				headerList.stream().forEach(s -> pw.print(s + "\r\n"));
			}
			Files.deleteIfExists(tmpFile.toPath());
			updateContractIfsLinkage(contractIdList, IfsLinkageCsvCreateStatus.作成済み);
		} catch (Exception e) {
			updateContractIfsLinkage(contractIdList, IfsLinkageCsvCreateStatus.作成エラー);
			throw e;
		}
		return true;
	}

	/**
	 * IFS連携用CSV作成フラグおよび作成日の更新
	 *
	 * @param contractId
	 */
	@Transactional
	private void updateContractIfsLinkage(List<Long> contractId, IfsLinkageCsvCreateStatus status) {
		contractRepository.findAll(contractId).iterator().forEachRemaining(contract -> {
			contract.setIfsLinkageCsvCreateStatus(status);
			if (IfsLinkageCsvCreateStatus.作成済み.equals(contract.getIfsLinkageCsvCreateStatus())) {
				contract.setIfsLinkageCsvCreateDate(new Date());
			}
			contractRepository.save(contract);
		});
	}

}
