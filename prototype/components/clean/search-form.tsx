import Form from "next/form";
import { Search } from "lucide-react";

export function SearchForm({ defaultValue, autoFocus }: { defaultValue?: string; autoFocus?: boolean }) {
  return (
    <Form action="/clean" className="relative">
      <Search
        aria-hidden
        className="pointer-events-none absolute top-1/2 left-5 size-5 -translate-y-1/2 text-muted-foreground"
      />
      <input
        name="q"
        type="search"
        defaultValue={defaultValue}
        autoFocus={autoFocus}
        autoComplete="off"
        placeholder="Name eingeben, z. B. Anna Schmidt"
        aria-label="Spieler suchen"
        className="h-14 w-full rounded-2xl border border-input bg-card pr-28 pl-13 text-base shadow-sm shadow-black/[0.03] transition outline-none placeholder:text-muted-foreground focus-visible:border-primary focus-visible:ring-4 focus-visible:ring-primary/15"
      />
      <button
        type="submit"
        className="absolute top-1/2 right-2 h-10 -translate-y-1/2 rounded-xl bg-primary px-4 text-sm font-medium text-primary-foreground transition hover:bg-primary/90"
      >
        Suchen
      </button>
    </Form>
  );
}
