select
	*
from
	contract c
where
	c.lifecycle_status = '2'
AND
	c.workflow_status = '3'
{{#contractNumberList}}
	AND c.contract_number || LPAD(c.contract_branch_number, 2, '0') IN ({{&contractNumberList}})
{{/contractNumberList}}
order by
	c.id