ALTER TABLE t_submission
ADD ack_failed_email_status CHAR DEFAULT '0';

ALTER TABLE t_scheduled_submission
ADD ack_delayed_email_status CHAR DEFAULT '0';

exit;