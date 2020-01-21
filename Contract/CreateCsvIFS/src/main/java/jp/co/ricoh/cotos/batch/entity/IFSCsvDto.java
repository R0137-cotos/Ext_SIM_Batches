package jp.co.ricoh.cotos.batch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({ "契約No.", "強制更新フラグ", "処理モードフラグ", "契約書No.", "タスクフォースNo.", "案件No.", "契約締結日", "契約管理区分", "売上先コード", "サブ店コード", "営業担当者コード", "承認者コード", "売上課所コード", "支店課所コード", "TMCコード", "SOコード", "THコード", "TSコード", "TS名", "受注先企事部", "TeMS代直区分", "DM送付不可区分", "メインCEコード", "エンドユーザ企事部", "契約先担当者", "契約先担当者（半角ｶﾅ）", "SI/NI区分", "商流区分", "概要", "受付", "切り分け", "CE手配", "部材手配", "顧客情報", "その他特別ルール", "コール依頼元番号", "販売店営業担当者", "コールセンタ問合せID", "営業担当者連絡先TEL（CC)", "営業担当者連絡先メールアドレス（CC)", "契約情報備考", "コールセンタ用契約判別フラグ", "エンドユーザ担当者TEL（CC)", "エンドユーザ担当者FAX（CC)", "エンドユーザ担当者メールアドレス（CC)", "契約開始日", "契約満了日", "移行用キーワード１", "移行用キーワード２", "移行用キーワード３", "サポート特記", "提供サービス番号", "契約形態", "提供サービスコード", "開始日", "終了日", "メニュー番号1", "自営/受託区分", "サービス計画書", "サービス購入フラグ", "オンサイト保守フラグ", "オンサイト上限管理単位", "オンサイト訪問上限数", "インシデント管理フラグ", "インシデント上限管理単位", "インシデント問合せ上限数", "CE手配完了報告", "CE確定報告", "CE到着予定報告", "CE到着報告", "機器前到着及び作業開始報告", "チェックポイント①", "チェックポイント②", "チェックポイント③", "作業終了報告", "退出報告", "部材到着予定報告", "部材到着報告", "成果物回収報告", "提供サービス備考", "リモート可/不可フラグ", "月報作成要否", "サポート開始日", "サポート終了日", "オーダ番号", "NotesPump対象DB", "業務連絡", "メールメモ欄", "その他機器明細番号", "保守SS", "機種コード", "SIMONS機番", "担当CEコード", "ユーザコード", "お客様/企事部", "お客様/担当者（カナ）", "お客様/担当者（漢字）", "メンテの注意", "設置日", "購入形態", "納入形態", "納入機器区分", "保証・開始年月日", "保証・終了年月日", "点検月", "RMCコード", "メニュー番号2", "管理ID", "契約区", "設置SS", "メーカ型番", "部品番号", "シリアルNo.", "ホスト名", "コンフィグ名", "IOSファイル名", "メモリサイズ", "備考1", "第一倉庫", "お客様/担当者電話番号（CC)", "その他機器表示順（CC)", "その他機器情報備考（CC)", "その他機器契約開始日", "その他機器契約終了日", "管理番号", "お客様機械No.", "契約機器特記", "その他構成品明細番号", "メーカ型番（オプション）", "部品番号（オプション）", "シリアルNo.（オプション）", "数量", "備考2", "第一倉庫（オプション）" })
public class IFSCsvDto {

	/** 契約No. */
	@JsonProperty("契約No.")
	private String contractId;

	/** 強制更新フラグ */
	@JsonProperty("強制更新フラグ")
	private String nforceUpdFlg;

	/** 処理モードフラグ */
	@JsonProperty("処理モードフラグ")
	private String nprocessModeFlg;

	/** 契約書No. */
	@JsonProperty("契約書No.")
	private String ncontractSheetNo;

	/** タスクフォースNo. */
	@JsonProperty("タスクフォースNo.")
	private String ntaskForceNo;

	/** 案件No. */
	@JsonProperty("案件No.")
	private String nitemNo;

	/** 契約締結日 */
	@JsonProperty("契約締結日")
	private String fromDate;

	/** 契約管理区分 */
	@JsonProperty("契約管理区分")
	private String contractType;

	/** 売上先コード */
	@JsonProperty("売上先コード")
	private String nbillAccountCode;

	/** サブ店コード */
	@JsonProperty("サブ店コード")
	private String nsubtencd;

	/** 営業担当者コード */
	@JsonProperty("営業担当者コード")
	private String nsalesEmpCode;

