package icu.windea.pls.config.util.generators

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.delegated.CwtExtendedGameRuleConfig
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.generators.CwtConfigGenerator.Hint
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.toFile
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.cwt.psi.CwtElementFactory
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.lang.util.CwtTemplateExpressionManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.properties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 从 `common/game_rules` 中的脚本文件生成 `game_rules.cwt`。
 *
 * @see CwtExtendedGameRuleConfig
 */
class CwtGameRuleConfigGenerator(override val project: Project) : CwtConfigGenerator {
    override val fromScripts get() = true

    override fun getName() = "GameRuleConfigGenerator"

    override fun getDefaultInputName() = "common/game_rules"

    override fun getDefaultOutputName() = "game_rules.cwt"

    override suspend fun generate(gameType: ParadoxGameType, inputPath: String, outputPath: String): Hint {
        // 1) 解析脚本目录，收集名称集合
        val namesFromScripts = parseScriptFiles(inputPath, gameType)

        // 2) 解析现有 CWT 配置（PSI）：静态名集合 + 模板正则列表
        val configInfo = parseConfigFile(outputPath, gameType)

        // 3) 差异：缺失（考虑模板匹配），未知（仅静态名）
        val addedNames = namesFromScripts
            .filter { name -> name !in configInfo.names }
            .filter { name -> configInfo.templates.none { CwtTemplateExpressionManager.toRegex(it).matches(name) } }
            .toSet()
        val removedNames = configInfo.names
            .filter { name -> name !in namesFromScripts }
            .toSet()
        val unmatchedTemplates = configInfo.templates
            .filter { namesFromScripts.none { name -> CwtTemplateExpressionManager.toRegex(it).matches(name) } }
            .toSet()

        // 4) 删除未知静态名并生成文本
        val file = outputPath.toFile()
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        val elementsToDelete = readAction { CwtConfigGeneratorUtil.getElementsToDelete(psiFile, CONTAINER_GAME_RULES) { toDelete(it, removedNames) } }
        var modifiedText = CwtConfigGeneratorUtil.getFileText(psiFile, elementsToDelete)

        // 5) 在容器末尾插入缺失名（空行 + 注释 + 条目）
        if (addedNames.isNotEmpty()) {
            val insertBlock = buildString {
                appendLine(NOTE_REMOVED_GAME_RULES)
                appendLine()
                appendLine(TODO_ADDED_GAME_RULES)
                for (name in addedNames.sorted()) {
                    appendLine(name)
                }
            }.trimEnd()
            val psiFile = readAction { CwtElementFactory.createDummyFile(project, modifiedText) }
            modifiedText = CwtConfigGeneratorUtil.insertIntoContainer(psiFile, CONTAINER_GAME_RULES, insertBlock)
        }

        // 6) 汇总
        val summary = buildString {
            if (addedNames.isNotEmpty()) appendLine("${addedNames.size} added game rules.")
            if (removedNames.isNotEmpty()) appendLine("${removedNames.size} removed game rules.")
            if (isEmpty()) appendLine("No added or removed game rules.")
        }.trimEnd()
        val details = buildString {
            if (addedNames.isNotEmpty()) {
                appendLine("Added game rules:")
                addedNames.sorted().forEach { appendLine("- $it") }
            }
            if (removedNames.isNotEmpty()) {
                appendLine("Removed game rules:")
                removedNames.sorted().forEach { appendLine("- $it") }
            }
            if (unmatchedTemplates.isNotEmpty()) {
                appendLine("Unmatched templates:")
                unmatchedTemplates.forEach { appendLine("- $it") }
            }
        }.trimEnd()
        val fileText = modifiedText.trimEnd() + "\n" // ensure ends with a line break

        val hint = Hint(summary, details, fileText)
        hint.putUserData(Keys.addedNames, addedNames)
        hint.putUserData(Keys.removedNames, removedNames)
        hint.putUserData(Keys.unmatchedTemplates, unmatchedTemplates)
        hint.putUserData(Keys.namesFromScripts, namesFromScripts)
        hint.putUserData(Keys.configInfo, configInfo)
        return hint
    }

    private suspend fun parseScriptFiles(inputPath: String, gameType: ParadoxGameType): Set<String> {
        val dir = CwtConfigGeneratorUtil.getPathInGameDirectory(inputPath, gameType)?.toFile()
        if (dir == null) throw IllegalStateException("Path `${inputPath}` in game directory of ${gameType.title} not exist")
        if (!dir.isDirectory) throw IllegalStateException("Path `${inputPath}` in game directory of ${gameType.title} is not a directory")
        val files = dir.walk().filter { it.isFile && it.extension.equals("txt", true) }
        val names = linkedSetOf<String>()
        for (file in files) {
            val text = withContext(Dispatchers.IO) { file.readText() }
            val psiFile = readAction { ParadoxScriptElementFactory.createDummyFile(project, text) }
            readAction { psiFile.properties().forEach { names += it.name } }
        }
        return names
    }

    private suspend fun parseConfigFile(outputPath: String, gameType: ParadoxGameType): GameRuleConfigInfo {
        val file = outputPath.toFile()
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        return readAction {
            val fileConfig = CwtFileConfig.resolve(psiFile, file.name, CwtConfigGroup(project, gameType))
            val rootConfig = fileConfig.properties.find { it.key == CONTAINER_GAME_RULES }
            val configs = rootConfig?.configs.orEmpty().map { CwtExtendedGameRuleConfig.resolve(it) }
            parseConfigInfo(configs)
        }
    }

    private fun parseConfigInfo(configs: List<CwtExtendedGameRuleConfig>): GameRuleConfigInfo {
        val names = caseInsensitiveStringSet()
        val templates = mutableSetOf<CwtTemplateExpression>()
        configs.forEach {
            val name = it.name
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
        return GameRuleConfigInfo(names, sortedTemplates)
    }

    private fun toDelete(member: CwtMember, removedNames: Set<String>): Boolean {
        return member.name in removedNames
    }

    data class GameRuleConfigInfo(
        val names: Set<String>,
        val templates: Set<CwtTemplateExpression>,
    )

    object Keys : KeyRegistry() {
        val addedNames by createKey<Set<String>>(Keys)
        val removedNames by createKey<Set<String>>(Keys)
        val unmatchedTemplates by createKey<Set<CwtTemplateExpression>>(Keys)
        val namesFromScripts by createKey<Set<String>>(Keys)
        val configInfo by createKey<GameRuleConfigInfo>(Keys)
    }

    private companion object {
        private const val CONTAINER_GAME_RULES = "game_rules"
        private const val NOTE_REMOVED_GAME_RULES = "# NOTE removed game rules are deleted"
        private const val TODO_ADDED_GAME_RULES = "# TODO added game rules"
    }
}
