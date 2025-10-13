package icu.windea.pls.config.util.generators

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.siblings
import icu.windea.pls.config.util.generators.CwtConfigGenerator.Hint
import icu.windea.pls.core.children
import icu.windea.pls.core.collections.filterIsInstance
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.toFile
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.cwt.psi.CwtElementFactory
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 从 `modifiers.log` 生成 `modifiers.cwt`。
 */
class CwtModifierConfigGenerator(
    override val gameType: ParadoxGameType,
    override val inputPath: String,
    override val outputPath: String,
) : CwtConfigGenerator {
    override fun getDefaultGeneratedFileName() = "modifiers.cwt"

    override suspend fun generate(project: Project): Hint {
        // 1) 解析日志：modifier -> categories
        val modifierCategoriesFromLog = parseLogFile()

        // 2) 解析现有 CWT 配置（PSI）：已存在的 modifier 键集合
        val modifierNamesInConfig = parseConfigFile(project)

        // 3) 差异
        val missingNames = modifierCategoriesFromLog.keys - modifierNamesInConfig
        val unknownNames = modifierNamesInConfig - modifierCategoriesFromLog.keys

        // 4) 删除未知项并生成文本
        val file = outputPath.toFile()
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        val elementsToDelete = readAction { getElementsToDelete(psiFile, CONTAINER_MODIFIERS, unknownNames) }
        var modifiedText = CwtConfigGeneratorUtil.getFileText(psiFile, elementsToDelete)

        // 5) 在容器末尾插入缺失项（空行 + TODO 注释 + 条目）
        if (missingNames.isNotEmpty()) {
            val insertBlock = buildString {
                appendLine()
                appendLine(TODO_MISSING_MODIFIERS)
                for (name in missingNames.sorted()) {
                    val categories = modifierCategoriesFromLog[name].orEmpty().sorted()
                    val valueText = when {
                        categories.isEmpty() -> "{}"
                        categories.size == 1 -> categories.first().quoteIfNecessary()
                        else -> categories.joinToString(" ", prefix = "{ ", postfix = " }") { it.quoteIfNecessary() }
                    }
                    appendLine("${name} = ${valueText}")
                }
            }.trimEnd()
            modifiedText = insertIntoContainer(project, modifiedText, CONTAINER_MODIFIERS, insertBlock)
        }

        // 6) 汇总
        val summary = buildString {
            if (missingNames.isNotEmpty()) appendLine("${missingNames.size} missing modifiers.")
            if (unknownNames.isNotEmpty()) appendLine("${unknownNames.size} unknown modifiers.")
            if (isEmpty()) appendLine("No missing or unknown modifiers.")
        }.trimEnd()
        val details = buildString {
            if (missingNames.isNotEmpty()) {
                appendLine("Missing modifiers:")
                missingNames.sorted().forEach { appendLine("- $it") }
            }
            if (unknownNames.isNotEmpty()) {
                appendLine("Unknown modifiers:")
                unknownNames.sorted().forEach { appendLine("- $it") }
            }
        }.trimEnd()

        val hint = Hint(summary, details, modifiedText.trimEnd())
        hint.putUserData(Keys.missingModifierNames, missingNames)
        hint.putUserData(Keys.unknownModifierNames, unknownNames)
        hint.putUserData(Keys.modifierCategoriesFromLog, modifierCategoriesFromLog)
        return hint
    }

    private suspend fun parseLogFile(): Map<String, Set<String>> {
        val file = inputPath.toFile()
        val regex = when (gameType) {
            ParadoxGameType.Stellaris -> """- (.*),\s*Category:\s*(.*)""".toRegex()
            else -> """Tag:(.*),\s*Categories:\s*(.*)""".toRegex()
        }
        val lines = withContext(Dispatchers.IO) { file.readLines() }
        val result = linkedMapOf<String, Set<String>>()
        for (line in lines) {
            val m = regex.matchEntire(line) ?: continue
            val name = m.groupValues[1].trim()
            val categories = m.groupValues[2].split(',')
                .mapNotNullTo(mutableSetOf()) { it.trim().takeIf { s -> s.isNotEmpty() } }
                .toSet()
            result[name] = categories
        }
        return result
    }

    private suspend fun parseConfigFile(project: Project): Set<String> {
        val file = outputPath.toFile()
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        return readAction {
            val rootProps = psiFile.block?.children()?.filterIsInstance<CwtProperty>()?.toList().orEmpty()
            val container = rootProps.find { it.name == CONTAINER_MODIFIERS }
            container?.propertyValue?.children()?.filterIsInstance<CwtProperty>()?.mapTo(mutableSetOf()) { it.name }
                ?: emptySet()
        }
    }

    @Suppress("SameParameterValue")
    private fun getElementsToDelete(
        psiFile: CwtFile,
        containerPropertyName: String,
        namesToDelete: Set<String>
    ): MutableList<PsiElement> {
        val result = mutableListOf<PsiElement>()
        val rootProps = psiFile.block?.children()?.filterIsInstance<CwtProperty>()?.toList().orEmpty()
        val container = rootProps.find { it.name == containerPropertyName } ?: return result
        val propsToDelete = container.propertyValue?.children()?.filterIsInstance<CwtProperty> { it.name in namesToDelete }
        propsToDelete?.forEach { p ->
            result += p
            p.siblings(forward = false, withSelf = false).takeWhile { e -> e !is CwtProperty }.forEach { e -> result += e }
        }
        return result
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
        val missingModifierNames by createKey<Set<String>>(Keys)
        val unknownModifierNames by createKey<Set<String>>(Keys)
        val modifierCategoriesFromLog by createKey<Map<String, Set<String>>>(Keys)
    }

    private companion object {
        private const val CONTAINER_MODIFIERS = "modifiers"
        private const val INDENT = "    "
        private const val TODO_MISSING_MODIFIERS = "# TODO missing modifiers (in actual, key is the modifier name, value is the categories)"
    }
}
