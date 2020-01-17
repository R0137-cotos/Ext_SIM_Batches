select
    rownum,
    target.*
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
        cc.company_name_kana
    from
        contract c,
        contract_detail cd,
        product_contract pc,
        product_master pm,
        item_contract ic,
        customer_contract cc
    where
        c.id = pc.contract_id AND
        c.id = cd.contract_id AND
        pc.product_master_id = pm.id AND
        c.id = cc.contract_id AND
        pm.product_class_div = 'SIM' AND
        cd.id = ic.contract_detail_id AND
        (cd.running_account_sales_date is NULL or trunc(cd.running_account_sales_date, 'MONTH') < to_date(substr(:baseDate, 1, 6), 'YYYYMM')) AND
        ic.cost_type in ('2','4') AND
        c.lifecycle_status = '6' AND
        exists (
            select
                1
            from
                item_detail_contract idc
            where
                idc.initial_running_div = '2' AND
                ic.id = idc.item_contract_id
        )
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
        null as company_name_kana
    from
        contract c,
        contract_detail cd,
        product_contract pc,
        product_master pm,
        item_contract ic,
        item_detail_contract idc,
        MV_WJMOC020_ORG_ALL_INFO_COM wwoaic
    where
        c.id = pc.contract_id AND
        c.id = cd.contract_id AND
        pc.product_master_id = pm.id AND
        pm.product_class_div = 'SIM' AND
        cd.id = ic.contract_detail_id AND
        ic.id = idc.item_contract_id AND
        (cd.running_account_sales_date is NULL or trunc(cd.running_account_sales_date, 'MONTH') < to_date(substr(:baseDate, 1, 6), 'YYYYMM')) AND
        ic.cost_type in ('2','4') AND
        c.lifecycle_status = '6' AND
        idc.initial_running_div = '2' AND
        idc.trans_to_service_org_code = wwoaic.org_id
) target