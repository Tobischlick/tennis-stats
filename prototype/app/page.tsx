import Link from "next/link";

const directions = [
  { slug: "clean", name: "Clean", brief: "Minimal, hell und luftig, eine Akzentfarbe, große Zahlen." },
  { slug: "court", name: "Court", brief: "Sportlich und kräftig, Sand- und Rasenfarben, große Hero-Stats." },
  { slug: "scoreboard", name: "Scoreboard", brief: "Dunkel, kompakt, datenorientiert wie eine Anzeigetafel." },
];

const scenarios = [
  { path: "", label: "Suche" },
  { path: "/player/a1b2c3", label: "Spielerin mit Daten (Anna Schmidt)" },
  { path: "/player/g7h8i9", label: "Noch keine Matches (Lena Hoffmann)" },
  { path: "/player/p7q8r9", label: "Sehr wenige Matches (Sophie Wagner)" },
  { path: "/player/a1b2c3/vs/d4e5f6", label: "Head-to-Head mit Historie" },
  { path: "/player/a1b2c3/vs/j1k2l3", label: "Head-to-Head, nie gegeneinander gespielt" },
];

export default function PrototypeIndex() {
  return (
    <main className="mx-auto w-full max-w-3xl px-4 py-12">
      <h1 className="text-2xl font-semibold">Tennis Stats Prototypen</h1>
      <p className="mt-2 text-sm text-muted-foreground">
        Wegwerf-Prototypen mit Mock-Daten. Drei Designrichtungen, jeweils dieselben Screens.
      </p>
      <div className="mt-8 grid gap-6 md:grid-cols-3">
        {directions.map((d) => (
          <section key={d.slug} className="rounded-lg border p-4">
            <h2 className="font-medium">
              <Link href={`/${d.slug}`} className="underline-offset-4 hover:underline">
                {d.name}
              </Link>
            </h2>
            <p className="mt-1 text-sm text-muted-foreground">{d.brief}</p>
            <ul className="mt-4 space-y-1 text-sm">
              {scenarios.map((s) => (
                <li key={s.path}>
                  <Link href={`/${d.slug}${s.path}`} className="text-primary underline-offset-4 hover:underline">
                    {s.label}
                  </Link>
                </li>
              ))}
            </ul>
          </section>
        ))}
      </div>
    </main>
  );
}
