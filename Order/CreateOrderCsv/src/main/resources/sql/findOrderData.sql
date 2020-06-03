SELECT
  rownum AS id,
  data.contract_id AS contract_id_temp,
  data.contract_number AS contract_number,
  data.contract_branch_number AS contract_branch_number,
  data.quantity AS quantity,
  data.ricoh_item_code AS ricoh_item_code,
  data.item_contract_name AS item_contract_name,
  data.conclusion_preferred_date AS conclusion_preferred_date,
  data.shortest_delivery_date AS shortest_delivery_date,
  data.pic_name AS pic_name,
  data.pic_name_kana AS pic_name_kana,
  data.post_number AS post_number,
  data.address AS address,
  data.company_name AS company_name,
  data.office_name AS office_name,
  data.pic_phone_number AS pic_phone_number,
  data.pic_fax_number AS pic_fax_number,
  data.pic_mail_address AS pic_mail_address,
  data.extends_parameter AS extends_parameter,
  data.contract_detail_id AS contract_detail_id,
  data.updated_at AS updated_at,
  data.extends_parameter_iterance AS extends_parameter_iterance
FROM
 (
  SELECT
	cont.id AS contract_id,                            --契約.契約ID
	cont.contract_number,                              --契約.契約番号
	cont.contract_branch_number,                       --契約.契約番号枝番
	detail.quantity,                                   --契約明細.数量
	item.ricoh_item_code,                              --品種(契約用).リコー品種コード
	item.item_contract_name,                           --品種(契約用).商品名
	cont.conclusion_preferred_date,                    --契約.サービス希望日
	im.shortest_delivery_date,                         --品種マスタ.最短納入日数
	location.pic_name,                                 --設置先(契約用).MoM非連携_担当者氏名
	location.pic_name_kana,                            --設置先(契約用).MoM非連携_担当者氏名(カナ)
	location.post_number,                              --設置先(契約用).郵便番号
	location.address,                                  --設置先(契約用).住所
	location.company_name,                             --設置先(契約用).企業名
	CONCAT(
		location.office_name,                          --設置先(契約用).事業所名
		location.pic_dept_name                         --設置先(契約用).MoM非連携_担当者部署
	) AS office_name,
	location.pic_phone_number,                         --設置先(契約用).MoM非連携_担当者電話番号
	location.pic_fax_number,                           --設置先(契約用).MoM非連携_担当者FAX番号
	customer.pic_mail_address,                         --顧客(契約用).MoM非連携_担当者メールアドレス
	detail.extends_parameter,                          --契約明細.拡張項目
	detail.id AS contract_detail_id,                   --契約明細.ID
	cont.updated_at,                                   --契約.更新日時
	product.extends_parameter_iterance                 --商品(契約用).拡張項目繰返
  FROM contract cont                                                 --契約
  INNER JOIN contract_detail detail                                  --契約明細
          ON detail.contract_id = cont.id
  INNER JOIN item_contract item                                      --品種(契約用)
          ON item.contract_detail_id = detail.id
  INNER JOIN item_master im                                          --品種マスタ
          ON im.id = item.item_master_id
  INNER JOIN contract_installation_location location                 --設置先(契約用)
          ON location.contract_id = cont.id
  INNER JOIN customer_contract customer                              --顧客(契約用)
          ON customer.contract_id = cont.id
  INNER JOIN product_contract product                                --商品(契約用)
          ON product.contract_id = cont.id
  INNER JOIN product_master pm                                       --商品マスタ
          ON pm.id = product.product_master_id
  WHERE pm.product_class_div = 'SIM'
    AND cont.workflow_status = '3'
    AND cont.lifecycle_status = '2'
    AND item.cost_type != '1'
    {{#contractType}}
    AND JSON_EXISTS(JSON_QUERY(product.EXTENDS_PARAMETER_ITERANCE, '$.extendsParameterList'), {{&contractType}} )
    {{/contractType}}
 ) data
ORDER BY id
