package jp.co.ricoh.cotos.component.base;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import jp.co.ricoh.cotos.BatchConstants;
import jp.co.ricoh.cotos.commonlib.entity.master.ProductGrpMaster;
import jp.co.ricoh.cotos.commonlib.exception.ErrorCheckException;
import jp.co.ricoh.cotos.commonlib.exception.ErrorInfo;
import jp.co.ricoh.cotos.commonlib.logic.check.CheckUtil;
import jp.co.ricoh.cotos.commonlib.repository.master.ProductGrpMasterRepository;
import jp.co.ricoh.cotos.component.IBatchStepComponent;
import jp.co.ricoh.cotos.dto.SendOrderMailDto;

@Component("BASE")
public class BatchStepComponent implements IBatchStepComponent {

	@Autowired
	CheckUtil checkUtil;

	@Autowired
	ProductGrpMasterRepository productGrpMasterRepository;

	/**
	 * パラメーターチェック処理
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @return
	 * @throws FileAlreadyExistsException 
	 */
	@Override
	public final SendOrderMailDto paramCheck(String[] args) {
		SendOrderMailDto dto = new SendOrderMailDto();

		// バッチパラメーターのチェックを実施
		if (null == args || args.length != 4) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "ParameterEmptyError", new String[] { BatchConstants.BATCH_PARAMETER_LIST_NAME }));
		}

		File csvFile = Paths.get(args[0], args[1]).toFile();
		if (!csvFile.exists()) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "FileNotFoundError"));
		}
		long productGrpMasterId;
		try {
			productGrpMasterId = Long.parseLong(args[2]);
		} catch (Exception e) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "ArrangementInvalidParameterError", new String[] { "商品グループマスタID" }));
		}

		ProductGrpMaster productGrpMaster = productGrpMasterRepository.findOne(productGrpMasterId);
		if (productGrpMaster == null) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "EntityCheckNotNullError", new String[] { "商品グループ情報" }));
		}

		List<String> mailAddressList = Lists.newArrayList(args[3].split(","));
		if (args[3].isEmpty() || CollectionUtils.isEmpty(mailAddressList)) {
			throw new ErrorCheckException(checkUtil.addErrorInfo(new ArrayList<ErrorInfo>(), "ParameterEmptyError", new String[] { "メールアドレス" }));
		}

		dto.setCsvFile(csvFile.getAbsolutePath());
		dto.setProductGrpMasterId(productGrpMasterId);
		dto.setMailAddressList(mailAddressList);
		return dto;
	}

	/**
	 * 処理データ取得
	 * ※標準コンポーネントでのみ実装できます。商材個別になる場合は別バッチとして実装することを検討してください。
	 * @param searchParam 処理データ取得用パラメーター
	 * @return 処理データリスト
	 */
	@Override
	public final List<String> getDataList(String searchParam) {
		// ファイル読み込み、SQL等で処理データリストを取得
		return null;
	}

	@Override
	public boolean dataCheck(List<String> dataList) {
		// 処理データリストのチェックを実施
		return true;
	}

	@Override
	public void beforeProcess(Object param) {
		// 事前処理を実施
	}

	@Override
	public void process(SendOrderMailDto dto) throws Exception {
		// データ加工等の処理を実施
	}

	@Override
	public void afterProcess(Object param) {
		// 事後処理（ファイル出力、テーブルデータ更新等）を実施
	}
}
