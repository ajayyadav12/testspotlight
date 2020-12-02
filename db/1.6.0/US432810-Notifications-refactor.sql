/*
Owner: Diego Flores - 212695020
Description: Add Email addreses for notifications
*/

ALTER TABLE T_PROCESS
ADD support_team_email varchar(255);

ALTER TABLE T_NOTIFICATION_DEF
ADD (additional_emails varchar(255));

ALTER TABLE T_NOTIFICATION_DEF
DROP (TEMPLATE_ID, DL_GROUP_ID);

ALTER TABLE T_SCHEDULE_REPORT
DROP (NOTIFICATION_ID, DL_GROUP_ID);

ALTER TABLE T_SCHEDULE_REPORT
ADD (additional_emails varchar(255));

exit;