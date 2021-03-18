-- Delete first Sender and Receiver Foreign keys
CREATE TABLE T_SYSTEM (
    id INTEGER NOT NULL PRIMARY KEY,
	name varchar(255),
    close_phase_id integer default 1 not null,
    app_owner_id integer default 1 not null,
    CONSTRAINT fk_close_phase_id 
        FOREIGN KEY (close_phase_id) 
        REFERENCES t_close_phase (id),
    CONSTRAINT fk_app_owner_id 
        FOREIGN KEY (app_owner_id) 
        REFERENCES t_user(id)
);

CREATE SEQUENCE S_SYSTEM_ID MINVALUE 1 MAXVALUE 9999999999 START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'DXL',6,1);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'OneBiller',3,1);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'SOA',164,1);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'CCL',87,2);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'Fusion Cirrus',87,2);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'RASR',54,4);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'ICS',164,1);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'TC Automic',2,1);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'CoStar',194,1);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'HFM',83,3);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'IBS',180,1);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'COHFM',166,3);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'Altais',166,2);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'GRC',166,2);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'Lighting SAP',166,2);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'Pascal',166,2);
Insert into T_SYSTEM (ID,NAME,APP_OWNER_ID,CLOSE_PHASE_ID) values (s_system_id.nextVal,'Uniform',166,2);

-- VERIFY Receiver, sender and system are correct
SELECT p.name "Process",r.name "receiver", s.name "receiver system", se.name "sender", sy.name "sender system"
from t_process p
JOIN t_receiver r on p.receiver_id = r.id
JOIN t_system s on p.receiver_id = s.id
JOIN t_sender se on p.sender_id = se.id
JOIN t_system sy on p.sender_id = sy.id
ORDER BY p.name;

-- Then run this:

ALTER TABLE T_PROCESS ADD FOREIGN KEY (sender_id) REFERENCES T_SYSTEM (id);
ALTER TABLE T_PROCESS ADD FOREIGN KEY (receiver_id) REFERENCES T_SYSTEM (id);

exit;