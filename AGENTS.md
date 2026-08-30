# Agent Instructions

- You should ALWAYS use [Conventional Commits](https://www.conventionalcommits.org/) for all git commits.
- Never hard-code user-visible strings in Kotlin, XML layouts, or Compose UI. Put them in Android string resources and use formatted or plural resources where appropriate.
- In Compose, load `<string>` resources with `stringResource` and `<plurals>` resources with `pluralStringResource`; do not use `stringResource` with a plural resource ID.
- Do not call `LocalContext.current.getString()` or read `LocalContext.current.resources` from a composable. Use `stringResource`/`pluralStringResource`, or `LocalResources.current` when a resource must be resolved dynamically, so configuration changes invalidate the composition.
- When translating UI labels, account for the available space: keep compact controls such as buttons and navigation labels as short as the target language allows, without sacrificing clarity.

## Code organization

- Keep screen files focused on UI composition and event wiring. Move reusable or non-UI helpers—such as matching, formatting, and data transformation—into appropriately named files.

## Git workflow

- GitHub release changelog generation relies on merge commits. Make every change through a pull request and merge it into `main`; never commit directly to `main`.
- Keep each pull request focused on one topic.
- Do not use the `codex/` prefix when creating branches.
