UPDATE contract_detail detail 
SET detail.extends_parameter = :extendsParam,
    detail.updated_user_id = 'COTOS_BATCH_USER',
    detail.updated_at = sysdate
WHERE detail.id IN (:idList)