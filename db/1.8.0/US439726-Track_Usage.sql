CREATE TABLE T_AUDIT_LOG
(
	ID INTEGER NOT NULL PRIMARY KEY,
	USER_ID INTEGER,
	LOG_IN TIMESTAMP,
  LOG_OUT TIMESTAMP,
	MODULE VARCHAR(255)
);

CREATE SEQUENCE S_AUDIT_LOG_ID MINVALUE 1 MAXVALUE 9999999999 START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
ALTER TABLE T_AUDIT_LOG ADD FOREIGN KEY (USER_ID) REFERENCES T_USER (id);

exit;