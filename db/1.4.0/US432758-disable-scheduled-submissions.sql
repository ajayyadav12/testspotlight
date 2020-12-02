/*
Owner: Diego Flores - 212695020
Description: New columns similar to acknowledge ones. 
Users are now able to disable specific scheduled submissions (disabled_flag = '1') and provide the reason for it.
*/

ALTER TABLE T_SCHEDULED_SUBMISSION
ADD disabled_flag char default '0';

ALTER TABLE T_SCHEDULED_SUBMISSION
ADD disabled_note varchar(255);

exit;