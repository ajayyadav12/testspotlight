BEGIN

  --Bye Tables!
  FOR i IN (SELECT ut.table_name
              FROM USER_TABLES ut) LOOP
    EXECUTE IMMEDIATE 'drop table '|| i.table_name ||' CASCADE CONSTRAINTS ';
  END LOOP;

  FOR j IN (SELECT us.SEQUENCE_NAME
              FROM USER_SEQUENCES us) LOOP
    EXECUTE IMMEDIATE 'drop sequence ' || j.sequence_name ;
  END LOOP;

END;