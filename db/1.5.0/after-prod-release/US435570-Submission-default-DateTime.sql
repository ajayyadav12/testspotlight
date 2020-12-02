/*
Owner: Diego Flores - 212695020
Description: 
- New column to save payload request from processes in submission_Step
- Removed unused columns related with sytem time from parent, steps and submissions.
*/

ALTER TABLE T_SUBMISSION_STEP
ADD request_payload varchar (4000);

ALTER TABLE T_SUBMISSION_STEP
DROP (system_start_time, system_end_time);

ALTER TABLE T_SUBMISSION
DROP (system_start_time, system_end_time, system_update_time);

ALTER TABLE T_PARENT_SUBMISSION
DROP (system_start_time, system_end_time, system_update_time);

exit;