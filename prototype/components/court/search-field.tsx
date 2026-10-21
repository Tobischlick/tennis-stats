import Form from "next/form";
import { Search } from "lucide-react";

export function SearchField({
  action,
  name,
  defaultValue,
  placeholder,
  label,
  scroll,
}: {
  action: string;
  name: string;
  defaultValue?: string;
  placeholder: string;
  label: string;
  scroll?: boolean;
}) {
  return (
    <Form action={action} scroll={scroll} role="search" className="w-full">
      <label className="sr-only" htmlFor={`court-search-${name}`}>
        {label}
      </label>
      <div className="flex h-14 w-full items-center overflow-hidden rounded-full bg-(--court-paper) shadow-[0_6px_0_rgba(0,0,0,0.18)] ring-2 ring-transparent focus-within:ring-(--court-ball)">
        <Search className="ml-5 size-5 shrink-0 text-(--court-muted)" aria-hidden="true" />
        <input
          id={`court-search-${name}`}
          name={name}
          type="search"
          defaultValue={defaultValue}
          placeholder={placeholder}
          autoComplete="off"
          className="h-full min-w-0 flex-1 bg-transparent px-3 text-lg text-(--court-ink) outline-none placeholder:text-(--court-muted)/70"
        />
        <button
          type="submit"
          className="court-display mr-1.5 h-11 shrink-0 rounded-full bg-(--court-ink) px-5 text-lg font-bold text-white hover:bg-(--court-clay-dark)"
        >
          Los
        </button>
      </div>
    </Form>
  );
}
