# Tennis Stats frontend prototypes

Throwaway, clickable prototypes: same screens in several visual directions, against mock data, to agree on look, feel and flow before the real frontend is built in `frontend/`. Not production code.

```bash
npm install
npm run dev
```

Open http://localhost:3000 for the index of directions and scenarios.

- `app/<direction>/`: one folder per direction (`clean`, `court`, `scoreboard`), each with search, player stats and head-to-head
- `components/<direction>/`: that direction's own components; `components/ui/` holds the shared shadcn/ui primitives
- `lib/mock/`: mock data and functions named after the `operationId`s in `api/openapi.yaml`
- `lib/api/schema.d.ts`: types generated from the spec with `npm run gen:api`
