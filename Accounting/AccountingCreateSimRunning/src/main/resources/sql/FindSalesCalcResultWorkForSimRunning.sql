select
    rownum,
    target.*,
    mem.emb_emp_number as emp_number
from (
    select
        c.rj_manage_number,
        c.id as contract_id,
        cd.id as contract_detail_id,
        c.contract_number,
        c.contact_no,
        cd.order_no,
        c.commercial_flow_div,
        ic.cost_type,
        ic.item_type,
        '20'as ffm_data_ptn,
        ic.ricoh_item_code,
        ic.item_contract_name,
        c.billing_customer_sp_code,
        cd.quantity,
        cd.amount_summary,
        ic.rj_dividing_price,
        ic.rj_purchase_price,
        ic.mother_store_price,
        ic.r_cost,
        null as cost,
        cd.unit_price,
        c.service_term_start,
        c.service_term_end,
        null as trans_to_service_org_code,
        c.immutable_cont_ident_number,
        cc.company_name_kana,
        c.purchase_manage_number,
        pm.product_class_div as accounting_product_class_div,
        null as sus_sal_mom_shain_cd,
        c.delivery_cd
    from
        contract c,
        contract_detail cd,
        product_contract pc,
        product_master pm,
        item_contract ic,
        customer_contract cc,
        v_valid_contract_period_history history
    where
        c.id = pc.contract_id AND
        c.id = cd.contract_id AND
        pc.product_master_id = pm.id AND
        c.id = cc.contract_id AND
        pm.product_class_div = 'SIM' AND
        cd.id = ic.contract_detail_id AND
        (cd.running_account_sales_date is NULL or trunc(cd.running_account_sales_date, 'MONTH') < to_date(substr(:baseDate, 1, 6), 'YYYYMM')) AND
        ic.cost_type in ('2','4') AND
        c.id = history.contract_id AND
        to_date(:baseDate, 'YYYY/MM/DD') between history.contract_date_start and history.contract_date_end
    union all
    select
        c.rj_manage_number,
        c.id as contract_id,
        cd.id as contract_detail_id,
        c.contract_number,
        c.contact_no,
        cd.order_no,
        c.commercial_flow_div,
        ic.cost_type,
        ic.item_type,
        '31'as ffm_data_ptn,
        ic.ricoh_item_code,
        ic.item_contract_name,
        c.billing_customer_sp_code,
        cd.quantity,
        cd.amount_summary,
        ic.rj_dividing_price,
        ic.rj_purchase_price,
        ic.mother_store_price,
        ic.r_cost,
        idc.price cost,
        cd.unit_price,
        c.service_term_start,
        c.service_term_end,
        wwoaic.cubic_org_id,
        c.immutable_cont_ident_number,
        null as company_name_kana,
        c.purchase_manage_number,
        pm.product_class_div as accounting_product_class_div,
        mv_108.sus_sal_mom_shain_cd as sus_sal_mom_shain_cd,
        c.delivery_cd
    from
        contract c
        inner join contract_detail cd on c.id = cd.contract_id
        inner join product_contract pc on c.id = pc.contract_id
        inner join product_master pm on pc.product_master_id = pm.id
        inner join item_contract ic on cd.id = ic.contract_detail_id
        inner join item_detail_contract idc on ic.id = idc.item_contract_id
        inner join MV_WJMOC020_ORG_ALL_INFO_COM wwoaic on idc.trans_to_service_org_code = wwoaic.org_id
        left join mv_t_jmci101 mv_101 on mv_101.original_system_code = c.billing_customer_sp_code and mv_101.sales_unit_code = '3139'
        left join mv_t_jmci108 mv_108 on mv_101.customer_site_number = mv_108.customer_site_number
    where
        pm.product_class_div = 'SIM' AND
        (cd.running_account_sales_date is NULL or trunc(cd.running_account_sales_date, 'MONTH') < to_date(substr(:baseDate, 1, 6), 'YYYYMM')) AND
        ic.cost_type in ('2','4') AND
        idc.initial_running_div = '2' AND
        exists (
            select
                1
            from
                v_valid_contract_period_history history
            where
                c.id = history.contract_id AND
                to_date(:baseDate, 'YYYY/MM/DD') between history.contract_date_start and history.contract_date_end AND
                history.PRODUCT_CLASS_DIV = 'SIM'
        )
) target
left join mv_employee_master mem on mem.emp_id = target.sus_sal_mom_shain_cd