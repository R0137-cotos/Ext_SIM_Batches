SELECT
  rownum AS id
  , c.id AS contract_id_temp
  , c.contract_number AS contract_number
  , c.contract_branch_number AS contract_branch_number
  , ic.ricoh_item_code AS ricoh_item_code
  , ic.item_contract_name AS item_contract_name
  , pc.extends_parameter_iterance  AS extends_parameter_iterance
  , c.cancel_application_date AS cancel_application_date
  , c.cancel_scheduled_date AS cancel_scheduled_date
  , c.application_date AS application_date
  , c.conclusion_preferred_date AS conclusion_preferred_date
  , c.lifecycle_status AS lifecycle_status
  , c.contract_type AS contract_type
FROM
  contract c 
  INNER JOIN product_contract pc 
    ON c.id = pc.contract_id 
  INNER JOIN contract_detail cd 
    ON c.id = cd.contract_id 
  INNER JOIN item_contract ic 
    ON cd.id = ic.contract_detail_id
  INNER JOIN product_master pm 
    ON pc.product_master_id = pm.id 
WHERE
  pm.product_class_div = 'SIM'
  AND ic.cost_type != '1' 
  AND ( 
    c.lifecycle_status = '8'
    AND c.workflow_status = '3'
  ) 
  OR ( 
    c.lifecycle_status = '2'
    AND c.workflow_status = '3'
    AND c.contract_type = '2'
  ) 
ORDER BY
  c.id
  , cd.id