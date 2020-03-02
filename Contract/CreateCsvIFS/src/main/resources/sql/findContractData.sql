with key_view as
(
select
  'N' as nforce_upd_flg,
  co.id as contract_id,
  detail.id as contract_detail_id,
  detail.quantity as quantity,
  detail.extends_parameter as extends_parameter,
  pc.extends_parameter_iterance as extends_parameter_iterance
 from
  contract co,
  contract_detail detail,
  product_contract pc,
  product_master pm
 where
  co.lifecycle_status = '6'
 and
  co.id = detail.contract_id
 and
  co.id = pc.contract_id
 and
  pc.product_master_id = pm.id
 and
  pm.product_class_div IN :productClassDiv
)
select
 rownum as id,
 rownum as nmigarate_keyword3,
 null as np_service_no,
 tmp.*
from
(
select
 kv.contract_id as contract_id,
 kv.contract_detail_id as contract_detail_id,
 kv.nforce_upd_flg as nforce_upd_flg,
 'N' as nprocess_mode_flg,
 customer.company_name as ncontract_sheet_no,
 null as ntask_force_no,
 null as nitem_no,
 null as from_date,
 '2' as contract_type,
 ifs.n_bill_acount_code as nbill_account_code,
 null as nsubtencd,
 'X92618' as nsales_emp_code,
 null as nacknowledger_code,
 '000133' as nsales_org_code,
 'J' as nbranch_section,
 null as ntmc_code,
 null as nso_code,
 'A000000' as nth_code,
 null as nts_code,
 null as nts_desc,
 null as norder_kjb_id,
 null as nkubun_direct_type,
 ifs.n_not_trans_dm_flg as nnot_trans_dm_flg,
 '99999' as nce_code,
 customer.mom_cust_id as nend_user_kjb_id,
 customer.pic_name as nend_user_person,
 null as nend_user_person_kana,
 '0' as nsi_kbn,
 '99' as nroot_info,
 null as nrule_outline_upd,
 null as nrule_receipt_upd,
 null as nrule_judge_upd,
 null as nrule_ce_upd,
 null as nrule_parts_upd,
 null as nrule_cus_info_upd,
 null as nrule_etc_upd,
 null as ncall_from_id,
 null as nsales_com_sales_man,
 null as ncont_id,
 null as nsales_emp_phone_tel,
 null as nsales_emp_email,
 null as ncont_note,
 'Y' as ncont_distinct_flg,
 customer.pic_phone_number as nenduser_charger_tel,
 null as nenduser_charger_fax,
 customer.pic_mail_address as nenduser_charger_email,
 null as ncont_start_date,
 null as ncont_end_date,
 ifs.n_migarate_keyword1 as nmigarate_keyword1,
 null as nmigarate_keyword2,
 null as nsupport_sp_note,
 null as ncontract_form,
 null as np_service_code,/*汎用マスタから取得*/
 to_char(co.service_term_start, 'yyyymmdd') as nservice_from_date1,
 to_char(co.service_term_end, 'yyyymmdd') as nservice_to_date1,
 null as nservice_menu_no,
 '3' as nown_accept_flg,
 null as nservice_plan,
 ifs.n_service_purch_flg as nservice_purch_flg,
 ifs.n_on_site_mainte_flg as non_site_mainte_flg,
 null as non_site_mainte_unit,
 null as non_site_max_time,
 ifs.n_incident_manage_flg as nincident_manage_flg,
 null as nicidet_maage_uit,
 null as nincident_max_time,
 ifs.n_ce_orderd_flg as nce_orderd_flg,
 ifs.n_ce_fixed_flg as nce_fixed_flg,
 ifs.n_ce_planned_arrive_flg as nce_planned_arrive_flg,
 ifs.n_ce_arrived_flg as nce_arrived_flg,
 ifs.n_work_start_flg as nwork_start_flg,
 ifs.n_check_point1_flg as ncheck_point1_flg,
 ifs.n_check_point2_flg as ncheck_point2_flg,
 ifs.n_check_point3_flg as ncheck_point3_flg,
 ifs.n_work_end_flg as nwork_end_flg,
 ifs.n_ce_check_out_flg as nce_check_out_flg,
 ifs.n_parts_to_arrive_flg as nparts_to_arrive_flg,
 ifs.n_parts_arrived_flg as nparts_arrived_flg,
 ifs.n_out_come_flg as noutcome_flg,
 null as ncont_service_note,
 ifs.n_remote_allowed_flg as nremote_allowed_flg,
 ifs.n_monthry_report_flg as nmonthly_report_flg,
 to_char(co.service_term_start, 'yyyymmdd') as nservice_from_date2,
 to_char(co.service_term_end, 'yyyymmdd') as nservice_to_date2,
 null as ngvas_order_no,
 null as nnotes_pump_note,
 null as nconn_for_business,
 null as nmail_note,
 null as noth_mech_line_no,
 null as nsupport_ss,
 ce.equipment_code as nmodel_code,
 null as nmech_no,
 '99999' as nce_code2,
 null as nsimons_user_cd,
 customer.mom_cust_id as nuser_kgb_id,
 null as nuser_person_kana,
 customer.pic_name as nuser_person,
 null as nmaint_note,
 null as nestablished_date,
 null as npurch_type,
 null as nsupply_type,
 null as nsupply_mech_type,
 null as nguarantee_from_date,
 null as nguarantee_to_date,
 null as ncheck_month,
 null as nrmc_code,
 null as nmenu_no,
 co.immutable_cont_ident_number as ncontrol_no,
 null as ncontract_dev,
 null as nestablish_ss,
 null as nmaker_model_code,
 null as ninv_part_no,
 null as nserial_no,
 null as nhost_name,
 null as nconfig_name,
 null as nios_file_name,
 null as nmemory,
 null as nnote,
 null as nwarehouse_first,
 customer.pic_phone_number as nuser_person_tel,
 null as noth_mech_sort_no,
 null as noth_mech_note,
 null as noth_mech_from_date,
 null as noth_mech_to_date,
 co.immutable_cont_ident_number as nmanage_no,
 null as nuser_machine_no,
 null as nmech_sp_note,
 null as noth_component_line_no,
 null as nop_maker_model_no,
 null as nop_inv_part_no,
 null as ncomp_serial_no,
 null as nop_quantity,
 null as nop_note,
 null as nop_warehouse_first,
 item.ricoh_item_code as ricoh_item_code,
 ifs.contract_no_header as contract_no_header,
 null as sequence_no,
 kv.quantity as quantity,
 kv.extends_parameter as extends_parameter,
 kv.extends_parameter_iterance as extends_parameter_iterance
from
 key_view kv,
 contract co,
 customer_contract customer,
 item_contract item,
 item_master im,
 ifs_csv_master ifs,
 contract_equipment ce
where
 kv.contract_id = co.id
and
 kv.contract_id = customer.contract_id
and
 kv.contract_detail_id = item.contract_detail_id
and
 ifs.product_master_id = item.product_master_id
and
 item.item_master_id = im.id
and
 im.ifs_linkage_flg = 1
and
 co.ifs_linkage_csv_create_status = 0
and
 co.id = ce.contract_id(+)
order by contract_id, contract_detail_id
) tmp