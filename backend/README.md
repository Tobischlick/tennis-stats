# Backend

## Tests

```sh
./gradlew test
```

Running the tests needs Docker: the database tests run against a
PostgreSQL 16 container started by [Testcontainers](https://testcontainers.com),
with the Flyway migrations building the schema. The local database from
`postgres/start.sh` isn't used and doesn't need to be running.

## Crawl logging

A crawl (`./gradlew bootRun --args='--spring.profiles.active=crawler'`)
logs at INFO: the groups found per championship, one summary line per group,
every failed page or meeting, and the final `Crawl finished` summary.

For more detail, set `CRAWLER_LOG_LEVEL=DEBUG`:

```sh
CRAWLER_LOG_LEVEL=DEBUG ./gradlew bootRun --args='--spring.profiles.active=crawler'
```

At DEBUG you also see every nuLiga request (URL, HTTP status and response
time, without the politeness delay), every skipped meeting and why it was
skipped, every crawled meeting with its number of saved rubbers, and how many
groups, meetings or rubbers each parsed page held.

To keep a crawl's log, write it to `logs/` (ignored by git), e.g. with
Spring Boot's `LOGGING_FILE_NAME`:

```sh
LOGGING_FILE_NAME=logs/crawl.log ./gradlew bootRun --args='--spring.profiles.active=crawler'
```
