ALTER TABLE T_SUBMISSION
ADD (    
    BU CHAR(3 CHAR)
);

CREATE TABLE T_BUSINESS_UNIT 
(
  BU CHAR(3) NOT NULL 
, NAME VARCHAR2(255) NOT NULL 
, CONSTRAINT T_BUSINESS_UNIT_PK PRIMARY KEY 
  (
    BU 
  )
  ENABLE 
);


alter table "SPOTLIGHT"."T_SUBMISSION" add constraint BU foreign key("BU") references "T_BUSINESS_UNIT"("BU");