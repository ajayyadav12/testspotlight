UPDATE t_scheduled_submission
SET ack_delayed_email_status = '0'
WHERE ack_delayed_email_status is null;

UPDATE t_submission
SET ack_failed_email_status = '0'
WHERE ack_failed_email_status is null;

exit;