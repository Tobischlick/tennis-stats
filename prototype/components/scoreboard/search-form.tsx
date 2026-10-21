import Form from "next/form";
import { Search } from "lucide-react";

export function SearchForm({
  action,
  name,
  defaultValue,
  placeholder,
  buttonLabel = "Suchen",
  autoFocus,
}: {
  action: string;
  name: string;
  defaultValue?: string;
  placeholder: string;
  buttonLabel?: string;
  autoFocus?: boolean;
}) {
  return (
    <Form action={action} scroll={false} className="flex w-full gap-2" role="search">
      <label className="relative flex-1">
        <span className="sr-only">{placeholder}</span>
        <Search
          className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-(--sb-muted)"
          aria-hidden
        />
        <input
          type="search"
          name={name}
          defaultValue={defaultValue}
          placeholder={placeholder}
          autoFocus={autoFocus}
          autoComplete="off"
          className="h-11 w-full rounded-md border border-(--sb-line) bg-(--sb-panel-2) pr-3 pl-9 text-base text-(--sb-text) placeholder:text-(--sb-muted) focus:border-(--sb-amber) focus:outline-none"
        />
      </label>
      <button
        type="submit"
        className="sb-label h-11 shrink-0 rounded-md bg-(--sb-amber) px-4 text-sm text-(--sb-bg) hover:brightness-110"
      >
        {buttonLabel}
      </button>
    </Form>
  );
}
