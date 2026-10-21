import type { Metadata } from "next";
import Link from "next/link";
import { Barlow_Condensed, Inter, JetBrains_Mono } from "next/font/google";
import { ArrowLeft } from "lucide-react";
import "./scoreboard.css";

const display = Barlow_Condensed({
  variable: "--font-sb-display",
  subsets: ["latin"],
  weight: ["500", "600", "700", "800"],
});

const sans = Inter({
  variable: "--font-sb-sans",
  subsets: ["latin"],
});

const mono = JetBrains_Mono({
  variable: "--font-sb-mono",
  subsets: ["latin"],
  weight: ["500", "700", "800"],
});

export const metadata: Metadata = {
  title: "Tennis Stats · Scoreboard",
};

export default function ScoreboardLayout({ children }: { children: React.ReactNode }) {
  return (
    <div
      className={`sb-root ${display.variable} ${sans.variable} ${mono.variable} flex min-h-dvh flex-1 flex-col`}
    >
      <header className="sticky top-0 z-10 border-b border-(--sb-line) bg-(--sb-bg)/90 backdrop-blur">
        <div className="mx-auto flex h-12 w-full max-w-5xl items-center justify-between px-4">
          <Link href="/scoreboard" className="flex items-center gap-2">
            <span className="sb-led size-2 rounded-full bg-(--sb-win) text-(--sb-win)" aria-hidden />
            <span className="sb-display text-xl font-extrabold tracking-wider">Tennis Stats</span>
          </Link>
          <Link
            href="/"
            className="sb-label flex items-center gap-1 text-[11px] text-(--sb-muted) hover:text-(--sb-text)"
          >
            <ArrowLeft className="size-3" aria-hidden />
            Prototypen
          </Link>
        </div>
      </header>
      <main className="mx-auto w-full max-w-5xl flex-1 px-4 pt-5 pb-16 md:pt-8">{children}</main>
    </div>
  );
}
