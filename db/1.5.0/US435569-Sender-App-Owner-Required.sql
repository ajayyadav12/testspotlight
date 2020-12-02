/*
Owner: Diego Flores - 212695020
Description: Set T_SENDER App Owner a provisional value and mark it as mandatory
*/

UPDATE T_SENDER
SET app_owner_id = 1
WHERE app_owner_id is null;

ALTER TABLE T_SENDER
MODIFY app_owner_id not null;

exit;