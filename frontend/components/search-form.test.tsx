import type { ComponentProps } from "react";
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { act, cleanup, fireEvent, render, screen } from "@testing-library/react";
import { SearchForm } from "./search-form";

const replace = vi.fn();

vi.mock("next/navigation", () => ({ useRouter: () => ({ replace }) }));
vi.mock("next/form", () => ({
  default: ({ action, ...props }: ComponentProps<"form"> & { action: string }) => <form action={action} {...props} />,
}));

beforeEach(() => {
  vi.useFakeTimers();
  replace.mockClear();
});

afterEach(() => {
  cleanup();
  vi.useRealTimers();
});

function input() {
  return screen.getByRole<HTMLInputElement>("searchbox", { name: "Spieler suchen" });
}

function type(value: string) {
  fireEvent.change(input(), { target: { value } });
}

describe("SearchForm", () => {
  test("updates the URL once after a pause in typing", () => {
    render(<SearchForm query="" />);
    type("M");
    act(() => vi.advanceTimersByTime(200));
    type("Ma");
    act(() => vi.advanceTimersByTime(200));
    type("Max");
    act(() => vi.advanceTimersByTime(299));
    expect(replace).not.toHaveBeenCalled();

    act(() => vi.advanceTimersByTime(1));
    expect(replace).toHaveBeenCalledTimes(1);
    expect(replace).toHaveBeenCalledWith("/?q=Max", { scroll: false });
  });

  test("does not search below the minimum length", () => {
    render(<SearchForm query="Max" />);
    type(" M ");
    act(() => vi.advanceTimersByTime(300));
    expect(replace).toHaveBeenCalledWith("/", { scroll: false });
  });

  test("keeps the typed value when an older query arrives late", () => {
    const { rerender } = render(<SearchForm query="" />);
    type("Mu");
    act(() => vi.advanceTimersByTime(300));
    expect(replace).toHaveBeenLastCalledWith("/?q=Mu", { scroll: false });

    type("Mus");
    act(() => vi.advanceTimersByTime(300));
    expect(replace).toHaveBeenLastCalledWith("/?q=Mus", { scroll: false });
    type("Must");

    rerender(<SearchForm query="Mu" />);
    expect(input().value).toBe("Must");
    rerender(<SearchForm query="Mus" />);
    expect(input().value).toBe("Must");
  });

  test("takes over a query from outside, e.g. the back button", () => {
    const { rerender } = render(<SearchForm query="Mustermann" />);
    rerender(<SearchForm query="Musterfrau" />);
    expect(input().value).toBe("Musterfrau");
    rerender(<SearchForm query="" />);
    expect(input().value).toBe("");
  });

  test("keeps focus and cursor position while results update", () => {
    const { rerender } = render(<SearchForm query="" />);
    input().focus();
    type("Muster");
    input().setSelectionRange(3, 3);
    act(() => vi.advanceTimersByTime(300));
    rerender(<SearchForm query="Muster" />);
    expect(document.activeElement).toBe(input());
    expect(input().selectionStart).toBe(3);
  });

  test("submitting cancels the pending debounced update", () => {
    render(<SearchForm query="" />);
    type("Muster");
    fireEvent.submit(input().form!);
    act(() => vi.advanceTimersByTime(300));
    expect(replace).not.toHaveBeenCalled();
  });
});
