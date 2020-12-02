--------------------------------------------------------
--  DDL for Table T_SUBMISSION_NOTIFICATION
--------------------------------------------------------

  CREATE TABLE "T_SUBMISSION_NOTIFICATION" ("ID" NUMBER, "SUBMISSION_ID" NUMBER) ;
--------------------------------------------------------
--  DDL for Index T_SUBMISSION_NOTIFICATION_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "T_SUBMISSION_NOTIFICATION_PK" ON "T_SUBMISSION_NOTIFICATION" ("ID") ;
--------------------------------------------------------
--  Constraints for Table T_SUBMISSION_NOTIFICATION
--------------------------------------------------------

  ALTER TABLE "T_SUBMISSION_NOTIFICATION" ADD CONSTRAINT "T_SUBMISSION_NOTIFICATION_PK" PRIMARY KEY ("ID") USING INDEX  ENABLE;
  ALTER TABLE "T_SUBMISSION_NOTIFICATION" MODIFY ("ID" NOT NULL ENABLE);

  --------------------------------------------------------
--  DDL for Table T_SUBMISSION_STEP_NOTIFICATION
--------------------------------------------------------

  CREATE TABLE "T_SUBMISSION_STEP_NOTIFICATION" ("ID" NUMBER, "SUBMISSION_ID" NUMBER, "PROCESS_STEP_ID" NUMBER) ;
--------------------------------------------------------
--  DDL for Index T_SUBMISSION_STEP_NOTIFICA_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "T_SUBMISSION_STEP_NOTIFICA_PK" ON "T_SUBMISSION_STEP_NOTIFICATION" ("ID") ;
--------------------------------------------------------
--  Constraints for Table T_SUBMISSION_STEP_NOTIFICATION
--------------------------------------------------------

  ALTER TABLE "T_SUBMISSION_STEP_NOTIFICATION" ADD CONSTRAINT "T_SUBMISSION_STEP_NOTIFICA_PK" PRIMARY KEY ("ID") USING INDEX  ENABLE;
  ALTER TABLE "T_SUBMISSION_STEP_NOTIFICATION" MODIFY ("ID" NOT NULL ENABLE);

exit;