# Releases

OpenDialer is distributed as a signed APK attached to each GitHub Release. Each
release is built from the pushed Git tag, includes a SHA-256 checksum, and has
a GitHub artifact attestation. GitHub-generated release notes are the
project's authoritative changelog.

## Versioning

`appVersionName` in [`gradle.properties`](../gradle.properties) is the single
authoritative semantic version (`MAJOR.MINOR.PATCH`). Android's `versionCode`
is derived as `MAJOR * 1,000,000 + MINOR * 1,000 + PATCH`, providing a
monotonically increasing integer for normal semantic-version releases. Minor
and patch values must each be 999 or less.

Before a release, change only `appVersionName`, commit it to `main`, and let CI
pass. The release tag must exactly match the version with a `v` prefix: for
example, `appVersionName=0.4.0` requires `v0.4.0`.

## One-time GitHub configuration

Create these **Actions secrets** in repository settings. Never commit a
keystore or any of these values.

| Secret | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded release keystore file |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Alias of the signing key |
| `ANDROID_KEY_PASSWORD` | Password for that key |

On macOS or Linux, create the Base64 value without line wrapping:

```bash
base64 < release-keystore.jks | tr -d '\n'
```

Copy the resulting single line into `ANDROID_KEYSTORE_BASE64`. Keep the
original keystore backed up securely: losing it prevents signing updates that
Android accepts as upgrades.

## Make a release

Releasing is a two-step process. First, prepare and push the version-change
commit; this does **not** publish a release:

```bash
scripts/prepare-release.sh 0.4.0
```

This updates `appVersionName`, commits `chore(release): prepare v0.4.0`, and
pushes `main`. Wait for CI on that commit to pass.

Then publish the already-prepared release by creating and pushing its
`v0.4.0` tag. This tag triggers the public release workflow. The prompt
defaults to **No**; pass `--yes` only for deliberate non-interactive use.

```bash
scripts/prepare-release.sh 0.4.0 --publish
```

The script requires a clean, up-to-date `main` branch, validates the semantic
version, and rejects existing tags.

The `Android Release` workflow validates the tag against `appVersionName`,
builds the signed release APK, writes its SHA-256 checksum, attests the assets,
and creates a GitHub Release with generated notes.

Users can verify a downloaded APK with:

```bash
sha256sum -c OpenDialer-v0.4.0.apk.sha256
```

## Troubleshooting

* **Tag/version validation failed:** update `appVersionName` on `main`, merge
  it, then tag the exact matching `vMAJOR.MINOR.PATCH` version.
* **Signing failed:** confirm all four secrets exist, the Base64 value was
  copied as one line, and the alias/password values open the original keystore.
* **A release already exists:** GitHub rejects duplicate tag releases. Correct
  the issue in a new version and push a new tag; do not replace published APKs.

Local builds work without signing secrets. `./gradlew assembleRelease` creates
an unsigned release APK locally; CI uses the separate debug build, tests, and
lint checks.
