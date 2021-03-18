CREATE TABLE t_notification_def_mobile(
    id INTEGER NOT NULL PRIMARY KEY,
    notification_def_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL
);

ALTER TABLE t_notification_def_mobile ADD FOREIGN KEY (notification_def_id) REFERENCES T_NOTIFICATION_DEF (id) ON DELETE CASCADE;
ALTER TABLE t_notification_def_mobile ADD FOREIGN KEY (user_id) REFERENCES T_USER (id) ON DELETE CASCADE;

CREATE SEQUENCE S_NOTIFICATION_MOBILE_ID MINVALUE 1 MAXVALUE 9999999999 START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- Migration Script

BEGIN
  FOR notification IN (
        SELECT id, created_for
        FROM t_notification_def
        WHERE text_msg = 1
    )
  LOOP
     insert into t_notification_def_mobile values (S_NOTIFICATION_MOBILE_ID.nextVal, notification.id, notification.created_for);
  END LOOP;
END;

ALTER TABLE t_notification_def DROP COLUMN created_for;

exit;