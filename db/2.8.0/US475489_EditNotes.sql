ALTER TABLE T_SCHEDULED_SUBMISSION
ADD edit_note varchar(255);

ALTER TABLE T_SCHEDULED_SUBMISSION
ADD acknowledged_by varchar(255);

ALTER TABLE T_SUBMISSION
ADD acknowledged_by varchar(255);


exit