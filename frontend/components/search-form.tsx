import Form from "next/form";
import { Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export function SearchForm({ defaultValue, autoFocus }: { defaultValue?: string; autoFocus?: boolean }) {
  return (
    <Form action="/" role="search" className="relative">
      <Search
        aria-hidden
        className="pointer-events-none absolute top-1/2 left-4 size-5 -translate-y-1/2 text-muted-foreground md:left-5"
      />
      <Input
        name="q"
        type="search"
        defaultValue={defaultValue}
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
