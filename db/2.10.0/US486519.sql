DROP TABLE t_sender;

DROP TABLE t_receiver;

DROP TABLE t_submission_predecessor;

ALTER TABLE t_parent_submission DROP COLUMN update_time;

ALTER TABLE t_submission DROP (UPDATE_TIME,
    SUB_STATUS_ID,
    RCA_DESCRIPTION,
    ENTITIES,
    FORECAST_RECORDS,
    FORECAST_CYCLE_TIME,
    SUSPENSE_LINES,
    CLASS,
    MANUAL_FLAG,
    SUPERSEDED_FLAG,
    REFERENCE1,
    REFERENCE2,
    REFERENCE3,
    COMMENTS);