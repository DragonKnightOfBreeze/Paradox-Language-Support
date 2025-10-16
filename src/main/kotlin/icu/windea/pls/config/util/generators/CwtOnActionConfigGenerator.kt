package icu.windea.pls.config.util.generators

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig
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
 * 从 `common/on_actions` 中的脚本文件生成 `on_actions.cwt`。
 *
 * @see CwtExtendedOnActionConfig
 */
class CwtOnActionConfigGenerator(override val project: Project) : CwtConfigGenerator {
    override val fromScripts get() = true

    override fun getName() = "OnActionConfigGenerator"

    override fun getDefaultInputName() = "common/on_actions"

    override fun getDefaultOutputName() = "on_actions.cwt"

    override suspend fun generate(gameType: ParadoxGameType, inputPath: String, outputPath: String): Hint {
        // 解析脚本目录，收集名称集合
        val namesFromScripts = parseScriptFiles(inputPath, gameType)
        // 解析现有 CWT 配置（PSI）：静态名集合 + 模板正则列表
        val configInfo = parseConfigFile(outputPath, gameType)
        // 差异：缺失（考虑模板匹配），未知（仅静态名）
        return generateHint(outputPath, namesFromScripts, configInfo)
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

    private suspend fun parseConfigFile(outputPath: String, gameType: ParadoxGameType): OnActionConfigInfo {
        val file = outputPath.toFile()
        if (!file.exists()) return OnActionConfigInfo() // file not exist -> return empty
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        return readAction {
            val fileConfig = CwtFileConfig.resolve(psiFile, file.name, CwtConfigGroup(project, gameType))
            val rootConfig = fileConfig.properties.find { it.key == CONTAINER_ON_ACTIONS }
            val configs = rootConfig?.configs.orEmpty().mapNotNull { CwtExtendedOnActionConfig.resolve(it) }
            parseConfigInfo(configs)
        }
    }

    private fun parseConfigInfo(configs: List<CwtExtendedOnActionConfig>): OnActionConfigInfo {
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
        return OnActionConfigInfo(names, sortedTemplates)
    }

    private suspend fun generateHint(outputPath: String, namesFromScripts: Set<String>, configInfo: OnActionConfigInfo): Hint {
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

        // 删除未知静态名并生成文本
        val file = outputPath.toFile()
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        val elementsToDelete = readAction { CwtConfigGeneratorUtil.getElementsToDelete(psiFile, CONTAINER_ON_ACTIONS) { toDelete(it, removedNames) } }
        var modifiedText = CwtConfigGeneratorUtil.getFileText(psiFile, elementsToDelete)

        // 在容器末尾插入缺失名（空行 + 注释 + 条目）
        if (addedNames.isNotEmpty()) {
            val insertBlock = buildString {
                appendLine(NOTE_REMOVED_ON_ACTIONS)
                appendLine()
                appendLine(TODO_ADDED_ON_ACTIONS)
                for (name in addedNames.sorted()) {
                    appendLine(name)
                }
            }.trimEnd()
            val psiFile = readAction { CwtElementFactory.createDummyFile(project, modifiedText) }
            modifiedText = CwtConfigGeneratorUtil.insertIntoContainer(psiFile, CONTAINER_ON_ACTIONS, insertBlock)
        }

        // 汇总
        val summary = buildString {
            if (addedNames.isNotEmpty()) appendLine("${addedNames.size} added on actions.")
            if (removedNames.isNotEmpty()) appendLine("${removedNames.size} removed on actions.")
            if (isEmpty()) appendLine("No added or removed on actions.")
        }.trimEnd()
        val details = buildString {
            if (addedNames.isNotEmpty()) {
                appendLine("Added on actions:")
                addedNames.sorted().forEach { appendLine("- $it") }
            }
            if (removedNames.isNotEmpty()) {
                appendLine("Removed on actions:")
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

    private fun toDelete(member: CwtMember, removedNames: Set<String>): Boolean {
        return member.name in removedNames
    }

    data class OnActionConfigInfo(
        val names: Set<String> = emptySet(),
        val templates: Set<CwtTemplateExpression> = emptySet(),
    )

    object Keys : KeyRegistry() {
        val addedNames by createKey<Set<String>>(Keys)
        val removedNames by createKey<Set<String>>(Keys)
        val unmatchedTemplates by createKey<Set<CwtTemplateExpression>>(Keys)
        val namesFromScripts by createKey<Set<String>>(Keys)
        val configInfo by createKey<OnActionConfigInfo>(Keys)
    }

    private companion object {
        private const val CONTAINER_ON_ACTIONS = "on_actions"

        private const val NOTE_REMOVED_ON_ACTIONS = "# NOTE removed on actions are deleted"
        private const val TODO_ADDED_ON_ACTIONS = "# TODO added on actions"
    }
}
