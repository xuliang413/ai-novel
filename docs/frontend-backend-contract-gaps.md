# Frontend and Backend Contract Status

Date: 2026-05-16

Scope: frontend work only. Backend code is owned by another agent, so this file records contract alignment and remaining integration risks found while implementing `saai-web`.

## Resolved and Integrated

1. WebSocket streaming contract
   - Backend now exposes `ws://host/ws/novel/write?token=xxx`.
   - Client actions observed in `NovelWriteWebSocketHandler`: `start`, `cancel`, `recover`, `ping`.
   - Server events observed in `NovelWebSocketEventVO`: `token`, `contentReady`, `canceled`, `failed`, `error`, `heartbeat`, plus forward-compatible constants.
   - Frontend writing workbench now has a "stream writing" entry and handles token appending, completion, cancel, and errors.

2. GraphPatch conflict resolution
   - `NovelPatchConfirmForm` now supports `conflictResolutions` keyed by `operationId`, with values `SKIP`, `FORCE`, and `REVIEW`.
   - Frontend GraphPatch table now allows per-operation conflict resolution and submits the selected strategy.

3. Chapter title review
   - `NovelContentReviewPassForm` now accepts `title`.
   - Frontend submits reviewed `title`, `summary`, and `content` in one pass.

4. Project status update
   - `NovelProjectUpdateForm` now accepts `status` for `ACTIVE` and `PAUSED`.
   - Frontend project editor now allows editing status except `ARCHIVED`, which still uses `/archive`.

5. Chapter detail DTO
   - `/novel/chapter/detail` now returns `NovelChapterDetailVO`.
   - Frontend can consume the stable detail contract.

6. Graph panel data
   - Backend graph panel endpoints are available:
     - `/novel/graph/character-relation`
     - `/novel/graph/clue-advancement`
     - `/novel/graph/location-character`
     - `/novel/graph/item-flow`
   - Frontend consumes these through `src/views/business/novel/novel-graph-panel.vue`.

7. Provider enum naming
   - `generation-provider` dict now documents and returns persisted value `TONGYI` instead of the `QWEN` alias.
   - Frontend submits `TONGYI` for Tongyi Qianwen.

8. Writing calendar DTOs
   - Calendar endpoints now return `CalendarEntryVO`, `CheckinResultVO`, and `StreakInfoVO`.
   - Frontend consumes the stable keys in `src/views/business/novel/novel-writing-calendar.vue`.

9. API Key usage
   - Backend now exposes `/novel/user-api-key/usage`.
   - Frontend model key page now shows today/week/month calls, successes, failures, tokens, and provider distribution.

## Open or Needs Verification

1. Menu route component paths
   - Frontend files exist for:
     - `src/views/business/novel/novel-write.vue`
     - `src/views/business/novel/novel-project.vue`
     - `src/views/business/novel/novel-chapter.vue`
     - `src/views/business/novel/novel-character.vue`
     - `src/views/business/novel/novel-location.vue`
     - `src/views/business/novel/novel-clue.vue`
     - `src/views/business/novel/novel-volume.vue`
     - `src/views/business/novel/novel-item.vue`
     - `src/views/business/novel/novel-event.vue`
     - `src/views/business/novel/novel-cheat.vue`
     - `src/views/business/novel/novel-alias.vue`
     - `src/views/business/novel/novel-rule.vue`
     - `src/views/business/novel/novel-graph-health.vue`
     - `src/views/business/novel/novel-graph-panel.vue`
     - `src/views/business/novel/novel-dashboard.vue`
     - `src/views/business/novel/novel-api-key.vue`
     - `src/views/business/novel/novel-writing-calendar.vue`
   - Repository file `saai-be/db/novel_menu.sql` still appears to point many menu rows at `/business/novel/novel-write.vue`.
   - If runtime menu seed has been fixed elsewhere, verify in the app. If this SQL is still authoritative, update each row to the matching component path.

2. Interactive context review stage
   - `NovelWebSocketEventVO` defines `contextReady` and `contextConfirmed`, but current `NovelWriteWebSocketHandler` only emits token/completion/error-style events in the observed `startGeneration` flow.
   - Frontend currently supports context preview after draft generation through `NovelWriteDraftVO.contextPreview`.
   - If M1 requires user approval before LLM generation, backend still needs a concrete action/event flow such as `contextReady` -> client `confirmContext` -> `generationStarted`.

3. GraphPatch history page
   - Dashboard exposes `/novel/dashboard/recent-patches`, but only as recent project activity.
   - A dedicated history page still needs a query endpoint with filters/pagination for `graph_change_log`, including operation batch ID, status, chapter, patch summary, inverse patch summary, error, and timestamps.

4. Dashboard log DTOs
   - `/novel/dashboard/chapter-progress` and `/pending-sessions` now use VOs.
   - `/novel/dashboard/recent-logs` and `/recent-patches` still return persistence entities directly.
   - Frontend consumes current fields, but dedicated VOs would avoid exposing persistence fields and keep masking/formatting stable.
