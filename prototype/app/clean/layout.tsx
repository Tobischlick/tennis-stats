import type { Metadata } from "next";
import { Manrope } from "next/font/google";
import Link from "next/link";
import "./clean.css";

const manrope = Manrope({
  variable: "--font-clean",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Tennis Stats · Clean",
};

export default function CleanLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className={`clean-theme ${manrope.variable} flex min-h-screen flex-1 flex-col`}>
      <header className="mx-auto flex w-full max-w-4xl items-center justify-between px-5 py-6 md:px-8 md:py-8">
        <Link href="/clean" className="flex items-center gap-2 text-lg font-semibold tracking-tight">
          <span aria-hidden className="size-2.5 rounded-full bg-primary" />
          Tennis Stats
        </Link>
        <Link
          href="/"
          className="text-xs text-muted-foreground transition-colors hover:text-foreground"
        >
          Alle Prototypen
        </Link>
      </header>
      <main className="mx-auto w-full max-w-4xl flex-1 px-5 pb-20 md:px-8">{children}</main>
    </div>
  );
}
