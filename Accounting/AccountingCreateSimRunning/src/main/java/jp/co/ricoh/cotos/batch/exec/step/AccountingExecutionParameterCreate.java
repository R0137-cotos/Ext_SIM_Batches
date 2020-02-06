package jp.co.ricoh.cotos.batch.exec.step;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ibm.icu.text.Transliterator;

import jp.co.ricoh.cotos.batch.util.AccountingCreateSimRunningUtil;
import jp.co.ricoh.cotos.commonlib.dto.result.SalesCalcResultWorkForCspRunning;
import jp.co.ricoh.cotos.commonlib.entity.accounting.Accounting;
import jp.co.ricoh.cotos.commonlib.entity.master.CommonMasterDetail;
import lombok.NoArgsConstructor;

/**
 *
 * 計上処理のパラメータ作成
 *
 */
@NoArgsConstructor
@Component
public class AccountingExecutionParameterCreate {

	/**
	 * データ作成区分
	 */
	enum DateCreateDiv {

		売上請求("20"), 振替("31");

		private String code;

		private DateCreateDiv(String code) {
			this.code = code;
		}

		private String getCode() {
			return code;
		}
	}

	@Autowired
	AccountingCreateSimRunningUtil appUtil;

	public Accounting createParameter(SalesCalcResultWorkForCspRunning work, String baseDate, Date execDate,
			CommonMasterDetail tax) {

		Accounting entity = new Accounting();

		// 1 計上ＩＤ ＣＯＴＯＳで採番

		// 2 RJ管理番号
		entity.setRjManageNumber(work.getRjManageNumber());

		// 3 契約ID(COTOS_一意ID)
		entity.setContractId(work.getContractId());

		// 4 契約明細ID(COTOS_一意ID)
		entity.setContractDetailId(work.getContractDetailId());

		// 5 取引年月日
		// 6 締日

		// 7 商流区分
		// －－－＜参考＞－－－
		// １：直売，２：代売＿接点店、３：代売＿母店＿接点店"
		// ※SIMは直売のみ
		entity.setDealerFlow(work.getCommercialFlowDiv());

		// 8 費用種別
		// －－－＜参考＞－－－
		// 初期費:1, 月額(定額):2, 年額:3,月額(従量):4"
		// ※SIMは月額(定額):2,月額(従量):4のみ (従量も存在しないので、実質月額(定額)のみ)
		entity.setCostType(work.getCostType());

		// 9 品種区分
		entity.setItemType(work.getItemType());

		// 10 請求年月
		// 11 サービス期間開始日
		// 12 サービス期間終了日
		// 13 注文番号

		// 14 品種コード
		entity.setProductTypeCd(work.getRicohItemCode());

		// 15 品種名
		entity.setProductTypeName(work.getItemContractName());

		// 16 FFM計上処理フラグ "FFM向けに計上処理を実施済か否かを示すフラグ
		// 0（未処理）固定
		// －－－＜参考＞－－－
		// 0:未処理，1:処理済
		// 9:処理対象外(意図的に処理対象から外したい場合にセット)
		entity.setFfmFlg(0);

		// 17 CUBIC計上処理フラグ

		// 18 データ作成日 COTOS処理実行日（システム日付） YYYYMMDD
		entity.setFfmDataCreateDate(new SimpleDateFormat("yyyyMMdd").format(execDate));

		// 19 データ作成時間 COTOS処理実行時刻（システム日付） HHMMSS
		entity.setFfmDataCreateTime(new SimpleDateFormat("HHmmss").format(execDate));

		// 20 会社コード 3139(固定)
		entity.setFfmCompanyCd("3139");

		// 21 契約種類区分(固定)
		entity.setFfmContractTypeKbn("28");

		// 22 作成データパターン
		entity.setFfmDataPtn(work.getFfmDataPtn());

		// 23 勘定識別

		// 24 データ種別 固定：C2103
		entity.setFfmDataType("C2103");

		// 25 赤黒区分 固定：0(黒）
		entity.setFfmRedBlackType("0");

		// 26 債権債務照合キー

		// 27 NSPユニークキー 【ＣＯＴＯＳ契約】.RJ管理番号＋【ＣＯＴＯＳ品種（契約用）】.リコー品種コード＋計上ID
		// 計上ＩＤは登録後に決定する。
		// →ここでは null のままとする

		// 28 案件番号

		// 29 契約書番号
		entity.setFfmContractDocNo(work.getContractNumber());

		// 30 契約番号
		// 31 契約明細番号
		// 32 請求明細番号
		// 33 お問合せ番号
		// 34 お問合せ明細番号
		// 35 手配時の案件番号
		// 36 手配時の問合せ番号
		// 37 赤伝理由
		// 38 元契約番号
		// 39 元請求明細番号
		// 40 請求条件
		// 41 請求分割回数
		// 42 契約締結日

		// 43 契約期間（開始）
		if (work.getServiceTermStart() != null) {
			entity.setFfmContractPeriodStart(new SimpleDateFormat("yyyyMMdd").format(work.getServiceTermStart()));
		}

		// 44 契約期間（終了）
		if (work.getServiceTermEnd() != null) {
			entity.setFfmContractPeriodEnd(new SimpleDateFormat("yyyyMMdd").format(work.getServiceTermEnd()));
		}

		// 45 契約ＳＳコード
		// 46 契約ＳＳ社員コード

		// 47 振替先課所コード(データ作成区分:振替の場合のみ設定)
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.振替.getCode())) {
			entity.setFfmTrnsLocationCd(work.getTransToServiceOrgCode());
		}

		// 48 振替先社員コード

		// 49 保守契約／リース/レンタルＮｏ

		// 50 契約金額
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			entity.setFfmContractPrice(work.getAmountSummary());
		}

		// 51 仕切前計上金額
		// 52 仕切前消費税額

		// 53 商品コード
		entity.setFfmProdactCd(work.getRicohItemCode());

		// 54 機種略号
		// 55 機番
		// 56 見積時の入力商品名
		// 57 原価計上商品コード

		// 58 仕入区分 固定（00:通常売上)
		// 59 仕入値引区分
		// 60 仕入購買区分
		// 61 仕入取引日
		// 62 他社商品区分 (固定:)
		// 63 仕入取引先コード
		// 64 仕入課所設定区分
		// 65 仕入課所コード
		// 66 仕入責任得意先コード
		// 67 在庫区コード
		// 68 特価番号
		// 69 仕入先請求ＮＯ
		// 70 商品名（支払通知書用）

		// 71 売上区分 00（固定）
		entity.setFfmSalesType("00");

		// 72 売上値引区分

		// 73 売上取引日（納品日）
		Calendar ffmSalesTradeDate = Calendar.getInstance();
		ffmSalesTradeDate.setTime(execDate);
		ffmSalesTradeDate.set(Calendar.DAY_OF_MONTH, 1);
		entity.setFfmSalesTradeDate(new SimpleDateFormat("yyyyMMdd").format(ffmSalesTradeDate.getTime()));

		// 74 得意先コード
		entity.setFfmClientCd(work.getBillingCustomerSpCode());

		// 75 売上課所設定区分
		// 76 売上課所コード
		// 77 売上社員設定区分
		// 78 売上社員コード
		// 79 値引番号
		// 80 伝票番号
		// 81 契約区分
		// 82 売上原価金額

		// 83 振替先振替金額
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.振替.getCode())) {
			entity.setFfmTrnsPrice(work.getCost());
		}

		// 84 代直区分（販売店データリンク・売上用）
		// 【ＣＯＴＯＳ契約】.商流区分の値により下記を設定
		// 商流区分=１ →2(直売)
		// 商流区分=2 or 3 →1(代売)
		// ※SIMは商流区分=1のみ
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			switch (work.getCommercialFlowDiv()) {
			case "1":
				entity.setFfmDistType("2");
				break;
			default:
				break;
			}
		}

		// 85 売上数量
		// 86 ユーザ売上単価
		// 87 ユーザ売上単価（税込）
		// 88 ユーザ売上金額
		// 89 ユーザ売上金額（税込）
		// 90 ユーザ売上消費税区分
		// 91 ユーザ売上消費税率区分
		// 92 ユーザ売上消費税額

		// 93 RJ売上数量
		entity.setFfmRjSalesCnt(work.getQuantity());

		// 94 RJ売上単価
		// 商流区分=１：契約明細.単価を設定
		// 商流区分=２ ：品種（契約用）.母店売価(接点店仕切)
		// 商流区分=３ ：品種（契約用）.RJ仕切価格
		// ※SIMは商流区分=1のみ
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			switch (work.getCommercialFlowDiv()) {
			case "1":
				entity.setFfmRjSalesPrice(work.getUnitPrice());
				break;
			default:
				break;
			}
		}

		// 95 RJ売上単価（税込）

		// 96 RJ売上金額
		// "商流区分=１：契約明細.金額
		// 商流区分=２ ：品種（契約用）.母店売価(接点店仕切)*契約明細.数量
		// 商流区分=３ ：品種（契約用）.RJ仕切価格*契約明細.数量
		// ※SIMは商流区分=1のみ
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			switch (work.getCommercialFlowDiv()) {
			case "1":
				entity.setFfmRjSalesAmt(work.getAmountSummary());
				break;
			default:
				break;
			}
		}

		// 97 RJ売上金額（税込）

		// 98 RJ売上消費税区分 1:外税（固定）
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			entity.setFfmRjSalesTaxType("1");
		}

		// 99 RJ売上消費税率区分
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			entity.setFfmRjSalesTaxRate(tax.getCodeValue());
		}

		// 100 RJ売上消費税額
		// 税率 * RJ売上金額(小数点以下切捨て)
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			entity.setFfmRjSalesTaxPrice(appUtil.calcConsumptionTax(entity.getFfmRjSalesAmt(), tax.getCodeValue()));
		}

		// 101 RJ仕入数量
		// 102 RJ仕入単価
		// 103 RJ仕入単価（税込）
		// 104 RJ仕入金額
		// 105 RJ仕入金額（税込）
		// 106 RJ仕入消費税区分 1:外税（固定）
		// 107 RJ仕入消費税率区分
		// 108 RJ仕入消費税額 "汎用マスタから取得した税率区分とRJ仕入金額から算出。
		// 109 販売店売上数量
		// 110 販売店売上単価
		// 111 販売店売上単価（税込）
		// 112 販売店売上金額
		// 113 販売店売上金額（税込）
		// 114 販売店売上消費税区分
		// 115 販売店売上消費税率区分
		// 116 販売店売上消費税額

		// 117 R原価数量
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			entity.setFfmRCostCnt(work.getQuantity());
		}

		// 118 R原価単価
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			entity.setFfmRCostPrice(work.getRCost());
		}

		// 119 R原価単価（税込）

		// 120 R原価金額
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			entity.setFfmRCostAmt(appUtil.multiply(work.getRCost(), work.getQuantity()));
		}

		// 121 R原価金額（税込）

		// 122 R原価消費税区分 1:外税（固定）
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			entity.setFfmRCostTaxType("1");
		}

		// 123 R原価消費税率区分
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			entity.setFfmRCostTaxRate(tax.getCodeValue());
		}

		// 124 R原価消費税額 "汎用マスタから取得した税率区分とR原価金額から算出。
		// 例：消費税率区分が8の場合
		// 0.08 * 1555（R原価金額） = 124
		// ※小数点以下切捨て
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			entity.setFfmRCostTaxPrice(appUtil.calcConsumptionTax(entity.getFfmRCostAmt(), tax.getCodeValue()));
		}

		// 125 手数料数量
		// 126 手数料単価
		// 127 手数料単価（税込）
		// 128 手数料金額
		// 129 手数料金額（税込）
		// 130 手数料消費税区分
		// 131 手数料消費税率区分
		// 132 手数料消費税額
		// 133 請求書明細識別コード

		// 134 納品書要否区分
		// 契約.商流区分の値により下記を設定
		// 商流区分=１ ：直売:""0""固定
		// それ以外：代売:""1""固定
		// SIMは商流区分=1のみ
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			if (StringUtils.equals(work.getCommercialFlowDiv(), "1")) {
				entity.setFfmBillOutputFlg("0");
			}
		}

		// 135 納品書出力パターン
		// 2（固定値）
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			entity.setFfmBillOutputPtn("2");
		}

		// 136 納品書出力形式
		// 10（固定値）
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			entity.setFfmBillOutputFmt("10");
		}

		// 137 請求書発行システム 1(固定：CUBIC）
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			entity.setFfmBillOutputSystem("1");
		}

		// 138 商品名パターン番号（納品書・請求書用）
		// 139 商品名（納品書・請求書用）
		// 140 業務への連絡事項
		// 141 備考（納品書・請求書用）
		// 142 請求期間（開始）
		// 143 請求期間（終了）
		// 144 請求月

		// 145 今回の請求回数
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			entity.setFfmThisBillingCnt(1);
		}

		// 146 カウンター

		// 147 コメント１ 恒久契約識別番号 ＋ 顧客の企業名（カナ）を半角カナ変換
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			String halfWidthCompanyKana = Optional.ofNullable(work.getCompanyNameKana())
					.filter(s -> StringUtils.isNotEmpty(s)).map(s -> {
						Transliterator transliterator = Transliterator.getInstance("Fullwidth-Halfwidth");
						return transliterator.transliterate(s);
					}).orElse("");
			entity.setFfmOutputComment1(work.getImmutableContIdentNumber() + halfWidthCompanyKana);
		}
		// 148 コメント２
		// 149 強制フラグ
		// 150 機器設置先名
		// 151 機器設置先部課名
		// 152 RINGS届先コード(3桁）
		// 153 OE届先コード(11桁）

		// 154 納品場所識別 11(固定)
		if (StringUtils.equals(work.getFfmDataPtn(), DateCreateDiv.売上請求.getCode())) {
			entity.setFfmDstType("11");
		}

		// 155 届先名１（会社名）
		// 156 届先名２（会社部課名）
		// 157 顧客名
		// 158 届先住所１
		// 159 届先住所２
		// 160 届先住所３
		// 161 届先郵便番号
		// 162 届先電話番号
		// 163 届先ＦＡＸ番号
		// 164 届先名（カナ）
		// 165 得意先コード（二次店）
		// 166 届先コード（二次店）
		// 167 支払利息相当額
		// 168 受取利息相当額
		// 169 見積番号
		// 170 見積明細番号
		// 171 本体見積明細番号
		// 172 R請求書摘要
		// 173 一次店_R会社コード
		// 174 一次店_R会社名
		// 175 一次店_販売店ID
		// 176 一次店_販売店名
		// 177 一次店_販売店摘要
		// 178 二次店_R会社コード
		// 179 二次店_R会社名
		// 180 二次店_販売店ID
		// 181 二次店_販売店名
		// 182 二次店_販売店摘要
		// 183 フォーマット種別
		// 184 勘定科目コード
		// 185 貸借区分 D(固定）
		// 186 システムコード
		// 187 会社コード
		// 188 会計計上日
		// 189 伝票ＮＯ
		// 190 伝票明細
		// 191 計上部門コード
		// 192 商品軸
		// 193 営業軸
		// 194 決算識別子
		// 195 CUBIC品種コード
		// 196 増減理由
		// 197 環境会計コード
		// 198 プロジェクトコード
		// 199 数量 計上
		// 200 取引金額
		// 201 外貨取引金額
		// 202 通貨コード
		// 203 通貨換算タイプ
		// 204 通貨換算レート
		// 205 通貨換算日
		// 206 摘要
		// 207 明細摘要
		// 208 各社セグメント
		// 209 管理セグメント予備2
		// 210 管理セグメント予備3
		// 211 消し込みキー
		// 212 扱い者コード
		// 213 グリーン購買コード
		// 214 案件ＮＯ
		// 215 Ｄ／Ｆ ＮＯ
		// 216 予算ＮＯ
		// 217 顧客コード
		// 218 各社固有管理セグメント1
		// 219 取引日
		// 220 請求先サイトコード
		// 221 国内／海外区分
		// 222 取引単価（税抜）
		// 223 外貨取引単価
		// 224 回収条件名
		// 225 回収方法名
		// 226 回収起算日
		// 227 請求分類名
		// 228 *CUBIC請求書明細識別コード A0（固定）
		// 229 値引名称
		// 230 請求書発行区分
		// 231 請求書ＮＯ
		// 232 荷為替手形ＮＯ
		// 233 Ｐ／ＣキーＮＯ
		// 234 請求書出力用伝票ＮＯ
		// 235 受注ＮＯ
		// 236 発注ＮＯ
		// 237 前受管理ＮＯ
		// 238 前受金消込額
		// 239 追加ＴＥＲＭ
		// 240 契約NO
		// 241 品名
		// 242 債権債務照合キー
		// 243 汎用転送データ
		// 244 元伝票ＮＯ（赤伝時）
		// 245 元伝票明細ＮＯ（赤伝時）
		// 246 元会計計上日（赤伝時）
		// 247 設置先サイトコード
		// 248 項目予備1
		// 249 項目予備2
		// 250 項目予備3
		// 251 拡張項目 テナントID

		return entity;
	}
}