	/** 承認者コード */
	@JsonProperty("承認者コード")
	private String nacknowledgerCode;

	/** 売上課所コード */
	@JsonProperty("売上課所コード")
	private String nsalesOrgCode;

	/** 支店課所コード */
	@JsonProperty("支店課所コード")
	private String nbranchSection;

	/** TMCコード */
	@JsonProperty("TMCコード")
	private String ntmcCode;

	/** SOコード */
	@JsonProperty("SOコード")
	private String nsoCode;

	/** THコード */
	@JsonProperty("THコード")
	private String nthCode;

	/** TSコード */
	@JsonProperty("TSコード")
	private String ntsCode;

	/** TS名 */
	@JsonProperty("TS名")
	private String ntsDesc;

	/** 受注先企事部 */
	@JsonProperty("受注先企事部")
	private String norderKjbId;

	/** TeMS代直区分 */
	@JsonProperty("TeMS代直区分")
	private String nkubunDirectType;

	/** DM送付不可区分 */
	@JsonProperty("DM送付不可区分")
	private String nnotTransDmFlg;

	/** メインCEコード */
	@JsonProperty("メインCEコード")
	private String nceCode;

	/** エンドユーザ企事部 */
	@JsonProperty("エンドユーザ企事部")
	private String nendUserKjbId;

	/** 契約先担当者 */
	@JsonProperty("契約先担当者")
	private String nendUserPerson;

	/** 契約先担当者（半角ｶﾅ） */
	@JsonProperty("契約先担当者（半角ｶﾅ）")
	private String nendUserPersonKana;

	/** SI/NI区分 */
	@JsonProperty("SI/NI区分")
	private String nsiKbn;

	/** 商流区分 */
	@JsonProperty("商流区分")
	private String nrootInfo;

	/** 概要 */
	@JsonProperty("概要")
	private String nruleOutlineUpd;

	/** 受付 */
	@JsonProperty("受付")
	private String nruleReceiptUpd;

	/** 切り分け */
	@JsonProperty("切り分け")
	private String nruleJudgeUpd;

	/** CE手配 */
	@JsonProperty("CE手配")
	private String nruleCeUpd;

	/** 部材手配 */
	@JsonProperty("部材手配")
	private String nrulePartsUpd;

	/** 顧客情報 */
	@JsonProperty("顧客情報")
	private String nruleCusInfoUpd;

	/** その他特別ルール */
	@JsonProperty("その他特別ルール")
	private String nruleEtcUpd;

	/** コール依頼元番号 */
	@JsonProperty("コール依頼元番号")
	private String ncallFromId;

	/** 販売店営業担当者 */
	@JsonProperty("販売店営業担当者")
	private String nsalesComSalesMan;

	/** コールセンタ問合せID */
	@JsonProperty("コールセンタ問合せID")
	private String ncontId;

	/** 営業担当者連絡先TEL（CC) */
	@JsonProperty("営業担当者連絡先TEL（CC)")
	private String nsalesEmpPhoneTel;

	/** 営業担当者連絡先メールアドレス（CC) */
	@JsonProperty("営業担当者連絡先メールアドレス（CC)")
	private String nsalesEmpEmail;

	/** 契約情報備考 */
	@JsonProperty("契約情報備考")
	private String ncontNote;

	/** コールセンタ用契約判別フラグ */
	@JsonProperty("コールセンタ用契約判別フラグ")
	private String ncontDistinctFlg;

	/** エンドユーザ担当者TEL（CC) */
	@JsonProperty("エンドユーザ担当者TEL（CC)")
	private String nenduserChargerTel;

	/** エンドユーザ担当者FAX（CC) */
	@JsonProperty("エンドユーザ担当者FAX（CC)")
	private String nenduserChargerFax;

	/** エンドユーザ担当者メールアドレス（CC) */
	@JsonProperty("エンドユーザ担当者メールアドレス（CC)")
	private String nenduserChargerEmail;

	/** 契約開始日 */
	@JsonProperty("契約開始日")
	private String ncontStartDate;

	/** 契約満了日 */
	@JsonProperty("契約満了日")
	private String ncontEndDate;

	/** 移行用キーワード１ */
	@JsonProperty("移行用キーワード１")
	private String nmigarateKeyword1;

	/** 移行用キーワード２ */
	@JsonProperty("移行用キーワード２")
	private String nmigarateKeyword2;

	/** 移行用キーワード３ */
	@JsonProperty("移行用キーワード３")
	private String nmigarateKeyword3;

	/** サポート特記 */
	@JsonProperty("サポート特記")
	private String nsupportSpNote;

