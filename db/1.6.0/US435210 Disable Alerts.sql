ALTER TABLE T_PROCESS
ADD (    
    SUBMISSION_ESCALATION_ALRT CHAR(1) DEFAULT 'Y' NOT NULL,
    LONG_RUNNING_SUB_ALRT CHAR(1) DEFAULT 'Y' NOT NULL,
    LONG_RUNNING_STEP_ALRT CHAR(1) DEFAULT 'Y' NOT NULL
    );
-- Comments for Testing Merge