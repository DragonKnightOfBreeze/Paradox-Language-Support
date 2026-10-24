@file:Suppress("unused")

package icu.windea.pls.gradle.markdown

/**
 * CHANGELOG 解析的构建期工具。
 */
object ChangelogSupport {
    /**
     * 从 [fullChangelog] 中提取并处理 [targetVersion] 对应的 h2 段落。
     */
    fun extractSectionForVersion(fullChangelog: String, targetVersion: String): String {
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

        var targetContent: String? = null

        // 1. 宽松匹配标题中的版本
        val expectVersion = targetVersion.substringBefore('-')
        for (section in sections) {
            val vMatch = versionRegex.matchEntire(section.heading)
            if (vMatch != null) {
                val actualVersion = vMatch.groupValues[1].substringBefore('-')
                if (expectVersion == actualVersion) {
                    targetContent = section.content
                    break
                }
            }
        }

        // 2. 回退到非空的 Unreleased 段落
        if (targetContent == null) {
            val unreleased = sections.find { it.heading == "Unreleased" }?.content
            if (!unreleased.isNullOrBlank()) {
                targetContent = unreleased
            }
        }

        // 3. 最后回退到第一个版本段落
        if (targetContent == null) {
            targetContent = sections.firstOrNull { versionRegex.matches(it.heading) }?.content ?: ""
        }

        return processSection(targetContent)
    }

    private fun processSection(content: String): String {
        val lines = content.lines()
        val processed = lines.mapNotNull { line ->
            val normalizedLine = line.trimStart().lowercase()
            when {
                normalizedLine.contains("HIDDEN") -> null
                normalizedLine.startsWith("- [ ] ") -> null
                normalizedLine.startsWith("- [x] ") -> line.replaceFirst("- [x] ", "- ")
                else -> line
            }
        }
        return processed.joinToString("\n").trim()
    }
}
