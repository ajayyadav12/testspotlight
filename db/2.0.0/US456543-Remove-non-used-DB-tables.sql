DROP TABLE t_attachment;
DROP TABLE t_process_alert;
DROP TABLE t_schedule_exception;
ALTER TABLE t_sender DROP column stage_id;
DROP TABLE t_stage;
ALTER TABLE t_submission_step DROP column sub_status_id;
DROP TABLE t_sub_status;
DROP TABLE t_submission_notification;
DROP TABLE t_submission_step_notification;
DROP TABLE t_user_view;
DROP TABLE t_view;
DROP TABLE t_process_user_permission;
DROP TABLE t_permission;

exit;