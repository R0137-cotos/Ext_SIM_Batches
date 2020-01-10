UPDATE contract_detail detail 
SET detail.extends_parameter = :extendsParam 
WHERE detail.contract_id IN (:idList) 
  AND detail.id IN (
    SELECT item.contract_detail_id
    FROM item_contract item
    WHERE item.cost_type != '1'
  )
