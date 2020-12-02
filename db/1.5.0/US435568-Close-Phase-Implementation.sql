/*
Owner: Diego Flores - 212695020
Description: Add Close phase field in Process Module.
*/

CREATE TABLE t_close_phase(
    id integer not null primary key,
    name varchar(255)
);

INSERT INTO t_close_phase VALUES(1, 'Subledger');
INSERT INTO t_close_phase VALUES(2, 'ERP / GL');
INSERT INTO t_close_phase VALUES(3, 'Business Consolidation');
INSERT INTO t_close_phase VALUES(4, 'Total Company Consolidation');
INSERT INTO t_close_phase VALUES(5, 'Reporting');

ALTER TABLE t_sender
ADD close_phase_id integer default 1 not null;

ALTER TABLE t_receiver
ADD close_phase_id integer default 1 not null;

ALTER TABLE T_SENDER ADD FOREIGN KEY (close_phase_id) REFERENCES t_close_phase (id);
ALTER TABLE T_RECEIVER ADD FOREIGN KEY (close_phase_id) REFERENCES t_close_phase (id);

exit;