SELECT
  * 
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
  AND (( 
    c.lifecycle_status = '7' 
    AND c.workflow_status = '3'
  ) 
  OR ( 
    c.lifecycle_status = '2' 
    AND c.workflow_status = '3' 
    AND c.contract_type = '2'
  )) 
{{#contractNumberList}}
	AND c.contract_number IN ({{&contractNumberList}})
{{/contractNumberList}}
ORDER BY
  c.id