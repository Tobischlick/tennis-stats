import type { components } from "@/lib/api/schema";

type Schemas = components["schemas"];

export type PlayerSummary = Schemas["PlayerSummary"];
export type PlayerStats = Schemas["PlayerStats"];
export type HeadToHead = Schemas["HeadToHead"];
export type RecentMatch = Schemas["RecentMatch"];
export type SetScore = Schemas["SetScore"];
export type MatchResult = Schemas["MatchResult"];
export type Overview = Schemas["Overview"];
export type DoublesMeeting = Schemas["DoublesMeeting"];
export type WinLossRecord = Schemas["WinLossRecord"];
export type FirstSetStats = Schemas["FirstSetStats"];
export type PlayerSeason = Schemas["PlayerSeason"];
