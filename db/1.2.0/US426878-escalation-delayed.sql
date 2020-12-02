ALTER TABLE t_scheduled_submission
ADD acknowledgement_flag CHAR DEFAULT 0;

ALTER TABLE t_scheduled_submission
ADD acknowledgement_note varchar(255);

ALTER TABLE t_scheduled_submission
ADD acknowledgement_date TIMESTAMP;

ALTER TABLE t_submission
ADD acknowledgement_date TIMESTAMP;

exit;