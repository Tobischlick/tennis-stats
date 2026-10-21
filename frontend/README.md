# Tennis Stats frontend

The Next.js (App Router) web app for Tennis Stats: player search, the stats page
and the head-to-head page. TypeScript, Tailwind CSS and shadcn/ui, styled per
the [UI concept](../docs/arc42/08_crosscutting_concepts/ui_concept.md).

Data comes from the backend API through the typed client in `lib/api-client/`.
It runs only on the Next.js server: pages are Server Components, and the
compare picker searches through a Server Action. The browser never calls the
backend, so the backend needs no CORS setup.

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `TENNIS_STATS_API_BASE_URL` | `http://localhost:8080/api/v1` | Backend API base URL, including `/api/v1`. Server-only; never prefix it with `NEXT_PUBLIC_`. |

See [`.env.example`](.env.example). Put local overrides in `.env.local`.

## Run against a local backend

1. In `backend/`: start Postgres with `postgres/start.sh` the first time (it deletes
   and recreates the container, data included) and with
   `docker start tennis-stats-postgres` afterwards; crawl once with
   `./gradlew bootRun --args='--spring.profiles.active=crawler'`, then start
   the API on port 8080 with `./gradlew bootRun`.
2. Start the frontend:

   ```sh
   npm install && npm run dev
   ```

3. Open http://localhost:3000.

Responses are fetched with `cache: "no-store"`, so new crawl results show up on
the next page load. If the backend is unreachable, the pages show a calm error
state with a retry button instead of the data.

## API types

`lib/api/schema.d.ts` is generated from [`api/openapi.yaml`](../api/openapi.yaml)
and committed. Regenerate it after the spec changes:

```sh
npm run gen:api
```
