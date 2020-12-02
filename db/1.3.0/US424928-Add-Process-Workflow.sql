/*
0 = Waiting for approval
N = Neglected
A = Approved
*/
ALTER TABLE T_PROCESS
ADD APPROVED_FLAG CHAR DEFAULT '0';

exit;