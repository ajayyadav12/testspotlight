ALTER TABLE t_sender
ADD APP_OWNER_ID number;

ALTER TABLE t_sender
ADD FOREIGN KEY (APP_OWNER_ID) REFERENCES T_USER (id);

ALTER TABLE t_process
ADD APP_OWNER_ID number;

ALTER TABLE t_process
ADD FOREIGN KEY (APP_OWNER_ID) REFERENCES T_USER (id);

ALTER TABLE t_submission
ADD acknowledgement_flag CHAR DEFAULT 0;

ALTER TABLE t_submission
ADD acknowledgement_note varchar(255);

exit;

