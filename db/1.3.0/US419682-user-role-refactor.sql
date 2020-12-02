ALTER TABLE t_user
MODIFY role_id INTEGER DEFAULT 2 NOT NULL;

ALTER TABLE T_USER ADD FOREIGN KEY (role_id) REFERENCES T_ROLE (id);

UPDATE t_user
SET role_id = (
    SELECT role_id 
    FROM t_user_role 
    WHERE t_user_role.user_id = t_user.id    
);

DROP TABLE t_user_role CASCADE CONSTRAINTS;

exit;