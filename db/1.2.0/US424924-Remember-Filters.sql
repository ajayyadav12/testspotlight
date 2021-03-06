CREATE TABLE T_MODULE_FILTER 
(
	id INTEGER NOT NULL PRIMARY KEY,
	user_id INTEGER,
	name VARCHAR(255),
	module_name VARCHAR(255),
    settings VARCHAR(255),
	global CHAR DEFAULT 'N'
);

ALTER TABLE T_MODULE_FILTER
ADD FOREIGN KEY (USER_ID) REFERENCES T_USER (id);

alter table T_MODULE_FILTER ADD constraint user_view_name_unique unique("USER_ID","NAME","MODULE_NAME");

CREATE SEQUENCE S_MODULE_FILTER_ID MINVALUE 1 MAXVALUE 9999999999 START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

exit;