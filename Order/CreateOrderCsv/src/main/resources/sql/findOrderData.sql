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
--	cont.contract_number,                              --�_��.�ԍ�
	cont.id as contract_id,
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
	) as office_name,
	location.pic_phone_number,                         --�ݒu��(�_��p).MoM��A�g_�S���ғd�b�ԍ�
	location.pic_fax_number,                           --�ݒu��(�_��p).MoM��A�g_�S����FAX�ԍ�
	customer.pic_mail_address,                         --�ڋq(�_��p).MoM��A�g_�S���҃��[���A�h���X
	detail.extends_parameter                           --�_�񖾍�.�g������
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
