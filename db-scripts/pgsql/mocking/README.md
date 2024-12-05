# PGSQL Database Setup
To set up mock tables for testing, run each of the sql files in this directory. This can be done with the following script:
```bash
./setup-mocks.sh
```

## Environment Variables
The following environment variables are used by the scripts in this directory:
* **PGSQL_DB_HOST** - The hostname of the PostgreSQL server.
* **PGSQL_DB_PORT** - The port number of the PostgreSQL server.
* **PGSQL_DB_NAME** - The name of the PostgreSQL database.
* **PGSQL_DB_USERNAME** - The username to use when connecting to the PostgreSQL database.
* **PGSQL_DB_PASSWORD** - The password to use when connecting to the PostgreSQL database.

See [sample.env](#sample.env) for an example of how to set these environment variables.

## sample.env
A sample.env file is provided in this directory. Rename this file to .env and update the values to match your environment.