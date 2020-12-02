/*
    Removed Max char validation using CLOB
*/

ALTER TABLE t_submission_step ADD (temp CLOB);
UPDATE t_submission_step SET temp = request_payload;
ALTER TABLE t_submission_step DROP COLUMN request_payload;
ALTER TABLE t_submission_step RENAME COLUMN temp TO request_payload;

commit;

ALTER TABLE t_submission_step ADD (temp CLOB);
UPDATE t_submission_step SET temp = notes;
ALTER TABLE t_submission_step DROP COLUMN notes;
ALTER TABLE t_submission_step RENAME COLUMN temp TO notes;

commit;

ALTER TABLE t_submission ADD (temp CLOB);
UPDATE t_submission SET temp = notes;
ALTER TABLE t_submission DROP COLUMN notes;
ALTER TABLE t_submission RENAME COLUMN temp TO notes;

exit;