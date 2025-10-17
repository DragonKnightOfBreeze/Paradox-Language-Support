package icu.windea.pls.config.util.generators

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.delegated.CwtLocalisationCommandConfig
import icu.windea.pls.config.config.delegated.CwtLocalisationPromotionConfig
import icu.windea.pls.config.util.generators.CwtConfigGenerator.Hint
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.toFile
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtElementFactory
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 从 `localizations.log` 生成 `localisation.cwt`。
 *
 * @property ignoredPromotionNames 需要忽略的提升的名字（忽略大小写）。
 * @property ignoredCommandNames 需要忽略的命令的名字（忽略大小写）。
 *
 * @see CwtLocalisationPromotionConfig
 * @see CwtLocalisationCommandConfig
 */
class CwtLocalisationConfigGenerator(override val project: Project) : CwtConfigGenerator {
    val ignoredPromotionNames = caseInsensitiveStringSet()
    val ignoredCommandNames = caseInsensitiveStringSet()

    init {
        configureDefaults()
    }

    private fun configureDefaults() {
        ignoredPromotionNames += setOf("This", "Root", "Prev", "From")
    }

    override fun getName() = "LocalisationConfigGenerator"

    override fun getDefaultInputName() = "localizations.log"

    override fun getDefaultOutputName() = "localisation.cwt"

    override suspend fun generate(gameType: ParadoxGameType, inputPath: String, outputPath: String): Hint {
        // 解析日志文件，汇总 promotions/commands -> scopes
        val infos = parseLogFile(inputPath)
        // 解析现有 CWT 配置（PSI）以获取已存在的键集合
        val configInfo = parseConfigFile(outputPath)
        // 计算差异
        return generateHint(outputPath, infos, configInfo)
    }

    private suspend fun parseLogFile(inputPath: String): List<LocalisationInfo> {
        val file = inputPath.toFile()
        val lines = withContext(Dispatchers.IO) { file.readLines() }
        val infos = mutableListOf<LocalisationInfo>()
        var position = Position.ScopeName
        var name = ""
        val promotions = mutableSetOf<String>()
        val properties = mutableSetOf<String>()
        for (raw in lines) {
            val line = raw.trim()
            if (line.startsWith("--") && line.endsWith("--")) {
                if (name.isNotEmpty()) {
                    infos += LocalisationInfo(name, promotions.toSet(), properties.toSet())
                    promotions.clear()
                    properties.clear()
                }
                name = line.removePrefix("--").removeSuffix("--").trim()
                position = Position.ScopeName
                continue
            }
            if (line == "Promotions:") {
                position = Position.Promotions
                continue
            }
            if (line == "Properties") {
                position = Position.Properties
                continue
            }
            when (position) {
                Position.Promotions -> {
                    val v = line.takeIf { it.isNotEmpty() && '=' !in it }
                    if (v != null) promotions += v
                }
                Position.Properties -> {
                    val v = line.takeIf { it.isNotEmpty() && '=' !in it }
                    if (v != null) properties += v
                }
                else -> {}
            }
        }
        if (name.isNotEmpty()) {
            infos += LocalisationInfo(name, promotions.toSet(), properties.toSet())
        }
        return infos
    }

    private suspend fun parseConfigFile(outputPath: String): LocalisationConfigInfo {
        val file = java.io.File(outputPath)
        if (!file.exists()) return LocalisationConfigInfo() // file not exist -> return empty
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        return readAction {
            val rootBlock = psiFile.block
            val rootProps = rootBlock?.children()?.filterIsInstance<CwtProperty>()?.toList().orEmpty()
            val promotionsProp = rootProps.find { it.name == CONTAINER_PROMOTIONS }
            val commandsProp = rootProps.find { it.name == CONTAINER_COMMANDS }

            val promotionNames = promotionsProp?.propertyValue?.castOrNull<CwtBlock>()?.children()
                ?.filterIsInstance<CwtProperty>()
                ?.mapTo(mutableSetOf()) { it.name }
                .orEmpty()
            val commandNames = commandsProp?.propertyValue?.castOrNull<CwtBlock>()?.children()
                ?.filterIsInstance<CwtProperty>()
                ?.mapTo(mutableSetOf()) { it.name }
                .orEmpty()
            LocalisationConfigInfo(promotionNames, commandNames)
        }
    }

