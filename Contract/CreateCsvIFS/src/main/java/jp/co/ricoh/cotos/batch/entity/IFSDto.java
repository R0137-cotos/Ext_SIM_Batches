package jp.co.ricoh.cotos.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.Data;

@Entity
@Data
public class IFSDto {

	@Id
	private long id;

	/** 契約No. */
	private String contractId;

	/** 契約明細No. */
	private String contractDetailId;

	/** ライフサイクル状態 */
	private String lifecycleStatus;

	/** 強制更新フラグ */
	private String nforceUpdFlg;

	/** 処理モードフラグ */
	private String nprocessModeFlg;

	/** 契約書No. */
	private String ncontractSheetNo;

	/** タスクフォースNo. */
	private String ntaskForceNo;

	/** 案件No. */
	private String nitemNo;

	/** 契約締結日 */
	private String fromDate;

	/** 契約管理区分 */
	private String contractType;

	/** 売上先コード */
	private String nbillAccountCode;

	/** サブ店コード */
	private String nsubtencd;

	/** 営業担当者コード */
	private String nsalesEmpCode;

	/** 承認者コード */
	private String nacknowledgerCode;

	/** 売上課所コード */
	private String nsalesOrgCode;

	/** 支店課所コード */
	private String nbranchSection;

	/** TMCコード */
	private String ntmcCode;

	/** SOコード */
	private String nsoCode;

	/** THコード */
	private String nthCode;

	/** TSコード */
	private String ntsCode;

	/** TS名 */
	private String ntsDesc;

	/** 受注先企事部 */
	private String norderKjbId;

	/** TeMS代直区分 */
	private String nkubunDirectType;

	/** DM送付不可区分 */
	private String nnotTransDmFlg;

	/** メインCEコード */
	private String nceCode;

	/** エンドユーザ企事部 */
	private String nendUserKjbId;

	/** 契約先担当者 */
	private String nendUserPerson;

	/** 契約先担当者（半角ｶﾅ） */
	private String nendUserPersonKana;

	/** SI/NI区分 */
	private String nsiKbn;

	/** 商流区分 */
	private String nrootInfo;

	/** 概要 */
	private String nruleOutlineUpd;

	/** 受付 */
	private String nruleReceiptUpd;

	/** 切り分け */
	private String nruleJudgeUpd;

	/** CE手配 */
	private String nruleCeUpd;

	/** 部材手配 */
	private String nrulePartsUpd;

	/** 顧客情報 */
	private String nruleCusInfoUpd;

	/** その他特別ルール */
	private String nruleEtcUpd;

	/** コール依頼元番号 */
	private String ncallFromId;

	/** 販売店営業担当者 */
	private String nsalesComSalesMan;

	/** コールセンタ問合せID */
	private String ncontId;

	/** 営業担当者連絡先TEL（CC) */
	private String nsalesEmpPhoneTel;

	/** 営業担当者連絡先メールアドレス（CC) */
	private String nsalesEmpEmail;

	/** 契約情報備考 */
	private String ncontNote;

	/** コールセンタ用契約判別フラグ */
	private String ncontDistinctFlg;

	/** エンドユーザ担当者TEL（CC) */
	private String nenduserChargerTel;

	/** エンドユーザ担当者FAX（CC) */
	private String nenduserChargerFax;

	/** エンドユーザ担当者メールアドレス（CC) */
	private String nenduserChargerEmail;

	/** 契約開始日 */
	private String ncontStartDate;

	/** 契約満了日 */
	private String ncontEndDate;

	/** 移行用キーワード１ */
	@Column(name = "nmigarate_keyword1")
	private String nmigarateKeyword1;

	/** 移行用キーワード２ */
	@Column(name = "nmigarate_keyword2")
	private String nmigarateKeyword2;

	/** 移行用キーワード３ */
	@Column(name = "nmigarate_keyword3")
	private String nmigarateKeyword3;

	/** サポート特記 */
	private String nsupportSpNote;

	/** 提供サービス番号 */
	private String npServiceNo;

	/** 契約形態 */
	private String ncontractForm;

	/** 提供サービスコード */
	private String npServiceCode;

	/** 開始日 */
	@Column(name = "nservice_from_date1")
	private String nserviceFromDate1;

	/** 終了日 */
	@Column(name = "nservice_to_date1")
	private String nserviceToDate1;

	/** メニュー番号 */
	private String nserviceMenuNo;

	/** 自営/受託区分 */
	private String nownAcceptFlg;

	/** サービス計画書 */
	private String nservicePlan;

	/** サービス購入フラグ */
	private String nservicePurchFlg;

	/** オンサイト保守フラグ */
	private String nonSiteMainteFlg;

	/** オンサイト上限管理単位 */
	private String nonSiteMainteUnit;

	/** オンサイト訪問上限数 */
	private String nonSiteMaxTime;

	/** インシデント管理フラグ */
	private String nincidentManageFlg;

	/** インシデント上限管理単位 */
	private String nicidetMaageUit;

	/** インシデント問合せ上限数 */
	private String nincidentMaxTime;

