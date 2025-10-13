package icu.windea.pls.config.util.generators

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.siblings
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.collections.filterIsInstance
import icu.windea.pls.core.toFile
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtElementFactory
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 从 `localisations.log` 生成 `localisations.cwt`。
 */
class CwtLocalisationConfigGenerator(
    override val gameType: ParadoxGameType,
    override val inputPath: String,
    override val outputPath: String,
) : CwtConfigGenerator {
    override suspend fun generate(project: Project): CwtConfigGenerator.Hint {
        // 1) 解析日志文件，汇总 promotions/commands -> scopes
        val infos = parseLogFile()
        val (promotionScopesFromLog, commandScopesFromLog) = aggregateScopes(infos)

        // 2) 解析现有 CWT 配置（PSI）以获取已存在的键集合
        val configInfo = parseConfigFile(project)

        // 3) 计算差异
        val promotionNamesInLog = promotionScopesFromLog.keys
        val commandNamesInLog = commandScopesFromLog.keys
        val missingPromotions = promotionNamesInLog - configInfo.promotions
        val unknownPromotions = configInfo.promotions - promotionNamesInLog
        val missingCommands = commandNamesInLog - configInfo.commands
        val unknownCommands = configInfo.commands - commandNamesInLog

        // 4) 基于 PSI 生成“删除未知项后”的文件文本
        val file = outputPath.toFile()
        val text = withContext(Dispatchers.IO) { runCatching { file.readText() }.getOrElse { "" } }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        val elementsToDelete = readAction {
            val list = mutableListOf<PsiElement>()
            list += getElementsToDelete(psiFile, CONTAINER_PROMOTIONS, unknownPromotions)
            list += getElementsToDelete(psiFile, CONTAINER_COMMANDS, unknownCommands)
            list
        }
        var modifiedText = CwtConfigGeneratorUtil.getFileText(psiFile, elementsToDelete)

        // 5) 将缺失项直接插入到现有容器末尾（空行 + TODO 注释 + 条目）
        if (missingPromotions.isNotEmpty()) {
            val insertBlock = buildString {
                appendLine(TODO_MISSING_PROMOTIONS)
                for (name in missingPromotions.sorted()) {
                    val scopes = promotionScopesFromLog[name].orEmpty()
                    // 跳过 any（不需要生成项）
                    if ("any" in scopes) continue
                    val valueText = scopes.sorted().joinToString(" ", prefix = "{ ", postfix = " }").ifEmpty { "{}" }
                    appendLine("${name} = ${valueText}")
                }
            }.trimEnd()
            modifiedText = insertIntoContainer(project, modifiedText, CONTAINER_PROMOTIONS, insertBlock)
        }
        if (missingCommands.isNotEmpty()) {
            val insertBlock = buildString {
                appendLine(TODO_MOSSING_COMMANDS)
                for (name in missingCommands.sorted()) {
                    val scopes = commandScopesFromLog[name].orEmpty()
                    val valueText = when {
                        scopes.isEmpty() -> "{}"
                        "any" in scopes -> "{ any }"
                        else -> scopes.sorted().joinToString(" ", prefix = "{ ", postfix = " }")
                    }
                    appendLine("${name} = ${valueText}")
                }
            }.trimEnd()
            modifiedText = insertIntoContainer(project, modifiedText, CONTAINER_COMMANDS, insertBlock)
        }
        val fileText = modifiedText.trim()

        // 6) 汇总摘要与详情
        val summary = buildString {
            if (missingPromotions.isNotEmpty()) appendLine("${missingPromotions.size} missing localisation promotions.")
            if (unknownPromotions.isNotEmpty()) appendLine("${unknownPromotions.size} unknown localisation promotions.")
            if (missingCommands.isNotEmpty()) appendLine("${missingCommands.size} missing localisation commands.")
            if (unknownCommands.isNotEmpty()) appendLine("${unknownCommands.size} unknown localisation commands.")
            if (isEmpty()) appendLine("No missing or unknown localisation promotions or commands.")
        }.trim()
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
        }.trim()

        val hint = CwtConfigGenerator.Hint(summary, details, fileText)
        hint.putUserData(Keys.missingPromotionNames, missingPromotions)
        hint.putUserData(Keys.unknownPromotionNames, unknownPromotions)
        hint.putUserData(Keys.missingCommandNames, missingCommands)
        hint.putUserData(Keys.unknownCommandNames, unknownCommands)
        hint.putUserData(Keys.promotionScopesFromLog, promotionScopesFromLog)
        hint.putUserData(Keys.commandScopesFromLog, commandScopesFromLog)
        return hint
    }

    private data class LocalisationInfo(
        var name: String = "",
        val promotions: MutableSet<String> = mutableSetOf(),
        val properties: MutableSet<String> = mutableSetOf(),
    )

    private enum class Position { ScopeName, Promotions, Properties }

    private fun parseLogFile(): List<LocalisationInfo> {
        val logFile = inputPath.toFile()
        val allLines = runCatching { logFile.readLines() }.getOrElse { emptyList() }
        val infos = mutableListOf<LocalisationInfo>()
        var info = LocalisationInfo()
        var position = Position.ScopeName
        for (raw in allLines) {
            val line = raw.trim()
            if (line.startsWith("--") && line.endsWith("--")) {
                if (info.name.isNotEmpty()) {
                    infos += info
                    info = LocalisationInfo()
                }
                info.name = line.removePrefix("--").removeSuffix("--").trim()
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
                    if (v != null) info.promotions += v
                }
                Position.Properties -> {
                    val v = line.takeIf { it.isNotEmpty() && '=' !in it }
                    if (v != null) info.properties += v
                }
                else -> {}
            }
        }
        if (info.name.isNotEmpty()) infos += info
        return infos
    }

    private data class LocalisationConfigInfo(
        val promotions: Set<String>,
        val commands: Set<String>,
    )

    private suspend fun parseConfigFile(project: Project): LocalisationConfigInfo {
        val file = java.io.File(outputPath)
        val text = withContext(Dispatchers.IO) { runCatching { file.readText() }.getOrElse { "" } }
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

    private fun getElementsToDelete(psiFile: CwtFile, containerPropertyName: String, propertyNamesToDelete: Set<String>): MutableList<PsiElement> {
        val result = mutableListOf<PsiElement>()
        val rootBlock = psiFile.block
        val rootProps = rootBlock?.children()?.filterIsInstance<CwtProperty>()?.toList()
        val container = rootProps?.find { it.name == containerPropertyName }
        val propsToDelete = container?.propertyValue?.castOrNull<CwtBlock>()?.children()
            ?.filterIsInstance<CwtProperty> { it.name in propertyNamesToDelete }
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
        // fallback：容器缺失，直接在文件末尾追加完整容器
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
        private const val INDENT = "    "

        private const val TODO_MISSING_PROMOTIONS = "# TODO missing localisation promotions (in actual, key is the link name, value is the scope types)"
        private const val TODO_MOSSING_COMMANDS = "# TODO missing localisation commands (in actual, key is the command name, value is the scope types)"
    }
}
