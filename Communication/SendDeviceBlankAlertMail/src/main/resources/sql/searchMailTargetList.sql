SELECT ROWNUM as seq_no,
       co.product_grp_master_id as product_grp_master_id,
       cpse.mail_address as mail_address,
       co.id as contract_id
FROM contract co
LEFT OUTER JOIN product_contract pc
             ON co.id = pc.contract_id
LEFT OUTER JOIN product_master pm
             ON pc.product_master_id = pm.id
LEFT OUTER JOIN contract_pic_sa_emp cpse
             ON co.id = cpse.contract_id
WHERE pm.product_class_div = 'SIM'
  AND co.LIFECYCLE_STATUS = 6
  AND co.service_term_start <= :serviceTermStart
  AND JSON_EXISTS(PC.EXTENDS_PARAMETER_ITERANCE, '$.extendsParameterList?(@.device == "")')