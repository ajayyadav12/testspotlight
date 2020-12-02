CREATE TABLE t_process_parent_child
(
    id INTEGER NOT NULL PRIMARY KEY,
	process_id INTEGER NOT NULL,
    child_id INTEGER NOT NULL,
	seq INTEGER NOT NULL
);

ALTER TABLE t_process_parent_child ADD CONSTRAINT unique_child_seq UNIQUE (child_id, seq);

ALTER TABLE t_process_parent_child ADD CONSTRAINT fk_process_id FOREIGN KEY (process_id) REFERENCES t_process(id) ON DELETE CASCADE;

ALTER TABLE t_process_parent_child ADD CONSTRAINT fk_child_id FOREIGN KEY (child_id) REFERENCES t_process(id) ON DELETE CASCADE;

CREATE SEQUENCE s_process_parent_child_id MINVALUE 1 MAXVALUE 9999999999 START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

exit;