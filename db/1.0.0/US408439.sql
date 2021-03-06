CREATE TABLE T_PARENT_SUBMISSION 
(
	id INTEGER NOT NULL PRIMARY KEY,
	process_id INTEGER,
	start_time TIMESTAMP,
	end_time TIMESTAMP,
	update_time TIMESTAMP,
	system_start_time TIMESTAMP,
	system_end_time TIMESTAMP,	
	system_update_time TIMESTAMP,
    ad_hoc_flag CHAR DEFAULT 'N',
	status_id INTEGER
);

CREATE SEQUENCE S_PARENT_SUBMISSION_ID MINVALUE 1 MAXVALUE 9999999999 START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

ALTER TABLE T_SUBMISSION ADD status_id INTEGER;
ALTER TABLE T_SUBMISSION ADD FOREIGN KEY (status_id) REFERENCES T_STATUS (id);

ALTER TABLE T_SUBMISSION ADD submission_parent_id INTEGER;
ALTER TABLE T_SUBMISSION ADD FOREIGN KEY (submission_parent_id) REFERENCES T_PARENT_SUBMISSION (id);

ALTER TABLE T_PROCESS ADD PREDECESSOR INTEGER;
ALTER TABLE T_PROCESS ADD SUCCESSOR INTEGER;
ALTER TABLE T_PROCESS ADD FOREIGN KEY (PREDECESSOR) REFERENCES T_PROCESS (id);
ALTER TABLE T_PROCESS ADD FOREIGN KEY (SUCCESSOR) REFERENCES T_PROCESS (id);

exit;