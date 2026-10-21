import { TennisBall } from "./tennis-ball";

export function FriendlyEmpty({
  title,
  children,
  action,
}: {
  title: string;
  children: React.ReactNode;
  action?: React.ReactNode;
}) {
  return (
    <div className="relative overflow-hidden rounded-3xl border-2 border-dashed border-(--court-grass)/30 bg-(--court-grass-soft) px-6 py-10 text-center">
      <div className="relative mx-auto mb-5 flex h-20 w-28 items-end justify-center">
        <div className="absolute inset-x-0 bottom-3 h-[3px] bg-white" />
        <TennisBall className="relative mb-4 size-12 animate-bounce [animation-duration:1.6s]" />
      </div>
      <h2 className="court-display text-3xl font-extrabold text-(--court-grass-dark)">{title}</h2>
      <div className="mx-auto mt-3 max-w-md text-base text-(--court-ink)/80">{children}</div>
      {action ? <div className="mt-6 flex flex-wrap justify-center gap-3">{action}</div> : null}
    </div>
  );
}
