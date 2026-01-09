# Codex Agent Rulebook

## Purpose
This rule file keeps the assistant aligned with the Cursor workflow. Whenever the user issues a new prompt, follow the steps below before producing an answer or touching the codebase.

## Mandatory Reading Sequence
1. `.cursorrules` – refresh global rules and workspace boundaries.
2. `README.md` (project root) – confirm project scope, structure, and quick-start notes.
3. `docs/README.md` – load the documentation index and import workflow.
4. Any file explicitly referenced in the latest user prompt (e.g., `docs/technical/frontend/*`, role guides, expectation docs). Open them before reasoning about the request.

## Prompt Handling Workflow
1. **Identify role** required (Requirements Analyst, Frontend Expert, Backend Expert) and re-read its rule file in `.cursor-rules/` if acting in that capacity.
2. **Confirm workspace boundaries** based on the active role before editing or reading additional files.
3. **Load referenced documentation** via the mandatory reading sequence plus any `@import` chains noted inside those docs.
4. **Summarize understanding** back to the user, citing which files informed the response.

## Additional Enforcement
- Never create or move files without first re-checking `.cursor-rules/structure.md`.
- When uncertain about context, prefer opening documentation over guessing.
- If the user references new materials mid-task, pause and repeat the reading sequence from step 4 above.
