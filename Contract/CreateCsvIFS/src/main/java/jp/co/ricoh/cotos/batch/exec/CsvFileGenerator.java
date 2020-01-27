package jp.co.ricoh.cotos.batch.exec;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.commonlib.logic.message.MessageUtil;
import lombok.extern.log4j.Log4j;

@Component
@Log4j
public class CsvFileGenerator {

	@Autowired
	CheckUtil checkUtil;

	@Autowired
	MessageUtil messageUtil;

	@Autowired
	ExportCSV exportCSV;

	public void execute(String args[]) {

		if (args.length != 3) {
			List<ErrorInfo> error = checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "ParameterEmptyError", new String[] { "ファイル名_処理日付.csv/出力先ディレクトリパス/商品種類区分" });
			error.stream().forEach(errorInfo -> log.fatal(errorInfo.getErrorId() + ":" + errorInfo.getErrorMessage()));
			System.exit(1);
		}

		String productClassDiv = args[2];

		File file = Paths.get(args[1], productClassDiv + "_" + args[0]).toFile();
		File tmpFile = Paths.get(args[1], "tmp_" + productClassDiv + "_" + args[0]).toFile();

		try {
			log.info(messageUtil.createMessageInfo("BatchCreateFileStartInfo", new String[] { file.getAbsolutePath() }).getMsg());
			boolean createdFlg = exportCSV.execute(tmpFile, file, productClassDiv);
			if (createdFlg) {
				log.info(messageUtil.createMessageInfo("BatchCreateFileEndInfo", new String[] { file.getAbsolutePath() }).getMsg());
			}
		} catch (FileAlreadyExistsException e) {
			List<ErrorInfo> error = checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "FileAlreadyExistsError", new String[] { file.getAbsolutePath() });
			error.stream().forEach(errorInfo -> log.fatal(errorInfo.getErrorId() + ":" + errorInfo.getErrorMessage(), e));
			System.exit(1);
		} catch (ErrorCheckException e) {
			e.getErrorInfoList().stream().forEach(errorInfo -> log.fatal(errorInfo.getErrorId() + ":" + errorInfo.getErrorMessage(), e));
			System.exit(1);
		} catch (IOException e) {
			List<ErrorInfo> error = checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "BatchCannotCreateFiles", new String[] { "CSVファイル" });
			error.stream().forEach(errorInfo -> log.fatal(errorInfo.getErrorId() + ":" + errorInfo.getErrorMessage(), e));
			System.exit(1);
		} catch (Throwable e) {
			List<ErrorInfo> error = checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "UnexpectedErrorBatch", new String[] { "CSVファイル" });
			error.stream().forEach(errorInfo -> log.fatal(errorInfo.getErrorId() + ":" + errorInfo.getErrorMessage(), e));
			System.exit(1);
		}

	}
}
