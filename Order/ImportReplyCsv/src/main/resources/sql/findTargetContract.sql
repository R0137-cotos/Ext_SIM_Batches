select
	*
from
	contract c
where
	c.lifecycle_status = '2'
AND
	c.workflow_status = '3'
{{#contractNumberList}}
	AND c.immutable_cont_ident_number IN ({{&contractNumberList}})
{{/contractNumberList}}
order by
	c.id