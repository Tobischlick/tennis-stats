"use client";

import { useEffect, useRef, useState, useTransition } from "react";
import Form from "next/form";
import { useRouter } from "next/navigation";
import { Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { MIN_QUERY_LENGTH, SEARCH_DEBOUNCE_MS, searchHref } from "@/lib/search";

export function SearchForm({ query, autoFocus }: { query: string; autoFocus?: boolean }) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();
  const [value, setValue] = useState(query);
  // Queries this form sent since the last outside navigation. One of them arriving
  // late must not overwrite newer typing; anything else (back button, header link)
  // replaces the typed value.
  const [sent, setSent] = useState(() => new Set([query]));
  const [prevQuery, setPrevQuery] = useState(query);
  const timer = useRef<ReturnType<typeof setTimeout>>(undefined);

  if (query !== prevQuery) {
    setPrevQuery(query);
    if (!sent.has(query)) {
      setSent(new Set([query]));
      setValue(query);
    }
  }

  useEffect(() => () => clearTimeout(timer.current), []);

  function handleChange(next: string) {
    setValue(next);
    clearTimeout(timer.current);
    timer.current = setTimeout(() => {
      const trimmed = next.trim();
      const q = trimmed.length >= MIN_QUERY_LENGTH ? trimmed : "";
      setSent((prev) => new Set(prev).add(q));
      startTransition(() => router.replace(searchHref(q), { scroll: false }));
    }, SEARCH_DEBOUNCE_MS);
  }

  return (
    <Form
      action="/"
      role="search"
      className="relative"
      data-pending={isPending ? "" : undefined}
      onSubmit={() => {
        clearTimeout(timer.current);
        setSent(new Set([value.trim()]));
      }}
    >
      <Search
        aria-hidden
        className="pointer-events-none absolute top-1/2 left-4 size-5 -translate-y-1/2 text-muted-foreground md:left-5"
      />
      <Input
        name="q"
        type="search"
        value={value}
        onChange={(e) => handleChange(e.target.value)}
        autoFocus={autoFocus}
        autoComplete="off"
        required
        placeholder="Name eingeben"
        aria-label="Spieler suchen"
        className="h-14 rounded-2xl bg-card pr-26 pl-12 text-base shadow-sm shadow-black/[0.03] focus-visible:border-primary focus-visible:ring-4 focus-visible:ring-primary/15 md:pl-13 md:text-base"
      />
      <Button type="submit" className="absolute top-1/2 right-2 h-10 -translate-y-1/2 rounded-xl px-4 hover:bg-primary/90">
        Suchen
      </Button>
    </Form>
  );
}
