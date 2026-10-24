# Backend

## Tests

```sh
./gradlew test
```

Running the tests needs Docker: the database tests run against a
PostgreSQL 16 container started by [Testcontainers](https://testcontainers.com),
with the Flyway migrations building the schema. The local database from
`postgres/start.sh` isn't used and doesn't need to be running.
