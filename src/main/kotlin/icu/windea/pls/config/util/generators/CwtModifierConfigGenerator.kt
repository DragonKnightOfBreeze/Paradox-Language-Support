package icu.windea.pls.config.util.generators

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.util.generators.CwtConfigGenerator.Hint
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.children
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.removeSuffixOrNull
import icu.windea.pls.core.toCommaDelimitedStringSet
import icu.windea.pls.core.toFile
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.cwt.psi.CwtElementFactory
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.isIdentifier
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
 *
 * @see CwtModifierConfig
 * @see CwtModifierCategoryConfig
 */
class CwtModifierConfigGenerator(override val project: Project) : CwtConfigGenerator {
    val ignoredNames = caseInsensitiveStringSet()
    val ignoredCategories = caseInsensitiveStringSet()

    init {
        configureDefaults()
    }

    private fun configureDefaults() {
        // nothing
    }

    override fun getName() = "ModifierConfigGenerator"

    override fun getDefaultInputName() = "modifiers.log"

    override fun getDefaultOutputName() = "modifiers.cwt"

    override suspend fun generate(gameType: ParadoxGameType, inputPath: String, outputPath: String): Hint {
        // 解析日志：modifier -> categories
        val infos = parseLogFile(inputPath, gameType)
        // 解析现有 CWT 配置（PSI）：静态键集合 + 模板正则
        val configInfo = parseConfigFile(outputPath)
        // 过滤日志（忽略名单/分类）并计算差异（缺失用模板匹配，未知仅针对静态名）
        return generateHint(outputPath, infos, configInfo, gameType)
    }

    private suspend fun parseLogFile(inputPath: String, gameType: ParadoxGameType): Map<String, ModifierInfo> {
        val file = inputPath.toFile()
        val lines = withContext(Dispatchers.IO) { file.readLines() }
        val startMarker = "Printing Modifier Definitions:"
        val startIndex = lines.indexOfFirst { it == startMarker }
        val definitionLines = if (startIndex == -1) lines else lines.drop(startIndex + 1)
        val infos = when (gameType) {
            ParadoxGameType.Ck3 -> {
                // Tag: world_innovation_camels_development_growth_factor
                // Use areas: character, province, and county
                val chunks = CwtConfigGeneratorUtil.splitChunks(definitionLines) { it.isEmpty() }
                chunks.mapNotNull f@{ chunkLines ->
                    val name = CwtConfigGeneratorUtil.parseValue(chunkLines, "Tag:")
                        ?.takeIf { it.isNotEmpty() && it.isIdentifier() }?.lowercase() ?: return@f null
                    val categories = CwtConfigGeneratorUtil.parseValue(chunkLines, "Use areas:")
                        ?.toCommaDelimitedStringSet().orEmpty()
                    ModifierInfo(name, categories)
                }
            }
            ParadoxGameType.Vic3 -> {
                // Tag: building_wat_arun_throughput_add, Categories: building
                val regex = """Tag:(\w+),\s*Categories:\s*(.*)""".toRegex()
                definitionLines.mapNotNull f@{ line ->
                    val m = regex.matchEntire(line) ?: return@f null
                    val name = m.groupValues[1].lowercase()
                    val categories = m.groupValues[2].toCommaDelimitedStringSet()
                    ModifierInfo(name, categories)
                }
            }
            else -> {
                // - ship_orbit_upkeep_mult, Category: Military Ships, Civilian Ships
                val regex = """-\s+(\w+),\s*Category:\s*(.*)""".toRegex()
                definitionLines.mapNotNull f@{ line ->
                    val m = regex.matchEntire(line) ?: return@f null
                    val name = m.groupValues[1].lowercase()
                    val categories = m.groupValues[2].toCommaDelimitedStringSet()
                    ModifierInfo(name, categories)
                }
            }
        }
        return infos.associateBy { it.name }
    }

    private suspend fun parseConfigFile(outputPath: String): ModifierConfigInfo {
        val file = outputPath.toFile()
        if (!file.exists()) return ModifierConfigInfo() // file not exist -> return empty
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        val names = caseInsensitiveStringSet()
        val templates = mutableSetOf<CwtTemplateExpression>()
        readAction {
            val rootProps = psiFile.block?.children()?.filterIsInstance<CwtProperty>()?.toList().orEmpty()
            val container = rootProps.find { it.name == CONTAINER_MODIFIERS }
            container?.propertyValue?.children()?.filterIsInstance<CwtProperty>()?.forEach { p ->
                val name = p.name.lowercase()
                val templateExpression = CwtTemplateExpression.resolve(name)
                when {
                    templateExpression.expressionString.isEmpty() -> names += name
                    else -> templates += templateExpression
                }
            }
        }
        // put xxx_<xxx>_xxx before xxx_<xxx>
        // see icu.windea.pls.ep.configGroup.CwtComputedConfigGroupDataProvider.process
        val sortedTemplates = templates
            .sortedByDescending { it.snippetExpressions.size }
            .toSet()
        return ModifierConfigInfo(names, sortedTemplates)
    }

