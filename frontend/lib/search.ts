export const MIN_QUERY_LENGTH = 2;
export const SEARCH_DEBOUNCE_MS = 300;

export function normalizeQuery(raw: string | string[] | undefined): string {
  return (Array.isArray(raw) ? raw[0] : raw)?.trim() ?? "";
}

export function searchHref(query: string): string {
  const q = query.trim();
  return q ? `/?${new URLSearchParams({ q })}` : "/";
}
