# Agent Instructions

- You should ALWAYS use [Conventional Commits](https://www.conventionalcommits.org/) for all git commits.
- Never hard-code user-visible strings in Kotlin, XML layouts, or Compose UI. Put them in Android string resources and use formatted or plural resources where appropriate.
- In Compose, load `<string>` resources with `stringResource` and `<plurals>` resources with `pluralStringResource`; do not use `stringResource` with a plural resource ID.
- When translating UI labels, account for the available space: keep compact controls such as buttons and navigation labels as short as the target language allows, without sacrificing clarity.
