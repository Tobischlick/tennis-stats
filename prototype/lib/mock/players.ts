import type { components } from "@/lib/api/schema";

export type PlayerSummary = components["schemas"]["PlayerSummary"];

export const players: PlayerSummary[] = [
  { id: "a1b2c3", name: "Anna Schmidt", club: "TC Blau-Weiss Karlsruhe" },
  { id: "d4e5f6", name: "Anna Schuster", club: "TC Rot-Gelb Heidelberg" },
  { id: "g7h8i9", name: "Lena Hoffmann", club: "TC Grün-Weiß Mannheim" },
  { id: "j1k2l3", name: "Jonas Weber", club: "TC Freiburg 1897" },
  { id: "m4n5o6", name: "Felix Maier", club: "TC Blau-Weiss Karlsruhe" },
  { id: "p7q8r9", name: "Sophie Wagner", club: "TC Baden-Baden" },
  { id: "s1t2u3", name: "Lukas Fischer", club: "TV Bruchsal" },
  { id: "v4w5x6", name: "Marie Becker", club: null },
];
