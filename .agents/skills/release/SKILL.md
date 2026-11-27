---
name: release
description: Prepare, verify, and execute Paradox Chronicle pre-releases and stable releases, including CHANGELOG/roadmap alignment, release notes preview, decoupled GitHub/Marketplace workflows, and post-release hygiene. Use when preparing a release, cutting a hotfix, previewing release notes, or triggering release workflows.
---

# Paradox Chronicle Release Workflow

Follow this SOP when preparing a pre-release, stable release, or hotfix for Paradox Chronicle.

## 1. Pre-Flight Alignment

Verify version metadata and maintainer records before merging or tagging:

- **`gradle.properties`**: verify `pluginVersion` (strip `-dev` for stable releases such as `3.1.0`, or keep `-dev` for pre-releases such as `3.1.0-dev`), `pluginSinceBuild`, `platformVersion`, and related properties.
- **`CHANGELOG.md`**:
  - Use heading `## <version> - YYYY-MM-DD` for a finalized stable release.
  - For pre-releases (e.g., `3.1.0-dev`), `buildSrc/.../ChangelogSupport.kt` strips the `-dev` suffix when matching headings (matching `## 3.1.0`) and falls back to `## Unreleased` if missing, so do not create a separate `## <version>-dev` heading.
  - Mark finalized entries with `[x]` in the bilingual format (`- [x] 中文描述 / English description`) documented in `documents/notes/guidelines.md`. Keep in mind that `ChangelogSupport.kt` drops `- [ ]` items and any line containing `HIDDEN` (case-insensitive) from JetBrains Marketplace `changeNotes`, while preserving `- [x]` and `- [ ]` checkboxes in GitHub Release Notes.
- **Roadmap**: update `documents/notes/roadmap/v<version>.md` (completion checkboxes, end date, status `已完成`) and `documents/notes/roadmap.md` (status, date range, summary).
- **ADRs**: ensure major release decisions (e.g., license or baseline upgrades) are recorded under `documents/notes/adr/` and indexed in `documents/notes/adr.md`.

## 2. Local Preview and CI Verification

Do **not** run full `./gradlew check` or `./gradlew verifyPlugin` locally as a pre-release gate. Full `check` takes tens of minutes, `verifyPlugin` downloads IDE distributions and frequently times out locally, and both are already run by `.github/workflows/build.yml` and `.github/workflows/release-github.yml`.

Locally, run only fast, deterministic checks through the built-in shell with `--no-daemon --console=plain`:

```bash
# Preview generated GitHub Release Notes locally
./gradlew generateReleaseNotes "-PreleaseVersion=<version>" "-ProadmapRef=<tag-or-branch>" "-PoutputFile=build/release-notes.md" --no-daemon --console=plain
```

- On Windows PowerShell, keep `-P...` / `-D...` arguments double-quoted (e.g., `"-PreleaseVersion=3.1.0-dev"`) to prevent splitting dotted version strings, and avoid backslash line continuations.
- Pass `"-PextraNotesFile=<path>"` when appending extra Markdown notes to the generated release body, and verify `build/release-notes.md` renders clean `## Release Notes` and `## Roadmap` sections.
- Run `./gradlew compileKotlin compileTestKotlin --no-daemon --console=plain` or targeted `--tests` runs when verifying last-minute edits, and run `./gradlew buildPlugin --no-daemon --console=plain` only when inspecting ZIP packaging layout (e.g., `LICENSE` or `NOTICE` inclusion).
- Rely on GitHub Actions (`build.yml` on PR/push, and `release-github.yml` with `run_checks=true`) for full `check` and `verifyPlugin`.

## 3. Branching, Merge Order, and Tagging

- **Feature or minor releases**: develop on `dev/<version>-dev`, open a PR into `master`, and merge once CI checks pass.
- **Hotfixes**: branch `dev/<version>` from `master`, land focused commits (code fix, regression test, changelog/roadmap update), open a PR into `master`, and merge.
- **Stable release ordering**: commit and merge `gradle.properties` (without `-dev`), `CHANGELOG.md` (`## <version> - YYYY-MM-DD`), and `roadmap/` updates into `master` **before** dispatching `release-github.yml`. The workflow builds and tags that exact `master` commit, and `release.yml` later checks out that tag to run `publishPlugin`.
- Do not rewrite or force-push `master`.

## 4. Decoupled GitHub and Marketplace Workflows

The repository separates GitHub Release creation from JetBrains Marketplace publishing across two workflows:

1. **Create GitHub Release via `.github/workflows/release-github.yml`** (`workflow_dispatch`):

```bash
# Pre-release (allows safely force-moving an existing pre-release tag)
gh workflow run release-github.yml \
  --ref <branch> \
  -f tag=v<version>-dev \
  -f prerelease=true \
  -f run_checks=true

# Stable release (creates a Draft release on master)
gh workflow run release-github.yml \
  --ref master \
  -f tag=v<version> \
  -f prerelease=false \
  -f run_checks=true
```

- Safety guards built into `release-github.yml`:
  - When `prerelease=true`, the tag is force-moved (`git tag -fa`), `roadmapRef` resolves to `${GITHUB_REF_NAME}`, and the release is published immediately with `draft=false`.
  - When `prerelease=false`, `roadmapRef` resolves to `$TAG`, the release is created as a **Draft** (`draft=true`), and the workflow refuses to overwrite an already published stable release or hijack an existing pre-release tag.
  - Optional `-f extra_notes="..."` appends extra Markdown to the release body.

2. **Publish to JetBrains Marketplace via `.github/workflows/release.yml`**:
  - Inspect the generated **Draft** release, ZIP artifact, and release notes on GitHub.
  - Once verified, the maintainer manually clicks **Publish release** on GitHub.
  - Publishing a non-prerelease, non-draft release automatically triggers `release.yml`, which checks out the release tag, runs `./gradlew publishPlugin`, uploads artifacts if needed, and closes the matching GitHub milestone (`v<version>`).

## 5. Post-Release Hygiene and Next Cycle

Do **not** assume a fixed linear next version number (e.g., `3.1.0` might be followed by `3.1.1`, `3.2.0`, or an open pause).

- **Immediate hygiene**:
  - After a hotfix (e.g., `dev/3.0.7` merged to `master`), merge or fast-forward `master` back into any active `dev/*` branches so fixes, `CHANGELOG.md`, and `roadmap/` stay aligned.
  - Ensure `CHANGELOG.md` keeps an `## Unreleased` section at the top for incoming changes.
- **Optional next-cycle setup** (only after the maintainer confirms the next target version `<next>`):
  1. Cut or fast-forward `dev/<next>-dev` from `master` if not already active.
  2. Bump `pluginVersion` in `gradle.properties` to `<next>-dev`.
  3. Create `documents/notes/roadmap/v<next>.md` and link it in `documents/notes/roadmap.md`.
