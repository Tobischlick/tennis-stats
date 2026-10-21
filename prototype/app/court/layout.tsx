import type { Metadata } from "next";
import Link from "next/link";
import { Barlow, Barlow_Condensed } from "next/font/google";
import { ArrowLeft } from "lucide-react";
import { TennisBall } from "@/components/court/tennis-ball";
import "./court.css";

const display = Barlow_Condensed({
  variable: "--font-court-display",
  subsets: ["latin"],
  weight: ["600", "700", "800"],
  style: ["normal", "italic"],
});

const body = Barlow({
  variable: "--font-court-body",
  subsets: ["latin"],
  weight: ["400", "500", "600", "700"],
});

export const metadata: Metadata = {
  title: "Tennis Stats · Court",
};

export default function CourtLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className={`court-theme ${display.variable} ${body.variable} flex min-h-full flex-1 flex-col`}>
      <header className="court-grass-texture text-white">
        <div className="mx-auto flex w-full max-w-5xl items-center justify-between gap-4 px-4 py-3">
          <Link href="/court" className="group flex items-center gap-2">
            <TennisBall className="size-6 transition-transform group-hover:rotate-45" />
            <span className="court-display text-2xl font-extrabold">Tennis Stats</span>
          </Link>
          <Link
            href="/"
            className="flex items-center gap-1 text-xs font-medium text-white/70 hover:text-white"
          >
            <ArrowLeft className="size-3.5" />
            Alle Prototypen
          </Link>
        </div>
        <div className="h-1 bg-(--court-ball)" />
      </header>
      <main className="flex-1">{children}</main>
      <footer className="mx-auto w-full max-w-5xl px-4 py-8 text-xs text-(--court-muted)">
        Daten aus den Ergebnissen des Badischen Tennisverbands.
      </footer>
    </div>
  );
}