	/** CE手配完了報告 */
	private String nceOrderdFlg;

	/** CE確定報告 */
	private String nceFixedFlg;

	/** CE到着予定報告 */
	private String ncePlannedArriveFlg;

	/** CE到着報告 */
	private String nceArrivedFlg;

	/** 機器前到着及び作業開始報告 */
	private String nworkStartFlg;

	/** チェックポイント① */
	@Column(name = "ncheck_point1_flg")
	private String ncheckPoint1Flg;

	/** チェックポイント② */
	@Column(name = "ncheck_point2_flg")
	private String ncheckPoint2Flg;

	/** チェックポイント③ */
	@Column(name = "ncheck_point3_flg")
	private String ncheckPoint3Flg;

	/** 作業終了報告 */
	private String nworkEndFlg;

	/** 退出報告 */
	private String nceCheckOutFlg;

	/** 部材到着予定報告 */
	private String npartsToArriveFlg;

	/** 部材到着報告 */
	private String npartsArrivedFlg;

	/** 成果物回収報告 */
	private String noutcomeFlg;

	/** 提供サービス備考 */
	private String ncontServiceNote;

	/** リモート可/不可フラグ */
	private String nremoteAllowedFlg;

	/** 月報作成要否 */
	private String nmonthlyReportFlg;

	/** サポート開始日 */
	@Column(name = "nservice_from_date2")
	private String nserviceFromDate2;

	/** サポート終了日 */
	@Column(name = "nservice_to_date2")
	private String nserviceToDate2;

	/** オーダ番号 */
	private String ngvasOrderNo;

	/** NotesPump対象DB */
	private String nnotesPumpNote;

	/** 業務連絡 */
	private String nconnForBusiness;

	/** メールメモ欄 */
	private String nmailNote;

	/** その他機器明細番号 */
	private String nothMechLineNo;

	/** 保守SS */
	private String nsupportSs;

	/** 機種コード */
	private String nmodelCode;

	/** SIMONS機番 */
	private String nmechNo;

	/** 担当CEコード */
	private String nceCode2;

	/** ユーザコード */
	private String nsimonsUserCd;

	/** お客様/企事部 */
	private String nuserKgbId;

	/** お客様/担当者（カナ） */
	private String nuserPersonKana;

	/** お客様/担当者（漢字） */
	private String nuserPerson;

	/** メンテの注意 */
	private String nmaintNote;

	/** 設置日 */
	private String nestablishedDate;

	/** 購入形態 */
	private String npurchType;

	/** 納入形態 */
	private String nsupplyType;

	/** 納入機器区分 */
	private String nsupplyMechType;

	/** 保証・開始年月日 */
	private String nguaranteeFromDate;

	/** 保証・終了年月日 */
	private String nguaranteeToDate;

	/** 点検月 */
	private String ncheckMonth;

	/** RMCコード */
	private String nrmcCode;

	/** メニュー番号 */
	private String nmenuNo;

	/** 管理ID */
	private String ncontrolNo;

	/** 契約区 */
	private String ncontractDev;

	/** 設置SS */
	private String nestablishSs;

	/** メーカ型番 */
	private String nmakerModelCode;

	/** 部品番号 */
	private String ninvPartNo;

	/** シリアルNo. */
	private String nserialNo;

	/** ホスト名 */
	private String nhostName;

	/** コンフィグ名 */
	private String nconfigName;

	/** IOSファイル名 */
	private String niosFileName;

	/** メモリサイズ */
	private String nmemory;

	/** 備考 */
	private String nnote;

	/** 第一倉庫 */
	private String nwarehouseFirst;

	/** お客様/担当者電話番号（CC) */
	private String nuserPersonTel;

	/** その他機器表示順（CC) */
	private String nothMechSortNo;

	/** その他機器情報備考（CC) */
	private String nothMechNote;

	/** その他機器契約開始日 */
	private String nothMechFromDate;

	/** その他機器契約終了日 */
	private String nothMechToDate;

	/** 管理番号 */
	private String nmanageNo;

	/** お客様機械No. */
	private String nuserMachineNo;

	/** 契約機器特記 */
	private String nmechSpNote;

	/** その他構成品明細番号 */
	private String nothComponentLineNo;

	/** メーカ型番（オプション） */
	private String nopMakerModelNo;

	/** 部品番号（オプション） */
	private String nopInvPartNo;

	/** シリアルNo.（オプション） */
	private String ncompSerialNo;

	/** 数量 */
	private String nopQuantity;

	/** 備考 */
	private String nopNote;

	/** 第一倉庫（オプション） */
	private String nopWarehouseFirst;

	/** 品種コード */
	private String ricohItemCode;

	/** 契約Noヘッダ */
	private String contractNoHeader;

	/** シーケンスによる契約No */
	private Long sequenceNo;
	
	/** 数量 */
	private int quantity;
	
	/** 拡張項目 */
	private String extendsParameter;
	
	/** 拡張項目繰返 */
	private String extendsParameterIterance;

}
