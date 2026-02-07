package icu.windea.pls.lang.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.mergeValue
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.processAsync
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.resolve.expression.ParadoxPathReferenceExpressionSupport
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.resolve.CwtConfigContext
import icu.windea.pls.lang.resolve.inlineScriptHasConflict
import icu.windea.pls.lang.resolve.inlineScriptHasRecursion
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxInlineScriptUsageSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.util.ParadoxInlineScriptManager.inlineScriptPathExpression
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptString

object ParadoxInlineScriptManager {
    const val inlineScriptKey = "inline_script"
    val inlineScriptPathExpression = CwtDataExpression.resolve("filepath[common/inline_scripts/,.txt]", false)

    /**
     * 检测指定的游戏类型是否支持内联脚本。
     */
    fun isSupported(gameType: ParadoxGameType?): Boolean {
        if (gameType == null) return false
        val configGroup = PlsFacade.getConfigGroup(gameType)
        val configs = configGroup.directivesModel.inlineScript
        if (configs.isEmpty()) return false
        return true
    }

    /**
     * 检查输入的字符串是否匹配内联脚本用法的键（不会检查游戏类型）。
     */
    fun isMatched(expression: String): Boolean {
        if (!expression.equals(inlineScriptKey, true)) return false // 这里忽略 `expression` 的大小写
        return true
    }

    /**
     * 检查输入的字符串是否匹配内联脚本用法的键（会从 [context] 选取游戏类型并检查）。
     */
    fun isMatched(expression: String, context: Any?): Boolean {
        if (context == null) return false
        if (!expression.equals(inlineScriptKey, true)) return false // 这里忽略 `expression` 的大小写
        return isSupported(selectGameType(context))
    }

    /**
     * 检查内联脚本用法在 [element] 对应的位置是否可用（不一定实际受游戏支持，格式也不一定正确）。这意味着至少会提供代码高亮。
     */
    fun isAvailable(element: ParadoxScriptProperty): Boolean {
        val propertyValue = element.propertyValue
        if (propertyValue !is ParadoxScriptString && propertyValue !is ParadoxScriptScriptedVariableReference && propertyValue !is ParadoxScriptBlock) return false
        val file = element.containingFile ?: return false
        if (!ParadoxPsiFileMatcher.isScriptFile(file, ParadoxPathConstraint.AcceptInlineScriptUsage, injectable = true)) return false // 额外检查
        return true
    }

    /**
     * 按照 [inlineScriptPathExpression]，尝试将指定的 [pathReference] 解析为内联脚本文件的路径。
     */
    fun getInlineScriptFilePath(pathReference: String): String? {
        val configExpression = inlineScriptPathExpression
        return ParadoxPathReferenceExpressionSupport.get(configExpression)?.resolvePath(configExpression, pathReference.normalizePath())?.firstOrNull()
    }

    /**
     * 得到指定的 [file] 对应的内联脚本表达式。如果不是内联脚本文件则返回 `null`。
     */
    fun getInlineScriptExpression(file: VirtualFile): String? {
        val fileType = file.fileType
        if (fileType != ParadoxScriptFileType) return null
        val fileInfo = file.fileInfo ?: return null
        val filePath = fileInfo.path.path
        val configExpression = inlineScriptPathExpression
        return ParadoxPathReferenceExpressionSupport.get(configExpression)?.extract(configExpression, null, filePath)?.orNull()
    }

    /**
     * 得到指定的 [file] 对应的内联脚本表达式。如果不是内联脚本文件则返回 `null`。
     */
    fun getInlineScriptExpression(file: PsiFile): String? {
        if (file !is ParadoxScriptFile) return null
        val vFile = selectFile(file) ?: return null
        return getInlineScriptExpression(vFile)
    }

    /**
     * 得到指定的内联脚本表达式对应的内联脚本文件。
     *
     * @param expression 指定的内联脚本表达式。用于定位内联脚本文件，例如，`test` 对应路径为 `common/inline_scripts/test.txt` 的内联脚本文件。
     */
    fun getInlineScriptFile(expression: String, project: Project, context: Any?): ParadoxScriptFile? {
        val selector = selector(project, context).file().contextSensitive()
        return ParadoxFilePathSearch.searchInlineScript(expression, selector).find()?.toPsiFile(project)?.castOrNull()
    }

