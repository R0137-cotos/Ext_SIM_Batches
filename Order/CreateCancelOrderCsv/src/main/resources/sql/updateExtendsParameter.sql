UPDATE contract_detail detail 
SET detail.extends_parameter = :extendsParam,
    updated_user_id = 'COTOS_BATCH_USER',
    updated_at = sysdate
WHERE detail.id IN (:idList)