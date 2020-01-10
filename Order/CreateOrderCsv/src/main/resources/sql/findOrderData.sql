SELECT
  rownum as id,
--data.contract_number as contract_number,
  data.contract_id as contract_id_temp,
  data.immutable_cont_ident_number as contract_number,
  data.quantity as quantity,
  data.ricoh_item_code as ricoh_item_code,
  data.item_contract_name as item_contract_name,
  data.conclusion_preferred_date as conclusion_preferred_date,
  data.shortest_delivery_date as shortest_delivery_date,
  data.pic_name as pic_name,
  data.pic_name_kana as pic_name_kana,
  data.post_number as post_number,
  data.address as address,
  data.company_name as company_name,
  data.office_name as office_name,
  data.pic_phone_number as pic_phone_number,
  data.pic_fax_number as pic_fax_number,
  data.pic_mail_address as pic_mail_address,
  data.extends_parameter as extends_parameter
FROM
 (
  SELECT
--	cont.contract_number,                              --Œ_–ñ.”Ô†
	cont.id as contract_id,
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
	) as office_name,
	location.pic_phone_number,                         --İ’uæ(Œ_–ñ—p).MoM”ñ˜AŒg_’S“–Ò“d˜b”Ô†
	location.pic_fax_number,                           --İ’uæ(Œ_–ñ—p).MoM”ñ˜AŒg_’S“–ÒFAX”Ô†
	customer.pic_mail_address,                         --ŒÚ‹q(Œ_–ñ—p).MoM”ñ˜AŒg_’S“–Òƒ[ƒ‹ƒAƒhƒŒƒX
	detail.extends_parameter                           --Œ_–ñ–¾×.Šg’£€–Ú
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