	/** 提供サービス番号 */
	@JsonProperty("提供サービス番号")
	private String npServiceNo;

	/** 契約形態 */
	@JsonProperty("契約形態")
	private String ncontractForm;

	/** 提供サービスコード */
	@JsonProperty("提供サービスコード")
	private String npServiceCode;

	/** 開始日 */
	@JsonProperty("開始日")
	private String nserviceFromDate1;

	/** 終了日 */
	@JsonProperty("終了日")
	private String nserviceToDate1;

	/** メニュー番号 */
	@JsonProperty("メニュー番号1")
	private String nserviceMenuNo;

	/** 自営/受託区分 */
	@JsonProperty("自営/受託区分")
	private String nownAcceptFlg;

	/** サービス計画書 */
	@JsonProperty("サービス計画書")
	private String nservicePlan;

	/** サービス購入フラグ */
	@JsonProperty("サービス購入フラグ")
	private String nservicePurchFlg;

	/** オンサイト保守フラグ */
	@JsonProperty("オンサイト保守フラグ")
	private String nonSiteMainteFlg;

	/** オンサイト上限管理単位 */
	@JsonProperty("オンサイト上限管理単位")
	private String nonSiteMainteUnit;

	/** オンサイト訪問上限数 */
	@JsonProperty("オンサイト訪問上限数")
	private String nonSiteMaxTime;

	/** インシデント管理フラグ */
	@JsonProperty("インシデント管理フラグ")
	private String nincidentManageFlg;

	/** インシデント上限管理単位 */
	@JsonProperty("インシデント上限管理単位")
	private String nicidetMaageUit;

	/** インシデント問合せ上限数 */
	@JsonProperty("インシデント問合せ上限数")
	private String nincidentMaxTime;

	/** CE手配完了報告 */
	@JsonProperty("CE手配完了報告")
	private String nceOrderdFlg;

	/** CE確定報告 */
	@JsonProperty("CE確定報告")
	private String nceFixedFlg;

	/** CE到着予定報告 */
	@JsonProperty("CE到着予定報告")
	private String ncePlannedArriveFlg;

	/** CE到着報告 */
	@JsonProperty("CE到着報告")
	private String nceArrivedFlg;

	/** 機器前到着及び作業開始報告 */
	@JsonProperty("機器前到着及び作業開始報告")
	private String nworkStartFlg;

	/** チェックポイント① */
	@JsonProperty("チェックポイント①")
	private String ncheckPoint1Flg;

	/** チェックポイント② */
	@JsonProperty("チェックポイント②")
	private String ncheckPoint2Flg;

	/** チェックポイント③ */
	@JsonProperty("チェックポイント③")
	private String ncheckPoint3Flg;

	/** 作業終了報告 */
	@JsonProperty("作業終了報告")
	private String nworkEndFlg;

	/** 退出報告 */
	@JsonProperty("退出報告")
	private String nceCheckOutFlg;

	/** 部材到着予定報告 */
	@JsonProperty("部材到着予定報告")
	private String npartsToArriveFlg;

	/** 部材到着報告 */
	@JsonProperty("部材到着報告")
	private String npartsArrivedFlg;

	/** 成果物回収報告 */
	@JsonProperty("成果物回収報告")
	private String noutcomeFlg;

	/** 提供サービス備考 */
	@JsonProperty("提供サービス備考")
	private String ncontServiceNote;

	/** リモート可/不可フラグ */
	@JsonProperty("リモート可/不可フラグ")
	private String nremoteAllowedFlg;

	/** 月報作成要否 */
	@JsonProperty("月報作成要否")
	private String nmonthlyReportFlg;

	/** サポート開始日 */
	@JsonProperty("サポート開始日")
	private String nserviceFromDate2;

	/** サポート終了日 */
	@JsonProperty("サポート終了日")
	private String nserviceToDate2;

	/** オーダ番号 */
	@JsonProperty("オーダ番号")
	private String ngvasOrderNo;

	/** NotesPump対象DB */
	@JsonProperty("NotesPump対象DB")
	private String nnotesPumpNote;

	/** 業務連絡 */
	@JsonProperty("業務連絡")
	private String nconnForBusiness;

	/** メールメモ欄 */
	@JsonProperty("メールメモ欄")
	private String nmailNote;

	/** その他機器明細番号 */
	@JsonProperty("その他機器明細番号")
	private String nothMechLineNo;

	/** 保守SS */
	@JsonProperty("保守SS")
	private String nsupportSs;

	/** 機種コード */
	@JsonProperty("機種コード")
	private String nmodelCode;

