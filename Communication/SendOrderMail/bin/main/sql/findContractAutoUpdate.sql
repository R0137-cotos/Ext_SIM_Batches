SELECT DISTINCT
    T_EXT_CONTRACT_MAIN.id as id,
    T_EXT_CONTRACT_MAIN.lifecycle_status as lifecycle_status,
    T_EXT_CUSTOMER_CONTRACT.company_name as company_name,
    T_CONTRACT_ELC.contract_ymd_end as contract_ymd_end,
    T_EXT_CONTRACT_SUB.id as child_id,
    T_EXT_CONTRACT_SUB.origin_contract_id as origin_contract_id
FROM {{extSchema}}.CONTRACT                                                                         T_EXT_CONTRACT_MAIN
         INNER JOIN {{extSchema}}.CUSTOMER_CONTRACT                                                 T_EXT_CUSTOMER_CONTRACT
                 ON T_EXT_CONTRACT_MAIN.id = T_EXT_CUSTOMER_CONTRACT.contract_id
         INNER JOIN CONTRACT_ELECTRIC                                                               T_CONTRACT_ELC
                 ON T_EXT_CONTRACT_MAIN.id = T_CONTRACT_ELC.contract_id
    LEFT OUTER JOIN (SELECT
                         SUBQUERY.id as id, 
                         SUBQUERY.origin_contract_id as origin_contract_id
                     FROM {{extSchema}}.CONTRACT                                                    SUBQUERY
                     WHERE
                         SUBQUERY.lifecycle_status = :yoteibimachi
                         OR SUBQUERY.lifecycle_status = :teiketsumachi
                    )                                                                               T_EXT_CONTRACT_SUB
                 ON T_EXT_CONTRACT_MAIN.id = T_EXT_CONTRACT_SUB.origin_contract_id
WHERE
    T_EXT_CONTRACT_MAIN.lifecycle_status = :teiketsuchu
    AND T_CONTRACT_ELC.contract_ymd_end < TO_DATE(:processDate, 'YYYY/MM/DD')
    AND T_EXT_CONTRACT_SUB.id is null
ORDER BY
    T_EXT_CONTRACT_MAIN.id