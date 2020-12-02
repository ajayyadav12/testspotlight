UPDATE T_PROCESS
SET SUPPORT_TEAM_EMAIL = ''
WHERE SUPPORT_TEAM_EMAIL is null;

ALTER TABLE T_PROCESS
MODIFY SUPPORT_TEAM_EMAIL default '';

UPDATE T_notification_def
SET additional_emails = ''
WHERE additional_emails is null;

ALTER TABLE T_notification_def
MODIFY additional_emails default '';

exit;
