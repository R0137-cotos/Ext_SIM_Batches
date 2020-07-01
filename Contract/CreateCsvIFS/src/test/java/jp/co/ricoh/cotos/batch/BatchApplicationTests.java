package jp.co.ricoh.cotos.batch;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import jp.co.ricoh.cotos.BatchApplication;
import jp.co.ricoh.cotos.batch.exec.IFSCsvCreateUtil;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract;
import jp.co.ricoh.cotos.commonlib.entity.contract.Contract.IfsLinkageCsvCreateStatus;
import jp.co.ricoh.cotos.commonlib.repository.contract.ContractRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BatchApplicationTests extends TestBase {

	static ConfigurableApplicationContext context;

	private String outputPath = "output/";

	@Autowired
	IFSCsvCreateUtil ifsCsvCreateUtil;

	@Autowired
	ContractRepository contractRepository;

	@Autowired
	public void injectContext(ConfigurableApplicationContext injectContext) {
		context = injectContext;
		context.getBean(DBConfig.class).clearData();
	}

	@AfterClass
	public static void exit() throws Exception {
		if (null != context) {
			context.getBean(DBConfig.class).clearData();
			context.stop();
		}
	}

	@Test
	public void 正常系_CSVファイルを出力できること_新規() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("all/InsertContractData.sql");
		context.getBean(DBConfig.class).initTargetTestData("resetSequence.sql");
		Files.deleteIfExists(Paths.get("output/SIM_result_20181228.csv"));
		Files.deleteIfExists(Paths.get("output/tmp_SIM_result_20181228.csv"));
		BatchApplication.main(new String[] { "result_20181228.csv", outputPath, "SIM" });

		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + "SIM_result_20181228.csv"));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/all/all.csv"));
		Assert.assertArrayEquals(expected, actuals);

		List<Contract> contractList = new ArrayList<>();
		contractRepository.findAll().iterator().forEachRemaining(contractList::add);
		Assert.assertEquals("6件の契約が作成済みになっていること", 6, contractList.stream().filter(s -> IfsLinkageCsvCreateStatus.作成済み.equals(s.getIfsLinkageCsvCreateStatus())).count());
		Assert.assertEquals("6件の契約の作成日時が設定されていること", 6, contractList.stream().filter(s -> null != s.getIfsLinkageCsvCreateDate()).count());
		Files.deleteIfExists(Paths.get("output/SIM_result_20181228.csv"));
		Files.deleteIfExists(Paths.get("output/tmp_SIM_result_20181228.csv"));
	}

	@Test
	public void 正常系_CSVファイルを出力できること_容量変更() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("all/InsertContractDataCapacityChange.sql");
		context.getBean(DBConfig.class).initTargetTestData("resetSequence.sql");
		Files.deleteIfExists(Paths.get("output/SIM_result_20181228.csv"));
		Files.deleteIfExists(Paths.get("output/tmp_SIM_result_20181228.csv"));
		BatchApplication.main(new String[] { "result_20181228.csv", outputPath, "SIM" });

		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + "SIM_result_20181228.csv"));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/all/all.csv"));
		Assert.assertArrayEquals(expected, actuals);

		List<Contract> contractList = new ArrayList<>();
		contractRepository.findAll().iterator().forEachRemaining(contractList::add);
		Assert.assertEquals("6件の契約が作成済みになっていること", 6, contractList.stream().filter(s -> IfsLinkageCsvCreateStatus.作成済み.equals(s.getIfsLinkageCsvCreateStatus())).count());
		Assert.assertEquals("6件の契約の作成日時が設定されていること", 6, contractList.stream().filter(s -> null != s.getIfsLinkageCsvCreateDate()).count());
		Files.deleteIfExists(Paths.get("output/SIM_result_20181228.csv"));
		Files.deleteIfExists(Paths.get("output/tmp_SIM_result_20181228.csv"));
	}

	@Test
	public void 正常系_CSVファイルを出力できること_有償交換() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("all/InsertContractDataPaidExchange.sql");
		context.getBean(DBConfig.class).initTargetTestData("resetSequence.sql");
		Files.deleteIfExists(Paths.get("output/SIM_result_20181228.csv"));
		Files.deleteIfExists(Paths.get("output/tmp_SIM_result_20181228.csv"));
		BatchApplication.main(new String[] { "result_20181228.csv", outputPath, "SIM" });

		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + "SIM_result_20181228.csv"));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/all/all.csv"));
		Assert.assertArrayEquals(expected, actuals);

		List<Contract> contractList = new ArrayList<>();
		contractRepository.findAll().iterator().forEachRemaining(contractList::add);
		Assert.assertEquals("6件の契約が作成済みになっていること", 6, contractList.stream().filter(s -> IfsLinkageCsvCreateStatus.作成済み.equals(s.getIfsLinkageCsvCreateStatus())).count());
		Assert.assertEquals("6件の契約の作成日時が設定されていること", 6, contractList.stream().filter(s -> null != s.getIfsLinkageCsvCreateDate()).count());
		Files.deleteIfExists(Paths.get("output/SIM_result_20181228.csv"));
		Files.deleteIfExists(Paths.get("output/tmp_SIM_result_20181228.csv"));
	}

	@Test
	public void 正常系_CSVファイルを出力できること_新規_容量変更_有償交換() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("all/InsertContractDataAll.sql");
		context.getBean(DBConfig.class).initTargetTestData("resetSequence.sql");
		Files.deleteIfExists(Paths.get("output/SIM_result_20181228.csv"));
		Files.deleteIfExists(Paths.get("output/tmp_SIM_result_20181228.csv"));
		BatchApplication.main(new String[] { "result_20181228.csv", outputPath, "SIM" });

		byte[] actuals = Files.readAllBytes(Paths.get(outputPath + "SIM_result_20181228.csv"));
		byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/all/all.csv"));
		Assert.assertArrayEquals(expected, actuals);

		List<Contract> contractList = new ArrayList<>();
		contractRepository.findAll().iterator().forEachRemaining(contractList::add);
		Assert.assertEquals("6件の契約が作成済みになっていること", 6, contractList.stream().filter(s -> IfsLinkageCsvCreateStatus.作成済み.equals(s.getIfsLinkageCsvCreateStatus())).count());
		Assert.assertEquals("6件の契約の作成日時が設定されていること", 6, contractList.stream().filter(s -> null != s.getIfsLinkageCsvCreateDate()).count());
		Files.deleteIfExists(Paths.get("output/SIM_result_20181228.csv"));
		Files.deleteIfExists(Paths.get("output/tmp_SIM_result_20181228.csv"));
	}

	@Test
	public void 正常系_商品種類区分一致なし_CSVファイルを出力されないこと() throws IOException {
		Files.deleteIfExists(Paths.get("output/SIM_result_20181228.csv"));
		Files.deleteIfExists(Paths.get("output/tmp_SIM_result_20181228.csv"));
		context.getBean(DBConfig.class).initTargetTestData("flgE/InsertContractData.sql");
		BatchApplication.main(new String[] { "result_20181228.csv", outputPath, "SIM" });

		Assert.assertEquals("処理対象データがないのでファイルが作成されていないこと", 0, checkFileExistence());

		List<Contract> contractList = new ArrayList<>();
		contractRepository.findAll().iterator().forEachRemaining(contractList::add);
		Assert.assertEquals("1件も契約が作成済みになっていないこと", 0, contractList.stream().filter(s -> IfsLinkageCsvCreateStatus.作成済み.equals(s.getIfsLinkageCsvCreateStatus())).count());
		Assert.assertEquals("契約の作成日時が設定されていないこと", 0, contractList.stream().filter(s -> null != s.getIfsLinkageCsvCreateDate()).count());
	}

	@Test
	public void 引数無しで実行すると失敗すること() {
		try {
			BatchApplication.main(new String[] {});
			Assert.fail("引数無しで実行したのに異常終了しなかった");
		} catch (ExitException e) {
		}
	}

	@Test
	public void 既存ファイルに上書きできないこと() throws IOException {
		if (!Files.exists(Paths.get("output/SIM_duplicate_20181228.csv"))) {
			Files.createFile(Paths.get("output/SIM_duplicate_20181228.csv"));
		}
		try {
			BatchApplication.main(new String[] { "duplicate_20181228.csv", "output", "SIM" });
			Assert.fail("既存ファイルがあるのに異常終了しなかった");
		} catch (ExitException e) {
		}
		Files.deleteIfExists(Paths.get("output/SIM_duplicate_20181228.csv"));
	}

	@Test
	public void パラメータ不正_実行日付() throws IOException {
		try {
			BatchApplication.main(new String[] { "param.csv", "output", "2018/12/23", "SIM" });
			Assert.fail("パラメータが不正なのに異常終了しなかった");
		} catch (ExitException e) {
		}
	}

	@Test
	public void パラメータ不正_存在しないディレクトリ() throws IOException {
		context.getBean(DBConfig.class).initTargetTestData("all/InsertContractData.sql");
		Files.deleteIfExists(Paths.get("output/dummy/SIM_result_20181228.csv"));

		try {
			BatchApplication.main(new String[] { "result_20181228.csv", outputPath + "dummy", "SIM" });
			Assert.fail("CSVファイルが書き込めないのに異常終了しなかった");
		} catch (ExitException e) {
		}
	}

	@Test
	public void ゼロパディング() {
		String num = ifsCsvCreateUtil.paddingZero("1");
		Assert.assertEquals("ゼロ埋めできてること", "0001", num);
	}

	@Test
	public void 半角全角変換() {
		String result = ifsCsvCreateUtil.transrateStr("123ｱｲｳｴｵABC!#$%&");
		Assert.assertEquals("全角に変換できること", "１２３アイウエオＡＢＣ！＃＄％＆", result);
	}

	private long checkFileExistence() {
		return Optional.of(Paths.get(outputPath).toFile()).filter(s -> s.exists()).map(s -> Arrays.asList(s.listFiles(getPdfFileFilter())).size()).orElse(0);
	}

	private FilenameFilter getPdfFileFilter() {
		return new FilenameFilter() {
			public boolean accept(File file, String str) {
				if (str.endsWith("csv")) {
					return true;
				} else {
					return false;
				}
			}
		};
	}

}
