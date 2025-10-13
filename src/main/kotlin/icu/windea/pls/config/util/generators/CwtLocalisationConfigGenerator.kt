package icu.windea.pls.config.util.generators

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.siblings
import icu.windea.pls.core.children
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
        val file = java.io.File(outputPath)
        val text = withContext(Dispatchers.IO) { runCatching { file.readText() }.getOrElse { "" } }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        val elementsToDelete = readAction {
            val list = mutableListOf<PsiElement>()
            list += getNestedElementsToDelete(psiFile, CONTAINER_PROMOTIONS, unknownPromotions)
            list += getNestedElementsToDelete(psiFile, CONTAINER_COMMANDS, unknownCommands)
            list
        }
        val modifiedText = CwtConfigGeneratorUtil.getFileText(psiFile, elementsToDelete)

        // 5) 生成缺失项的 TODO 片段并拼接
        val missingText = buildString {
            if (missingPromotions.isNotEmpty()) {
                appendLine()
                appendLine("# TODO missing localisation_promotions")
                for (name in missingPromotions.sorted()) {
                    val scopes = promotionScopesFromLog[name].orEmpty()
                    // 跳过 any（不需要生成项）
                    if ("any" in scopes) continue
                    val scopesText = CwtConfigGeneratorUtil.getScopesOptionText(scopes)
                    // 格式：key = { scopes }
                    val valueText = scopes.sorted().joinToString(" ", prefix = "{ ", postfix = " }").ifEmpty { "{}" }
                    // 保留 scopes 选项以利于后续维护
                    appendLine("# ${scopesText}")
                    appendLine("${INDENT}${name} = ${valueText}")
                }
            }
            if (missingCommands.isNotEmpty()) {
                appendLine()
                appendLine("# TODO missing localisation_commands")
                for (name in missingCommands.sorted()) {
                    val scopes = commandScopesFromLog[name].orEmpty()
                    val valueText = when {
                        scopes.isEmpty() -> "{}"
                        "any" in scopes -> "{ any }"
                        else -> scopes.sorted().joinToString(" ", prefix = "{ ", postfix = " }")
                    }
                    val scopesText = CwtConfigGeneratorUtil.getScopesOptionText(scopes)
                    appendLine("# ${scopesText}")
                    appendLine("${INDENT}${name} = ${valueText}")
                }
            }
        }.trimEnd()

        val fileText = buildString {
            append(modifiedText)
            if (missingText.isNotEmpty()) {
                appendLine()
                appendLine(missingText)
            }
        }.trimEnd()

        // 6) 汇总摘要与详情
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
        val logFile = java.io.File(inputPath)
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
            val topLevel = psiFile.block
            val topLevelProps = topLevel?.children()?.filterIsInstance<CwtProperty>()?.toList().orEmpty()
            val promotionsProp = topLevelProps.firstOrNull { it.name == CONTAINER_PROMOTIONS }
            val commandsProp = topLevelProps.firstOrNull { it.name == CONTAINER_COMMANDS }

            val promotionNames = promotionsProp?.let {
                PsiTreeUtil.findChildrenOfType(it, CwtProperty::class.java)
                    .mapNotNull { p -> p.name }
                    .filter { n -> n != CONTAINER_PROMOTIONS && n != CONTAINER_COMMANDS }
                    .toSet()
            }.orEmpty()
            val commandNames = commandsProp?.let {
                PsiTreeUtil.findChildrenOfType(it, CwtProperty::class.java)
                    .mapNotNull { p -> p.name }
                    .filter { n -> n != CONTAINER_PROMOTIONS && n != CONTAINER_COMMANDS }
                    .toSet()
            }.orEmpty()
            LocalisationConfigInfo(promotionNames, commandNames)
        }
    }

    private fun getNestedElementsToDelete(
        psiFile: CwtFile,
        containerPropertyName: String,
        namesToDelete: Set<String>
    ): MutableList<PsiElement> {
        val result = mutableListOf<PsiElement>()
        val rootProps = psiFile.block?.children()?.filterIsInstance<CwtProperty>()?.toList().orEmpty()
        val container = rootProps.firstOrNull { it.name == containerPropertyName } ?: return result
        val nestedProps = PsiTreeUtil.findChildrenOfType(container, CwtProperty::class.java)
            .filter { p -> p != container && p.name in namesToDelete }
        for (p in nestedProps) {
            result += p
            p.siblings(forward = false, withSelf = false)
                .takeWhile { e -> e !is CwtProperty }
                .forEach { e -> result += e }
        }
        return result
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
        const val CONTAINER_PROMOTIONS = "localisation_promotions"
        const val CONTAINER_COMMANDS = "localisation_commands"
        const val INDENT = "    "
    }
}
