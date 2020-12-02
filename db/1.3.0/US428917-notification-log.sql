ALTER TABLE T_NOTIFICATION_LOG
ADD (
    to_emails VARCHAR(4000),
    notification_template_id INTEGER,    
    scheduled_submission_id INTEGER,
    process_id INTEGER
);

ALTER TABLE T_NOTIFICATION_LOG ADD FOREIGN KEY (notification_template_id) REFERENCES T_NOTIFICATION_TEMPLATE (id);
ALTER TABLE T_NOTIFICATION_LOG ADD FOREIGN KEY (scheduled_submission_id) REFERENCES T_SCHEDULED_SUBMISSION(id);
ALTER TABLE T_NOTIFICATION_LOG ADD FOREIGN KEY (process_id) REFERENCES T_PROCESS (id);

CREATE SEQUENCE S_NOTIFICATION_LOG_ID MINVALUE 1 MAXVALUE 9999999999 START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

exit;