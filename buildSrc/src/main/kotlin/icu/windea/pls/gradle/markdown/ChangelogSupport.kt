@file:Suppress("unused")

package icu.windea.pls.gradle.markdown

/**
 * CHANGELOG 解析的构建期工具。
 */
object ChangelogSupport {
    /**
     * 从 [fullChangelog] 中提取并处理 [targetVersion] 对应的 h2 段落。
     *
     * @param keepTaskList 为 `true` 时保留 `- [x]` 与 `- [ ]` 任务列表格式（适用于 GitHub Release Notes）；
     * 为 `false` 时剔除未完成项并将 `- [x] ` 转为普通列表项 `- `（适用于插件详情页 HTML）。
     */
    fun extractSectionForVersion(
        fullChangelog: String,
        targetVersion: String,
        keepTaskList: Boolean = false,
    ): String {
        val targetContent = findSectionContent(fullChangelog, targetVersion)
        return processSection(targetContent, keepTaskList)
    }

    /**
     * 构建用于 GitHub Release 的 Markdown 正文。
     *
     * 遵循项目惯例，依次包含 `## Release Notes`（保留任务列表格式）、`## Roadmap` 链接，以及可选的附加说明。
     */
    fun buildGitHubReleaseNotes(
        fullChangelog: String,
        targetVersion: String,
        repositoryUrl: String,
        roadmapRef: String,
        extraNotes: String? = null,
    ): String {
        val releaseNotes = extractSectionForVersion(fullChangelog, targetVersion, keepTaskList = true)
        val normalizedRepoUrl = repositoryUrl.trimEnd('/')
        return buildString {
            appendLine("## Release Notes")
            if (releaseNotes.isNotBlank()) {
                appendLine()
                appendLine(releaseNotes)
            }
            appendLine()
            appendLine("## Roadmap")
            appendLine()
            append("See [roadmap.md]($normalizedRepoUrl/blob/$roadmapRef/documents/notes/roadmap.md)")
            val extra = extraNotes?.trim()
            if (!extra.isNullOrEmpty()) {
                appendLine()
                appendLine()
                append(extra)
            }
        }
    }

    private fun findSectionContent(fullChangelog: String, targetVersion: String): String {
        // 匹配 h2 标题：`## <heading>`
        val headingRegex = """^##\s+(.+)$""".toRegex()
        // 匹配标题中的版本：`3.0.0`、`3.0.0-dev`、`3.0.0-dev-262`、`3.0.0 - 2026-01-01` 等
        val versionRegex = """^(\d+(?:\.\d+)+(?:-[A-Za-z0-9.-]+)?)(?:\s+-\s+.*)?$""".toRegex()

        data class Section(val heading: String, val content: String)

        val sections = mutableListOf<Section>()
        var currentHeading = ""
        var currentContent = StringBuilder()

        for (line in fullChangelog.lines()) {
            val match = headingRegex.matchEntire(line)
            if (match != null) {
                if (currentHeading.isNotEmpty()) {
                    sections.add(Section(currentHeading, currentContent.toString().trim()))
                }
                currentHeading = match.groupValues[1].trim()
                currentContent = StringBuilder()
            } else {
                if (currentContent.isNotEmpty()) currentContent.appendLine()
                currentContent.append(line)
            }
        }
        if (currentHeading.isNotEmpty()) {
            sections.add(Section(currentHeading, currentContent.toString().trim()))
        }

        // 1. 宽松匹配标题中的版本
        val expectVersion = targetVersion.substringBefore('-')
        for (section in sections) {
            val vMatch = versionRegex.matchEntire(section.heading)
            if (vMatch != null) {
                val actualVersion = vMatch.groupValues[1].substringBefore('-').trim()
                if (expectVersion == actualVersion) {
                    return section.content
                }
            }
        }

        // 2. 回退到非空的 Unreleased 段落
        val unreleased = sections.find { it.heading == "Unreleased" }?.content
        if (!unreleased.isNullOrBlank()) {
            return unreleased
        }

        // 3. 最后回退到第一个版本段落
        return sections.firstOrNull { versionRegex.matches(it.heading) }?.content ?: ""
    }

    private fun processSection(content: String, keepTaskList: Boolean): String {
        val lines = content.lines()
        val processed = lines.mapNotNull { line ->
            val trimmed = line.trimStart()
            when {
                trimmed.contains("HIDDEN", ignoreCase = true) -> null
                !keepTaskList && trimmed.startsWith("- [ ] ") -> null
                !keepTaskList && trimmed.startsWith("- [x] ", ignoreCase = true) -> line.replaceFirst(Regex("- \\[[xX]] "), "- ")
                else -> line
            }
        }
        return processed.joinToString("\n").trim()
    }
}
