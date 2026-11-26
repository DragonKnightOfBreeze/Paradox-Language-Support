---
name: release
description: Prepare, verify, and execute Paradox Chronicle pre-releases and stable releases, including CHANGELOG/roadmap alignment, release notes preview, decoupled GitHub/Marketplace workflows, and post-release rollover. Use when preparing a release, cutting a hotfix, previewing release notes, or triggering release workflows.
---

# Paradox Chronicle Release Workflow

Follow this SOP when preparing a pre-release, stable release, or hotfix for Paradox Chronicle.

## 1. Pre-Flight Alignment

Verify version metadata and maintainer records before merging or tagging:

- **`gradle.properties`**: check `pluginVersion` (e.g., `3.1.0` for stable or `3.1.0-dev` for dev/pre-release), `pluginSinceBuild`, `platformVersion`, and related properties.
- **`CHANGELOG.md`**:
  - Use heading `## <version> - YYYY-MM-DD` for a finalized release (or `## <version>` while iterating on a pre-release).
  - Follow the bilingual entry format (`- [x] 中文描述 / English description`) documented in `documents/notes/guidelines.md`.
  - Keep `buildSrc/.../ChangelogSupport.kt` behavior in mind: `changeNotes` for JetBrains Marketplace omits `- [ ]` items and any line containing `HIDDEN` (case-insensitive), while `generateReleaseNotes` keeps task-list checkboxes (`- [x]` / `- [ ]`) for GitHub Releases.
- **Roadmap**: update `documents/notes/roadmap/v<version>.md` (completion checkboxes, end date, status `已完成`) and `documents/notes/roadmap.md` (status, date range, summary).
- **ADRs**: ensure any major release decisions (e.g., license or baseline upgrades) are recorded under `documents/notes/adr/` and indexed in `documents/notes/adr.md`.

## 2. Local Verification and Release Notes Preview

Run all Gradle commands through the built-in shell with `--no-daemon --console=plain`:

```bash
# Full check, Plugin Verifier, and distribution ZIP build
./gradlew check verifyPlugin buildPlugin --no-daemon --console=plain

# Preview generated GitHub Release Notes locally
./gradlew generateReleaseNotes \
  -PreleaseVersion=<version> \
  -ProadmapRef=<tag-or-branch> \
  -PoutputFile=build/release-notes.md \
  --no-daemon --console=plain
```

- Pass `-PextraNotesFile=<path>` when appending extra Markdown notes to the generated release body.
- Confirm that `build/release-notes.md` renders clean `## Release Notes` and `## Roadmap` sections.

## 3. Branching and Merge Flow

- **Feature/minor releases**: develop on `dev/<version>-dev`, open a PR into `master`, and merge once checks pass.
- **Hotfixes**: branch `dev/<version>` from `master`, land focused commits (code fix, regression test, changelog/roadmap update), open a PR into `master`, and merge.
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
  - When `prerelease=true`, the tag is force-moved (`git tag -fa`) and published immediately with `draft=false`.
  - When `prerelease=false`, the release is created as a **Draft** (`draft=true`), and the workflow refuses to overwrite an already published stable release or hijack an existing pre-release tag.
  - Optional `-f extra_notes="..."` appends extra Markdown to the release body.

2. **Publish to JetBrains Marketplace via `.github/workflows/release.yml`**:
  - Inspect the generated **Draft** release, ZIP artifact, and release notes on GitHub.
  - Once verified, the maintainer manually clicks **Publish release** on GitHub.
  - Publishing a non-prerelease, non-draft release automatically triggers `release.yml`, which runs `./gradlew publishPlugin`, uploads artifacts if needed, and closes the matching GitHub milestone (`v<version>`).

## 5. Post-Release Rollover

After a stable release is published:

1. Cut or fast-forward the next development branch (e.g., `dev/<next>-dev`) from `master`.
2. Bump `pluginVersion` in `gradle.properties` to `<next>-dev`.
3. Add the new version section in `CHANGELOG.md` and create/link `documents/notes/roadmap/v<next>.md` in `documents/notes/roadmap.md`.
