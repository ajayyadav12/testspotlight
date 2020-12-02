@echo off
set env=%2
if not defined env set env=local
set user=spotlight
set pass=spotlight
set host=localhost:1521/xe
set v=1
if "%2" == "dev" (
	set user=CO_SPOTLIGHT
	set pass=%3
	set host=agecedd3-scan.corporate.ge.com:1525/pdbcoexad.cloud.ge.com
)
if [%1] == [] (
	echo use up or down
	Exit /b
)
if "%1" == "up" (
	sqlplus -s -l %user%/%pass%@%host% @V%v%__initial_setup_up.sql
	Exit /b
)
if "%1" == "down" (
	sqlplus -s -l %user%/%pass%@%host% @V%v%__initial_setup_down.sql
	Exit /b
)
if "%1" == "seed" (
	sqlplus -s -l %user%/%pass%@%host% @V%v%__%env%_seed.sql
	Exit /b
)
echo %1 not recognized