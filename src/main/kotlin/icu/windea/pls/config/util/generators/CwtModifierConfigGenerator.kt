package icu.windea.pls.config.util.generators

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.siblings
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.util.generators.CwtConfigGenerator.Hint
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.children
import icu.windea.pls.core.collections.filterIsInstance
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.removeSuffixOrNull
import icu.windea.pls.core.toFile
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.cwt.psi.CwtElementFactory
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.util.CwtTemplateExpressionManager
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 从 `modifiers.log` 生成 `modifiers.cwt`。
 *
 * 注意：可能的经济修正（由经济类型生成的修正）会被忽略。
 *
 * @property ignoredNames 需要忽略的修正的名字（忽略大小写）。
 * @property ignoredCategories 需要忽略的修正的分类（忽略大小写）。
 */
class CwtModifierConfigGenerator(
    override val gameType: ParadoxGameType,
    override val inputPath: String,
    override val outputPath: String,
) : CwtConfigGenerator {
    val ignoredNames = caseInsensitiveStringSet()
    val ignoredCategories = caseInsensitiveStringSet()

    init {
        configureDefaults()
    }

    private fun configureDefaults() {
        // nothing
    }

    override fun getDefaultGeneratedFileName() = "modifiers.cwt"

    override suspend fun generate(project: Project): Hint {
        // 1) 解析日志：modifier -> categories
        val infos = parseLogFile()

        // 2) 解析现有 CWT 配置（PSI）：静态键集合 + 模板正则
        val configInfo = parseConfigFile(project)

        // 3) 过滤日志（忽略名单/分类）并计算差异（缺失用模板匹配，未知仅针对静态名）
        val filteredInfos = infos
            .filterValues { info -> info.name !in ignoredNames }
            .filterValues { info -> info.categories.none { it in ignoredCategories } }
            .filterValues { info -> !isPossibleEconomicModifier(info) }
        val missingNames = filteredInfos.keys
            .filter { name -> name !in configInfo.names }
            .filter { name -> configInfo.templates.none { CwtTemplateExpressionManager.toRegex(it).matches(name) } }
            .toSet()
        val unknownNames = configInfo.names
            .filter { name -> name !in filteredInfos.keys }
            .toSet()
        val unmatchedTemplates = configInfo.templates
            .filter { filteredInfos.keys.none { name -> CwtTemplateExpressionManager.toRegex(it).matches(name) } }
            .toSet()

        // 4) 删除未知项并生成文本（不删除通过模板匹配的项）
        val file = outputPath.toFile()
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        val elementsToDelete = readAction { getElementsToDelete(psiFile, CONTAINER_MODIFIERS, unknownNames) }
        var modifiedText = CwtConfigGeneratorUtil.getFileText(psiFile, elementsToDelete)

        // 5) 在容器末尾插入缺失项（空行 + 注释 + 条目）
        if (missingNames.isNotEmpty()) {
            val insertBlock = buildString {
                appendLine(NOTE_UNKNOWN_PREDEFINED_MODIFIERS)
                appendLine(NOTE_ECONOMIC_MODIFIERS)
                appendLine()
                appendLine(TODO_MISSING_MODIFIERS)
                for (name in missingNames.sorted()) {
                    val categories = filteredInfos[name]?.categories.orEmpty().sorted()
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
            appendLine("Note that possible economic modifiers are ignored.")
            if (missingNames.isNotEmpty()) {
                appendLine("Missing modifiers:")
                missingNames.sorted().forEach { appendLine("- $it") }
            }
            if (unknownNames.isNotEmpty()) {
                appendLine("Unknown modifiers:")
                unknownNames.sorted().forEach { appendLine("- $it") }
            }
            if (unmatchedTemplates.isNotEmpty()) {
                appendLine("Unmatched templates:")
                unmatchedTemplates.forEach { appendLine("- $it") }
            }
        }.trimEnd()
        val fileText = modifiedText.trimEnd()

        val hint = Hint(summary, details, fileText)
        hint.putUserData(Keys.missingNames, missingNames)
        hint.putUserData(Keys.unknownNames, unknownNames)
        hint.putUserData(Keys.unmatchedTemplates, unmatchedTemplates)
        hint.putUserData(Keys.infos, infos)
        hint.putUserData(Keys.configInfo, configInfo)
        return hint
    }

    private suspend fun parseLogFile(): Map<String, ModifierInfo> {
        val file = inputPath.toFile()
        val regex = when (gameType) {
            ParadoxGameType.Stellaris -> """- (.*),\s*Category:\s*(.*)""".toRegex()
            else -> """Tag:(.*),\s*Categories:\s*(.*)""".toRegex()
        }
        val lines = withContext(Dispatchers.IO) { file.readLines() }
        val result = mutableMapOf<String, ModifierInfo>()
        for (line in lines) {
            val m = regex.matchEntire(line) ?: continue
            val name = m.groupValues[1].trim().lowercase()
            val categories = m.groupValues[2].split(',')
                .mapNotNullTo(mutableSetOf()) { it.trim().takeIf { s -> s.isNotEmpty() } }
                .toSet()
            result[name] = ModifierInfo(name, categories)
        }
        return result
    }

    private suspend fun parseConfigFile(project: Project): ModifierConfigInfo {
        val file = outputPath.toFile()
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        return readAction {
            val rootProps = psiFile.block?.children()?.filterIsInstance<CwtProperty>()?.toList().orEmpty()
            val container = rootProps.find { it.name == CONTAINER_MODIFIERS }
            val names = caseInsensitiveStringSet()
            val templates = mutableSetOf<CwtTemplateExpression>()
            container?.propertyValue?.children()?.filterIsInstance<CwtProperty>()?.forEach { p ->
                val name = p.name.lowercase()
                val templateExpression = CwtTemplateExpression.resolve(name)
                when {
                    templateExpression.expressionString.isEmpty() -> names += name
                    else -> templates += templateExpression
                }
            }
            // put xxx_<xxx>_xxx before xxx_<xxx>
            // see icu.windea.pls.ep.configGroup.ComputedCwtConfigGroupDataProvider.process
            val sortedTemplates = templates
                .sortedByDescending { it.snippetExpressions.size }
                .toSet()
            ModifierConfigInfo(names, sortedTemplates)
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
        val propsToDelete = container.propertyValue?.children()?.filterIsInstance<CwtProperty> { it.name.lowercase() in namesToDelete }
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

    data class ModifierInfo(
        val name: String,
        val categories: Set<String>,
    )

    data class ModifierConfigInfo(
        val names: Set<String>,
        val templates: Set<CwtTemplateExpression>,
    )

    object Keys : KeyRegistry() {
        val missingNames by createKey<Set<String>>(Keys)
        val unknownNames by createKey<Set<String>>(Keys)
        val unmatchedTemplates by createKey<Set<CwtTemplateExpression>>(Keys)
        val infos by createKey<Map<String, ModifierInfo>>(Keys)
        val configInfo by createKey<ModifierConfigInfo>(Keys)
    }

    private companion object {
        private const val CONTAINER_MODIFIERS = "modifiers"
        private const val INDENT = "    "
        private const val NOTE_UNKNOWN_PREDEFINED_MODIFIERS = "# NOTE unknown predefined modifiers are deleted"
        private const val NOTE_ECONOMIC_MODIFIERS = "# NOTE possible economic modifiers are ignored"
        private const val TODO_MISSING_MODIFIERS = "# TODO missing modifiers (key is the modifier name, value is the modifier categories)"

        // see cwt/cwtools-stellaris-config/config/common/economic_categories.cwt
        private const val CATEGORY_AI_ECONOMY = "AI Economy"
        private val economicModifierCategories = setOf("produces", "cost", "upkeep", "logistics")
        private val economicModifierTypes = setOf("mult", "add")

        private fun isPossibleEconomicModifier(info: ModifierInfo): Boolean {
            // NOTE 这里并不能访问运行时数据，也就是游戏文件，因为规则生成器应当随时可用
            if (CATEGORY_AI_ECONOMY !in info.categories) return false
            if (info.categories.size < 2) return false
            var s = info.name
            s = economicModifierTypes.firstNotNullOfOrNull { s.removeSuffixOrNull("_$it") } ?: return false
            s = economicModifierCategories.firstNotNullOfOrNull { s.removeSuffixOrNull("_$it") } ?: return false
            if (s.isEmpty()) return false
            return true
        }
    }
}
