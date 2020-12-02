### Local

For local environment you can just run `db_setup.bat up` to create the tables in the database with username `spotlight` and password `spotlight`. After the tables are created you can run `db_setup.bat seed` to seed initial data to them. You can also run `.\db_setup.bat` from PowerShell, there is no script yet for an Unix shell.

### Dev, QA, Prod

For dev, qa and prod environments, you should run `db_setup.bat up {env} {password}` providing the correct environment (`dev`. `qa`, or `prod`) and the correct database password. For example `db_setup.bat up dev MySuperSecurePassword1`. Only `dev` environment is supported at this time. To seed initial data you should run `db_setup.bat {env} {password}`. Keep in mind that there is a different seed file per environment.