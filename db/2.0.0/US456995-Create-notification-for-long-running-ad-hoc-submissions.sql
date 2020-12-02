ALTER TABLE t_process 
ADD (
    max_run_time_hours number default 0,
    max_run_time_minutes number default 0
);

Insert into T_NOTIFICATION_TEMPLATE (ID,NAME,SUBJECT,BODY) values (995,'Long Running adHoc submission','${PROCESS_NAME} beyond max run time','<p>Submission #<strong>${SUBMISSION_ID} </strong>for process: <strong>${PROCESS_NAME} </strong>has passed the maximum run time duration.</p><p><br></p><h2>Spotlight</h2><p><em>Shining light on the financial close process</em></p><p><a href="https://spotlight.corporate.ge.com/" rel="noopener noreferrer" target="_blank">https://spotlight.corporate.ge.com</a></p>');

exit;