	/** SIMONS機番 */
	@JsonProperty("SIMONS機番")
	private String nmechNo;

	/** 担当CEコード */
	@JsonProperty("担当CEコード")
	private String nceCode2;

	/** ユーザコード */
	@JsonProperty("ユーザコード")
	private String nsimonsUserCd;

	/** お客様/企事部 */
	@JsonProperty("お客様/企事部")
	private String nuserKgbId;

	/** お客様/担当者（カナ） */
	@JsonProperty("お客様/担当者（カナ）")
	private String nuserPersonKana;

	/** お客様/担当者（漢字） */
	@JsonProperty("お客様/担当者（漢字）")
	private String nuserPerson;

	/** メンテの注意 */
	@JsonProperty("メンテの注意")
	private String nmaintNote;

	/** 設置日 */
	@JsonProperty("設置日")
	private String nestablishedDate;

	/** 購入形態 */
	@JsonProperty("購入形態")
	private String npurchType;

	/** 納入形態 */
	@JsonProperty("納入形態")
	private String nsupplyType;

	/** 納入機器区分 */
	@JsonProperty("納入機器区分")
	private String nsupplyMechType;

	/** 保証・開始年月日 */
	@JsonProperty("保証・開始年月日")
	private String nguaranteeFromDate;

	/** 保証・終了年月日 */
	@JsonProperty("保証・終了年月日")
	private String nguaranteeToDate;

	/** 点検月 */
	@JsonProperty("点検月")
	private String ncheckMonth;

	/** RMCコード */
	@JsonProperty("RMCコード")
	private String nrmcCode;

	/** メニュー番号 */
	@JsonProperty("メニュー番号2")
	private String nmenuNo;

	/** 管理ID */
	@JsonProperty("管理ID")
	private String ncontrolNo;

	/** 契約区 */
	@JsonProperty("契約区")
	private String ncontractDev;

	/** 設置SS */
	@JsonProperty("設置SS")
	private String nestablishSs;

	/** メーカ型番 */
	@JsonProperty("メーカ型番")
	private String nmakerModelCode;

	/** 部品番号 */
	@JsonProperty("部品番号")
	private String ninvPartNo;

	/** シリアルNo. */
	@JsonProperty("シリアルNo.")
	private String nserialNo;

	/** ホスト名 */
	@JsonProperty("ホスト名")
	private String nhostName;

	/** コンフィグ名 */
	@JsonProperty("コンフィグ名")
	private String nconfigName;

	/** IOSファイル名 */
	@JsonProperty("IOSファイル名")
	private String niosFileName;

	/** メモリサイズ */
	@JsonProperty("メモリサイズ")
	private String nmemory;

	/** 備考 */
	@JsonProperty("備考1")
	private String nnote;

	/** 第一倉庫 */
	@JsonProperty("第一倉庫")
	private String nwarehouseFirst;

	/** お客様/担当者電話番号（CC) */
	@JsonProperty("お客様/担当者電話番号（CC)")
	private String nuserPersonTel;

	/** その他機器表示順（CC) */
	@JsonProperty("その他機器表示順（CC)")
	private String nothMechSortNo;

	/** その他機器情報備考（CC) */
	@JsonProperty("その他機器情報備考（CC)")
	private String nothMechNote;

	/** その他機器契約開始日 */
	@JsonProperty("その他機器契約開始日")
	private String nothMechFromDate;

	/** その他機器契約終了日 */
	@JsonProperty("その他機器契約終了日")
	private String nothMechToDate;

	/** 管理番号 */
	@JsonProperty("管理番号")
	private String nmanageNo;

	/** お客様機械No. */
	@JsonProperty("お客様機械No.")
	private String nuserMachineNo;

	/** 契約機器特記 */
	@JsonProperty("契約機器特記")
	private String nmechSpNote;

	/** その他構成品明細番号 */
	@JsonProperty("その他構成品明細番号")
	private String nothComponentLineNo;

	/** メーカ型番（オプション） */
	@JsonProperty("メーカ型番（オプション）")
	private String nopMakerModelNo;

	/** 部品番号（オプション） */
	@JsonProperty("部品番号（オプション）")
	private String nopInvPartNo;

	/** シリアルNo.（オプション） */
	@JsonProperty("シリアルNo.（オプション）")
	private String ncompSerialNo;

	/** 数量 */
	@JsonProperty("数量")
	private String nopQuantity;

	/** 備考 */
	@JsonProperty("備考2")
	private String nopNote;

	/** 第一倉庫（オプション） */
	@JsonProperty("第一倉庫（オプション）")
	private String nopWarehouseFirst;

}