    private suspend fun generateHint(outputPath: String, infos: List<LocalisationInfo>, configInfo: LocalisationConfigInfo): Hint {
        val (promotionScopesFromLog, commandScopesFromLog) = aggregateScopes(infos)
        val oldPromotions = configInfo.promotions.filter { it !in ignoredPromotionNames }.toSet()
        val newPromotions = promotionScopesFromLog.keys.filter { it !in ignoredPromotionNames }.toSet()
        val oldCommands = configInfo.commands.filter { it !in ignoredCommandNames }.toSet()
        val newCommands = commandScopesFromLog.keys.filter { it !in ignoredCommandNames }.toSet()
        val missingPromotions = newPromotions - oldPromotions
        val unknownPromotions = oldPromotions - newPromotions
        val missingCommands = newCommands - oldCommands
        val unknownCommands = oldCommands - newCommands

        // 基于 PSI 生成“删除未知项后”的文件文本
        val file = outputPath.toFile()
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        val elementsToDelete = readAction {
            val list = mutableListOf<PsiElement>()
            list += CwtConfigGeneratorUtil.getElementsToDelete(psiFile, CONTAINER_PROMOTIONS) { toDelete(it, unknownPromotions) }
            list += CwtConfigGeneratorUtil.getElementsToDelete(psiFile, CONTAINER_COMMANDS) { toDelete(it, unknownCommands) }
            list
        }
        var modifiedText = CwtConfigGeneratorUtil.getFileText(psiFile, elementsToDelete)

        // 将缺失项直接插入到现有容器末尾（空行 + 注释 + 条目）
        val insertBlockForPromotions = buildString {
            if (unknownPromotions.isNotEmpty()) {
                appendLine(NOTE_UNKNOWN_PROMOTIONS)
                appendLine()
            }
            if (missingPromotions.isNotEmpty()) {
                appendLine(TODO_MISSING_PROMOTIONS)
                for (name in missingPromotions.sorted()) {
                    val scopes = promotionScopesFromLog[name].orEmpty()
                    // 跳过 any（不需要生成项）
                    if ("any" in scopes) continue
                    val valueText = scopes.sorted().joinToString(" ", "{ ", " }").ifEmpty { "{}" }
                    appendLine("${name} = ${valueText}")
                }
            }
        }.trimEnd()
        if (insertBlockForPromotions.isNotEmpty()) {
            val psiFile = readAction { CwtElementFactory.createDummyFile(project, modifiedText) }
            modifiedText = CwtConfigGeneratorUtil.insertIntoContainer(psiFile, CONTAINER_PROMOTIONS, insertBlockForPromotions)
        }
        val insertBlockForCommands = buildString {
            if (unknownCommands.isNotEmpty()) {
                appendLine(NOTE_UNKNOWN_COMMANDS)
                appendLine()
            }
            if (missingCommands.isNotEmpty()) {
                appendLine(TODO_MOSSING_COMMANDS)
                for (name in missingCommands.sorted()) {
                    val scopes = commandScopesFromLog[name].orEmpty()
                    val valueText = when {
                        scopes.isEmpty() -> "{}"
                        "any" in scopes -> "{ any }"
                        else -> scopes.sorted().joinToString(" ", "{ ", " }")
                    }
                    appendLine("${name} = ${valueText}")
                }
            }
        }
        if (insertBlockForPromotions.isNotEmpty()) {
            val psiFile = readAction { CwtElementFactory.createDummyFile(project, modifiedText) }
            modifiedText = CwtConfigGeneratorUtil.insertIntoContainer(psiFile, CONTAINER_COMMANDS, insertBlockForCommands)
        }

        // 汇总摘要与详情
        val summary = buildString {
            if (missingPromotions.isNotEmpty()) appendLine("${missingPromotions.size} missing localisation promotions.")
            if (unknownPromotions.isNotEmpty()) appendLine("${unknownPromotions.size} unknown localisation promotions.")
            if (missingCommands.isNotEmpty()) appendLine("${missingCommands.size} missing localisation commands.")
            if (unknownCommands.isNotEmpty()) appendLine("${unknownCommands.size} unknown localisation commands.")
            if (isEmpty()) appendLine("No missing or unknown localisation promotions or commands.")
        }.trimEnd()
        val details = buildString {
            if (missingPromotions.isNotEmpty()) {
                appendLine("Missing localisation promotions:")
                missingPromotions.sorted().forEach { appendLine("- ${it}") }
            }
            if (unknownPromotions.isNotEmpty()) {
                appendLine("Unknown localisation promotions:")
                unknownPromotions.sorted().forEach { appendLine("- ${it}") }
            }
            if (missingCommands.isNotEmpty()) {
                appendLine("Missing localisation commands:")
                missingCommands.sorted().forEach { appendLine("- ${it}") }
            }
            if (unknownCommands.isNotEmpty()) {
                appendLine("Unknown localisation commands:")
                unknownCommands.sorted().forEach { appendLine("- ${it}") }
            }
        }.trimEnd()
        val fileText = modifiedText.trimEnd() + "\n" // ensure ends with a line break

        val hint = Hint(summary, details, fileText)
        hint.putUserData(Keys.missingPromotionNames, missingPromotions)
        hint.putUserData(Keys.unknownPromotionNames, unknownPromotions)
        hint.putUserData(Keys.missingCommandNames, missingCommands)
        hint.putUserData(Keys.unknownCommandNames, unknownCommands)
        hint.putUserData(Keys.promotionScopesFromLog, promotionScopesFromLog)
        hint.putUserData(Keys.commandScopesFromLog, commandScopesFromLog)
        return hint
    }

