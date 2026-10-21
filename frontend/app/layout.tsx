import type { Metadata } from "next";
import { Manrope } from "next/font/google";
import Link from "next/link";
import "./globals.css";

const manrope = Manrope({
  variable: "--font-manrope",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Tennis Stats",
  description:
    "Bilanz, Tie-Breaks und aktuelle Form von jedem Spieler im Badischen Tennisverband.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="de" className={`${manrope.variable} h-full antialiased`}>
      <body className="flex min-h-full flex-col">
        <header className="mx-auto w-full max-w-4xl px-5 py-6 md:px-8 md:py-8">
          <Link href="/" className="inline-flex items-center gap-2 text-lg font-semibold tracking-tight">
            <span aria-hidden className="size-2.5 rounded-full bg-primary" />
            Tennis Stats
          </Link>
        </header>
        <main className="mx-auto w-full max-w-4xl flex-1 px-5 pb-20 md:px-8">{children}</main>
      </body>
    </html>
  );
}
