UPDATE contract_detail detail 
SET detail.extends_parameter = :extendsParam 
WHERE detail.id IN (:idList)