    private fun aggregateScopes(infos: List<LocalisationInfo>): Pair<Map<String, Set<String>>, Map<String, Set<String>>> {
        val promotionScopes = mutableMapOf<String, MutableSet<String>>()
        val commandScopes = mutableMapOf<String, MutableSet<String>>()
        for (info in infos) {
            val scopes = getScopeIds(info.name)
            for (k in info.promotions) promotionScopes.getOrPut(k) { mutableSetOf() } += scopes
            for (k in info.properties) commandScopes.getOrPut(k) { mutableSetOf() } += scopes
        }
        return promotionScopes.mapValues { it.value.toSet() } to commandScopes.mapValues { it.value.toSet() }
    }

    private fun getScopeIds(text: String): Set<String> = when (text) {
        "Base Scope" -> setOf("any")
        "Ship (and Starbase)" -> setOf("ship", "starbase")
        else -> setOf(text.lowercase().replace(" ", "_"))
    }

    private fun toDelete(member: CwtMember, unknownNames: Set<String>): Boolean {
        return member is CwtProperty && member.name in unknownNames
    }

    private enum class Position { ScopeName, Promotions, Properties }

    data class LocalisationInfo(
        val name: String,
        val promotions: Set<String>,
        val properties: Set<String>,
    )

    data class LocalisationConfigInfo(
        val promotions: Set<String> = emptySet(),
        val commands: Set<String> = emptySet(),
    )

    object Keys : KeyRegistry() {
        val missingPromotionNames by createKey<Set<String>>(Keys)
        val unknownPromotionNames by createKey<Set<String>>(Keys)
        val missingCommandNames by createKey<Set<String>>(Keys)
        val unknownCommandNames by createKey<Set<String>>(Keys)
        val promotionScopesFromLog by createKey<Map<String, Set<String>>>(Keys)
        val commandScopesFromLog by createKey<Map<String, Set<String>>>(Keys)
    }

    private companion object {
        private const val CONTAINER_PROMOTIONS = "localisation_promotions"
        private const val CONTAINER_COMMANDS = "localisation_commands"

        private const val NOTE_UNKNOWN_PROMOTIONS = "# NOTE unknown localisation promotions are deleted"
        private const val TODO_MISSING_PROMOTIONS = "# TODO missing localisation promotions (key is the link name, value is the scope types)"
        private const val NOTE_UNKNOWN_COMMANDS = "# NOTE unknown localisation commands are deleted"
        private const val TODO_MOSSING_COMMANDS = "# TODO missing localisation commands (key is the command name, value is the scope types)"
    }
}
