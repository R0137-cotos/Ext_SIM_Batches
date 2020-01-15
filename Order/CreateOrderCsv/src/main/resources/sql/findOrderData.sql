SELECT
  rownum AS id,
  data.contract_id AS contract_id_temp,
  data.immutable_cont_ident_number AS contract_number,
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
  data.contract_detail_id AS contract_detail_id
FROM
 (
  SELECT
	cont.id AS contract_id,                            --Œ_–ñ.Œ_–ñID
	cont.immutable_cont_ident_number,                  --Œ_–ñ.P‹vŒ_–ñ¯•Ê”Ô†
	detail.quantity,                                   --Œ_–ñ–¾×.”—Ê
	item.ricoh_item_code,                              --•ií(Œ_–ñ—p).ƒŠƒR[•iíƒR[ƒh
	item.item_contract_name,                           --•ií(Œ_–ñ—p).¤•i–¼
	cont.conclusion_preferred_date,                    --Œ_–ñ.ƒT[ƒrƒXŠó–]“ú
	im.shortest_delivery_date,                         --•iíƒ}ƒXƒ^.Å’Z”[“ü“ú”
	location.pic_name,                                 --İ’uæ(Œ_–ñ—p).MoM”ñ˜AŒg_’S“–Ò–¼
	location.pic_name_kana,                            --İ’uæ(Œ_–ñ—p).MoM”ñ˜AŒg_’S“–Ò–¼(ƒJƒi)
	location.post_number,                              --İ’uæ(Œ_–ñ—p).—X•Ö”Ô†
	location.address,                                  --İ’uæ(Œ_–ñ—p).ZŠ
	location.company_name,                             --İ’uæ(Œ_–ñ—p).Šé‹Æ–¼
	CONCAT(
		location.office_name,                          --İ’uæ(Œ_–ñ—p).–‹ÆŠ–¼
		location.pic_dept_name                         --İ’uæ(Œ_–ñ—p).MoM”ñ˜AŒg_’S“–Ò•”
	) AS office_name,
	location.pic_phone_number,                         --İ’uæ(Œ_–ñ—p).MoM”ñ˜AŒg_’S“–Ò“d˜b”Ô†
	location.pic_fax_number,                           --İ’uæ(Œ_–ñ—p).MoM”ñ˜AŒg_’S“–ÒFAX”Ô†
	customer.pic_mail_address,                         --ŒÚ‹q(Œ_–ñ—p).MoM”ñ˜AŒg_’S“–Òƒ[ƒ‹ƒAƒhƒŒƒX
	detail.extends_parameter,                          --Œ_–ñ–¾×.Šg’£€–Ú
	detail.id AS contract_detail_id                             --Œ_–ñ–¾×.ID
  FROM contract cont                                                 --Œ_–ñ
  INNER JOIN contract_detail detail                                  --Œ_–ñ–¾×
          ON detail.contract_id = cont.id
  INNER JOIN item_contract item                                      --•ií(Œ_–ñ—p)
          ON item.contract_detail_id = detail.id
  INNER JOIN item_master im                                          --•iíƒ}ƒXƒ^
          ON im.id = item.item_master_id
  INNER JOIN contract_installation_location location                 --İ’uæ(Œ_–ñ—p)
          ON location.contract_id = cont.id
  INNER JOIN customer_contract customer                              --ŒÚ‹q(Œ_–ñ—p)
          ON customer.contract_id = cont.id
  WHERE cont.workflow_status = '3'
    AND item.cost_type != '1'
 ) data
ORDER BY id
