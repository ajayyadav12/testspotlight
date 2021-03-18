create table T_FILE_SUBMISSION (
	id integer not null primary key,
	name varchar(255),
	comments varchar(255),
	submitted_by integer,
	submission_id integer,
	secret varchar(255) not null,
	file_name varchar(255),
	file_content clob
);

alter table T_FILE_SUBMISSION add foreign key (submmited_by) references T_USER (id);
alter table T_FILE_SUBMISSION add foreign key (submission_id) references T_SUBMISSION (id);

create sequence S_FILE_SUBMISSION_ID MINVALUE 1 MAXVALUE 9999999999 START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

insert into T_NOTIFICATION_TEMPLATE (id, name, subject, body) values (1103, 'ADMIN: file submission failed', 'WARNING: Manual upload for file for process ${PROCESS_NAME}', '<p><strong>Manual upload for file</strong> step does not exist for process <strong>${PROCESS_NAME}</strong>.</p><p><br></p><p><br></p><h2>Spotlight</h2><p><em>Shining light on the financial close process</em></p><p><a href="https://spotlight-dev.corporate.ge.com/" rel="noopener noreferrer" target="_blank">https://spotlight-dev.corporate.ge.com/</a></p>')

exit;