package icu.windea.pls.config.util.generators

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import icu.windea.pls.config.util.generators.CwtConfigGenerator.Hint
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.children
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.toFile
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.cwt.psi.CwtElementFactory
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 从 `modifiers.log` 生成 `modifier_categories.cwt`。
 *
 * @property ignoredNames 需要忽略的修正分类的名字（忽略大小写）。
 */
class CwtModifierCategoriesConfigGenerator(
    override val gameType: ParadoxGameType,
    override val inputPath: String,
    override val outputPath: String,
) : CwtConfigGenerator {
    val ignoredNames = caseInsensitiveStringSet()

    init {
        configureDefaults()
    }

    private fun configureDefaults() {
        // nothing
    }

    override fun getDefaultGeneratedFileName() = "modifier_categories.cwt"

    override suspend fun generate(project: Project): Hint {
        // 1) 解析日志：聚合所有出现过的类别
        val categoriesFromLog = parseLogFile()

        // 2) 解析现有 CWT 配置（PSI）：已存在的类别
        val categoriesInConfig = parseConfigFile(project)

        // 3) 差异识别（不删除未知类别，仅提示）
        val oldNames = categoriesInConfig.filter { it !in ignoredNames }.toSet()
        val newNames = categoriesFromLog.filter { it !in ignoredNames }.toSet()
        val missingNames = newNames - oldNames
        val unknownNames = oldNames - newNames

        // 4) 读取原文件文本，并在容器末尾插入缺失类别的空块（空行 + 注释 + 条目）
        val file = outputPath.toFile()
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        var modifiedText = readAction { psiFile.text } // 不做删除，仅做插入
        if (missingNames.isNotEmpty()) {
            val insertBlock = buildString {
                appendLine(TODO_MISSING_MODIFIER_CATEGORIES)
                for (name in missingNames.sorted()) {
                    val key = name.quoteIfNecessary()
                    appendLine("${key} = {")
                    appendLine("${INDENT}# TODO choose supported scopes")
                    appendLine("${INDENT}supported_scopes = {}")
                    appendLine("}")
                }
            }.trimEnd()
            modifiedText = insertIntoContainer(project, modifiedText, CONTAINER_MODIFIER_CATEGORIES, insertBlock)
        }

        // 5) 汇总
        val summary = buildString {
            if (missingNames.isNotEmpty()) appendLine("${missingNames.size} missing modifier categories.")
            if (unknownNames.isNotEmpty()) appendLine("${unknownNames.size} unknown modifier categories.")
            if (isEmpty()) appendLine("No missing or unknown modifier categories.")
        }.trimEnd()
        val details = buildString {
            if (missingNames.isNotEmpty()) {
                appendLine("Missing modifier categories:")
                missingNames.sorted().forEach { appendLine("- $it") }
            }
            if (unknownNames.isNotEmpty()) {
                appendLine("Unknown modifier categories:")
                unknownNames.sorted().forEach { appendLine("- $it") }
            }
        }.trimEnd()

        val hint = Hint(summary, details, modifiedText.trimEnd())
        hint.putUserData(Keys.missingNames, missingNames)
        hint.putUserData(Keys.unknownNames, unknownNames)
        hint.putUserData(Keys.categoriesFromLog, categoriesFromLog)
        return hint
    }

    private suspend fun parseLogFile(): Set<String> {
        val file = inputPath.toFile()
        val regex = when (gameType) {
            ParadoxGameType.Stellaris -> """- (.*),\s*Category:\s*(.*)""".toRegex()
            else -> """Tag:(.*),\s*Categories:\s*(.*)""".toRegex()
        }
        val lines = withContext(Dispatchers.IO) { file.readLines() }
        val categories = linkedSetOf<String>()
        for (line in lines) {
            val m = regex.matchEntire(line) ?: continue
            m.groupValues[2].split(',').mapNotNullTo(categories) { it.trim().takeIf { s -> s.isNotEmpty() } }
        }
        return categories
    }

    private suspend fun parseConfigFile(project: Project): Set<String> {
        val file = outputPath.toFile()
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        return readAction {
            val rootProps = psiFile.block?.children()?.filterIsInstance<CwtProperty>()?.toList().orEmpty()
            val container = rootProps.find { it.name == CONTAINER_MODIFIER_CATEGORIES }
            container?.propertyValue?.children()?.filterIsInstance<CwtProperty>()
                ?.mapTo(mutableSetOf()) { it.name.unquote() } ?: emptySet()
        }
    }

    private suspend fun insertIntoContainer(
        project: Project,
        fileText: String,
        containerPropertyName: String,
        insertBlock: String,
    ): String {
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, fileText) }
        val container = readAction {
            val rootProps = psiFile.block?.children()?.filterIsInstance<CwtProperty>()?.toList().orEmpty()
            rootProps.firstOrNull { it.name == containerPropertyName }
        }
        if (container != null) {
            val insertionOffset = readAction {
                val containerText = container.text
                val relIndex = containerText.lastIndexOf('}')
                val rel = if (relIndex == -1) containerText.length else relIndex
                container.textRange.startOffset + rel
            }
            return buildString(fileText.length + insertBlock.length + 64) {
                appendLine(fileText.substring(0, insertionOffset))
                appendLine(insertBlock.prependIndent(INDENT))
                append(fileText.substring(insertionOffset))
            }
        }
        val containerPatch = buildString {
            appendLine("${containerPropertyName} = {")
            appendLine(insertBlock.prependIndent(INDENT))
            append("}")
        }
        return buildString(fileText.length + containerPatch.length + 64) {
            append(fileText.trimEnd())
            appendLine()
            append(containerPatch)
        }
    }

    object Keys : KeyRegistry() {
        val missingNames by createKey<Set<String>>(Keys)
        val unknownNames by createKey<Set<String>>(Keys)
        val categoriesFromLog by createKey<Set<String>>(Keys)
    }

    private companion object {
        private const val CONTAINER_MODIFIER_CATEGORIES = "modifier_categories"
        private const val INDENT = "    "
        private const val TODO_MISSING_MODIFIER_CATEGORIES = "# TODO missing modifier categories"
    }
}
