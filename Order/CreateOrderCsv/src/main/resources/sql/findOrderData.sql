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
	cont.id AS contract_id,                            --�_��.�_��ID
	cont.immutable_cont_ident_number,                  --�_��.�P�v�_�񎯕ʔԍ�
	detail.quantity,                                   --�_�񖾍�.����
	item.ricoh_item_code,                              --�i��(�_��p).���R�[�i��R�[�h
	item.item_contract_name,                           --�i��(�_��p).���i��
	cont.conclusion_preferred_date,                    --�_��.�T�[�r�X��]��
	im.shortest_delivery_date,                         --�i��}�X�^.�ŒZ�[������
	location.pic_name,                                 --�ݒu��(�_��p).MoM��A�g_�S���Ҏ���
	location.pic_name_kana,                            --�ݒu��(�_��p).MoM��A�g_�S���Ҏ���(�J�i)
	location.post_number,                              --�ݒu��(�_��p).�X�֔ԍ�
	location.address,                                  --�ݒu��(�_��p).�Z��
	location.company_name,                             --�ݒu��(�_��p).��Ɩ�
	CONCAT(
		location.office_name,                          --�ݒu��(�_��p).���Ə���
		location.pic_dept_name                         --�ݒu��(�_��p).MoM��A�g_�S���ҕ���
	) AS office_name,
	location.pic_phone_number,                         --�ݒu��(�_��p).MoM��A�g_�S���ғd�b�ԍ�
	location.pic_fax_number,                           --�ݒu��(�_��p).MoM��A�g_�S����FAX�ԍ�
	customer.pic_mail_address,                         --�ڋq(�_��p).MoM��A�g_�S���҃��[���A�h���X
	detail.extends_parameter,                          --�_�񖾍�.�g������
	detail.id AS contract_detail_id                             --�_�񖾍�.ID
  FROM contract cont                                                 --�_��
  INNER JOIN contract_detail detail                                  --�_�񖾍�
          ON detail.contract_id = cont.id
  INNER JOIN item_contract item                                      --�i��(�_��p)
          ON item.contract_detail_id = detail.id
  INNER JOIN item_master im                                          --�i��}�X�^
          ON im.id = item.item_master_id
  INNER JOIN contract_installation_location location                 --�ݒu��(�_��p)
          ON location.contract_id = cont.id
  INNER JOIN customer_contract customer                              --�ڋq(�_��p)
          ON customer.contract_id = cont.id
  WHERE cont.workflow_status = '3'
    AND item.cost_type != '1'
 ) data
ORDER BY id