    /**
     * 得到指定的内联脚本表达式对应的所有内联脚本文件。
     *
     * @param expression 指定的内联脚本表达式。用于定位内联脚本文件，例如，`test` 对应路径为 `common/inline_scripts/test.txt` 的内联脚本文件。
     */
    fun getInlineScriptFiles(expression: String, project: Project, context: Any?): List<ParadoxScriptFile> {
        val selector = selector(project, context).file().contextSensitive()
        return ParadoxFilePathSearch.searchInlineScript(expression, selector).findAll().mapNotNull { it.toPsiFile(project)?.castOrNull() }
    }

    /**
     * 遍历指定的内联脚本表达式对应的内联脚本文件。
     *
     * @param expression 指定的内联脚本表达式。用于定位内联脚本文件，例如，`test` 对应路径为 `common/inline_scripts/test.txt` 的内联脚本文件。
     */
    fun processInlineScriptFile(expression: String, project: Project, context: Any?, onlyMostRelevant: Boolean = false, processor: (ParadoxScriptFile) -> Boolean): Boolean {
        val selector = selector(project, context).file().contextSensitive()
        return ParadoxFilePathSearch.searchInlineScript(expression, selector).onlyMostRelevant(onlyMostRelevant).processAsync p@{
            val file = it.toPsiFile(project)?.castOrNull<ParadoxScriptFile>() ?: return@p true
            processor(file)
            true
        }
    }

    /**
     * 得到指定的内联脚本表达式对应的内联脚本的推断的规则上下文。
     *
     * @param expression 指定的内联脚本表达式。用于定位内联脚本文件，例如，`test` 对应路径为 `common/inline_scripts/test.txt` 的内联脚本文件。
     */
    fun getInferredContextConfigs(expression: String, contextElement: ParadoxScriptMember, context: CwtConfigContext, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        if (!PlsSettings.getInstance().state.inference.configContextForInlineScripts) return emptyList()
        return withRecursionGuard {
            withRecursionCheck(expression) {
                context.inlineScriptHasConflict = false
                context.inlineScriptHasRecursion = false
                doGetInferredContextConfigs(expression, contextElement, context, options)
            }
        } ?: run {
            context.inlineScriptHasRecursion = true
            emptyList()
        }
    }

    private fun doGetInferredContextConfigs(expression: String, contextElement: ParadoxScriptMember, context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> {
        val fromConfig = doGetInferredContextConfigsFromConfig(expression, contextElement, context, options)
        if (fromConfig.isNotEmpty()) return fromConfig

        return doGetInferredContextConfigsFromUsages(expression, contextElement, context, options)
    }

    private fun doGetInferredContextConfigsFromConfig(expression: String, contextElement: ParadoxScriptMember, context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> {
        val configGroup = context.configGroup
        val config = configGroup.extendedInlineScripts.findByPattern(expression, contextElement, configGroup, options) ?: return emptyList()
        return config.getContextConfigs()
    }

    private fun doGetInferredContextConfigsFromUsages(expression: String, contextElement: ParadoxScriptMember, context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> {
        // infer & merge
        val fastInference = PlsSettings.getInstance().state.inference.configContextForInlineScriptsFast
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        val project = context.configGroup.project
        val selector = selector(project, contextElement).inlineScriptUsage()
        ParadoxInlineScriptUsageSearch.search(expression, selector).processAsync p@{ p ->
            if (!isMatched(p.name)) return@p true // 再次确认
            val memberElement = p.parentOfType<ParadoxScriptMember>() ?: return@p true
            val usageConfigContext = ParadoxConfigManager.getConfigContext(memberElement) ?: return@p true
            val usageConfigs = usageConfigContext.getConfigs(options).orNull()
            if (fastInference && usageConfigs.isNotNullOrEmpty()) {
                result.set(usageConfigs)
                return@p false
            }
            // merge
            result.mergeValue(usageConfigs) { v1, v2 -> CwtConfigManipulator.mergeConfigs(v1, v2) }.also {
                if (it) return@also
                context.inlineScriptHasConflict = true
                result.set(null)
            }
        }
        return result.get().orEmpty()
    }
}
