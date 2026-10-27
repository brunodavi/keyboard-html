# Deployment / release pipeline

Standardized so anyone (or any agent) can be told "ship it" and follow the same steps. The only "deploy" surface for an Android app here is: **tag → CI build → GitHub Release with APK + checksums**.

## Release procedure (standard order)

1. On `dev`, bump `APP_VERSION` (and `APP_VERSION_CODE`) in `gradle.properties`.
2. Add the release section to `CHANGELOG.md` (`## [X.Y.Z] - YYYY-MM-DD` format — the workflow extracts exactly this section for the release notes).
3. Commit (`chore: release vX.Y.Z`), push, then tag:

   ```bash
   git tag -a vX.Y.Z -m "vX.Y.Z" && git push origin dev --tags
   ```

4. The tag triggers `release.yml` automatically; do not build releases by hand.

## What the pipeline enforces

- `on: push: tags: ['v*.*.*']` (+ `workflow_dispatch` for manual runs).
- Tests run again before packaging.
- SDK and Gradle caches (keyed by `gradle.properties` hash / wrapper) so repeat runs are fast.
- APKs are renamed with version + platform, hashed (`sha256sum`), and published against the Changelog section:

  ```bash
  VERSION="${GITHUB_REF_NAME#v}"
  awk -v ver="$VERSION" '
    BEGIN { hdr = "## [" ver "]" }
    index($0, hdr) == 1 { flag=1; next }
    flag && index($0, "## [") == 1 { exit }
    flag { print }
  ' CHANGELOG.md > changelog_section.md
  ```

  `changelog_section.md` becomes the release description, stating what changed and which checksum matches which artifact.

## Android packaging cardápio (easy → hard)

Currently shipped: universal APK (debug-signed, directly installable) + `.sha256`. Future options, in the order we'd add them:

- APK (universal) ✔ current
- APK per ABI
- ABB (only needed for Play Store)
- Termux .deb packaging (highest effort, revisit only on demand)

Release APK signing (production keystore) is intentionally not set up yet; when needed, add a `config/`-style gitignored keystore + `keystore.properties` pattern (private never committed, example file committed) rather than secrets in CI env.