    private suspend fun generateHint(outputPath: String, infos: Map<String, ModifierInfo>, configInfo: ModifierConfigInfo, gameType: ParadoxGameType): Hint {
        val filteredInfos = infos
            .filterValues { info -> info.name !in ignoredNames }
            .filterValues { info -> info.categories.none { it in ignoredCategories } }
            .filterValues { info -> !isForceIgnoredModifier(info, gameType) }
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

        // 删除未知项并生成文本（不删除通过模板匹配的项）
        val file = outputPath.toFile()
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        val elementsToDelete = readAction { CwtConfigGeneratorUtil.getElementsToDelete(psiFile, CONTAINER_MODIFIERS) { toDelete(it, unknownNames) } }
        var modifiedText = CwtConfigGeneratorUtil.getFileText(psiFile, elementsToDelete)

        // 在容器末尾插入缺失项（空行 + 注释 + 条目）
        val insertBlock = buildString {
            appendLine(NOTE_ECONOMIC_MODIFIERS)
            appendLine()
            if (unknownNames.isNotEmpty()) {
                appendLine(NOTE_UNKNOWN_PREDEFINED_MODIFIERS)
                appendLine()
            }
            if (missingNames.isNotEmpty()) {
                appendLine(TODO_MISSING_MODIFIERS)
                for (name in missingNames.sorted()) {
                    val categories = filteredInfos[name]?.categories.orEmpty().sorted()
                    val valueText = when {
                        categories.isEmpty() -> "{}"
                        else -> categories.joinToString(" ", "{ ", " }") { it.quoteIfNecessary() }
                    }
                    appendLine("${name} = ${valueText}")
                }
            }
        }.trimEnd()
        if (insertBlock.isNotEmpty()) {
            val psiFile = readAction { CwtElementFactory.createDummyFile(project, modifiedText) }
            modifiedText = CwtConfigGeneratorUtil.insertIntoContainer(psiFile, CONTAINER_MODIFIERS, insertBlock)
        }

        // 汇总
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
        val fileText = modifiedText.trimEnd() + "\n" // ensure ends with a line break

        val hint = Hint(summary, details, fileText)
        hint.putUserData(Keys.missingNames, missingNames)
        hint.putUserData(Keys.unknownNames, unknownNames)
        hint.putUserData(Keys.unmatchedTemplates, unmatchedTemplates)
        hint.putUserData(Keys.infos, infos)
        hint.putUserData(Keys.configInfo, configInfo)
        return hint
    }

    private fun toDelete(member: CwtMember, unknownNames: Set<String>): Boolean {
        return member is CwtProperty && member.name.lowercase() in unknownNames
    }

    data class ModifierInfo(
        val name: String,
        val categories: Set<String>,
    )

    data class ModifierConfigInfo(
        val names: Set<String> = emptySet(),
        val templates: Set<CwtTemplateExpression> = emptySet(),
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

        private const val NOTE_ECONOMIC_MODIFIERS = "# NOTE possible economic modifiers are ignored"
        private const val NOTE_UNKNOWN_PREDEFINED_MODIFIERS = "# NOTE unknown predefined modifiers are deleted"
        private const val TODO_MISSING_MODIFIERS = "# TODO missing modifiers (key is the modifier name, value is the modifier categories)"

        private fun isForceIgnoredModifier(info: ModifierInfo, gameType: ParadoxGameType): Boolean {
            return when (gameType) {
                ParadoxGameType.Stellaris -> isPossibleEconomicModifier(info)
                else -> false
            }
        }

        private val economicModifierCategories = setOf("produces", "cost", "upkeep", "logistics")
        private val economicModifierTypes = setOf("mult", "add")

        private fun isPossibleEconomicModifier(info: ModifierInfo): Boolean {
            // NOTE 这里并不能访问运行时数据，也就是游戏文件，因为规则生成器应当随时可用
            if ("AI Economy" !in info.categories) return false
            if (info.categories.size < 2) return false
            var s = info.name
            s = economicModifierTypes.firstNotNullOfOrNull { s.removeSuffixOrNull("_$it") } ?: return false
            s = economicModifierCategories.firstNotNullOfOrNull { s.removeSuffixOrNull("_$it") } ?: return false
            if (s.isEmpty()) return false
            return true
        }
    }
}
