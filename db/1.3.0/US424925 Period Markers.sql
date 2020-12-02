ALTER TABLE T_SUBMISSION
ADD (
    period_year INTEGER DEFAULT 0,
    period_quarter INTEGER DEFAULT 0,    
    period_month INTEGER DEFAULT 0 );