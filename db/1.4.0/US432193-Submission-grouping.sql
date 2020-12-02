/*
Owner: Diego Flores - 212695020
Description: New column that will work as a alternate ID. 
Processes will send at start step call, and they will make reference to it to update the submission ID linked to it.
*/

ALTER TABLE t_submission
ADD alt_id varchar (255);

exit;