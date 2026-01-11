package icu.windea.pls.lang.util

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configContext.CwtConfigContext
import icu.windea.pls.config.configContext.inlineScriptHasConflict
import icu.windea.pls.config.configContext.inlineScriptHasRecursion
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
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.psi.ParadoxPsiManager
import icu.windea.pls.lang.psi.property
import icu.windea.pls.lang.psi.search
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxInlineScriptUsageSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.inlineScriptUsage
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.util.ParadoxInlineScriptManager.inlineScriptPathExpression
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptLightTreeUtil
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.parentBlock
import icu.windea.pls.script.psi.parentProperty
import icu.windea.pls.script.psi.resolved
import icu.windea.pls.script.psi.stringValue

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
     * 从内联脚本用法对应的 PSI，得到内联脚本表达式对应的 PSI。
     *
     * - `inline_script = "some/expression"` -> `"some/expression"`
     * - `inline_script = { script = "some/expression" }` -> `"some/expression"`
     */
    fun getExpressionElement(usageElement: ParadoxScriptProperty): ParadoxScriptValue? {
        // hardcoded
        if (!isMatched(usageElement.name)) return null // NOTE 2.1.0 这里目前不验证游戏类型
        val v = usageElement.propertyValue ?: return null
        val v1 = v.takeIf { it is ParadoxScriptString || it is ParadoxScriptScriptedVariable }
        if (v1 != null) return v1
        val v2 = v.search { property("script") }?.propertyValue?.takeIf { it is ParadoxScriptString || it is ParadoxScriptScriptedVariable }
        if (v2 != null) return v2
        return null
    }

    /**
     * 从内联脚本表达式对应的 PSI，得到内联脚本用法对应的 PSI。
     *
     * - `"some/expression"` -> `inline_script = "some/expression"`
     * - `"some/expression"` -> `inline_script = { script = "some/expression" }`
     */
    fun getUsageElement(expressionElement: PsiElement): ParadoxScriptProperty? {
        // hardcoded
        if (expressionElement !is ParadoxScriptString && expressionElement !is ParadoxScriptScriptedVariableReference) return null
        val p1 = expressionElement.parentProperty ?: return null
        if (isMatched(p1.name)) return p1 // NOTE 2.1.0 这里目前不验证游戏类型
        if (p1.name.equals("script", true)) {
            val p2 = p1.parentBlock?.parentProperty ?: return null
            if (isMatched(p2.name)) return p2 // NOTE 2.1.0 这里目前不验证游戏类型
            return null
        }
        return null
    }

    /**
     * 从内联脚本用法对应的 PSI，得到对应的内联脚本表达式。
     *
     * @param resolve 如果内联脚本表达式对应的 PSI 是一个封装变量引用，是否尝试解析。
     */
    fun getInlineScriptExpressionFromUsageElement(usageElement: ParadoxScriptProperty, resolve: Boolean = false): String? {
        val expressionElement = getExpressionElement(usageElement)?.let { if (resolve) it.resolved() else it }
        if (expressionElement !is ParadoxScriptString) return null
        return expressionElement.stringValue.orNull()
    }

    /**
     * 从内联脚本用法对应的 Lighter AST，得到对应的内联脚本表达式。
     */
    fun getInlineScriptExpressionFromUsageElement(tree: LighterAST, node: LighterASTNode): String? {
        val v1 = ParadoxScriptLightTreeUtil.getStringValueFromPropertyNode(node, tree)
        if (v1 != null) return v1
        val v2 = ParadoxScriptLightTreeUtil.findPropertyFromPropertyNode(node, tree, "script")
            ?.let { ParadoxScriptLightTreeUtil.getStringValueFromPropertyNode(it, tree) }
        if (v2 != null) return v2
        return null
    }

    /**
     * 从内联脚本用法对应的 PSI，得到对应的传入参数的键值映射。
     *
     * @param resolve 如果传入参数的值对应的 PSI 是一个封装变量引用，是否尝试解析。
     */
    @Suppress("unused")
    fun getInlineScriptArgumentMapFromUsageElement(usageElement: ParadoxScriptProperty, resolve: Boolean = false): Map<String, String> {
        val v = usageElement.block ?: return emptyMap()
        return ParadoxPsiManager.getArgumentTupleList(v, "script").toMap()
    }

    /**
     * 得到指定的内联脚本表达式对应的内联脚本的推断的规则上下文。
     *
     * @param expression 指定的内联脚本表达式。用于定位内联脚本文件，例如，`test` 对应路径为 `common/inline_scripts/test.txt` 的内联脚本文件。
     */
    fun getInferredContextConfigs(expression: String, contextElement: ParadoxScriptMember, context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>> {
        if (!PlsSettings.getInstance().state.inference.configContextForInlineScripts) return emptyList()
        return withRecursionGuard {
            withRecursionCheck(expression) {
                context.inlineScriptHasConflict = false
                context.inlineScriptHasRecursion = false
                doGetInferredContextConfigs(expression, contextElement, context, matchOptions)
            }
        } ?: run {
            context.inlineScriptHasRecursion = true
            emptyList()
        }
    }

    private fun doGetInferredContextConfigs(expression: String, contextElement: ParadoxScriptMember, context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>> {
        val fromConfig = doGetInferredContextConfigsFromConfig(expression, contextElement, context, matchOptions)
        if (fromConfig.isNotEmpty()) return fromConfig

        return doGetInferredContextConfigsFromUsages(expression, contextElement, context, matchOptions)
    }

    private fun doGetInferredContextConfigsFromConfig(expression: String, contextElement: ParadoxScriptMember, context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>> {
        val configGroup = context.configGroup
        val config = configGroup.extendedInlineScripts.findByPattern(expression, contextElement, configGroup, matchOptions) ?: return emptyList()
        return config.getContextConfigs()
    }

    private fun doGetInferredContextConfigsFromUsages(expression: String, contextElement: ParadoxScriptMember, context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>> {
        // infer & merge
        val fastInference = PlsSettings.getInstance().state.inference.configContextForInlineScriptsFast
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        val project = context.configGroup.project
        val selector = selector(project, contextElement).inlineScriptUsage()
        ParadoxInlineScriptUsageSearch.search(expression, selector).processAsync p@{ p ->
            if (!isMatched(p.name)) return@p true // 再次确认
            val memberElement = p.parentOfType<ParadoxScriptMember>() ?: return@p true
            val usageConfigContext = ParadoxExpressionManager.getConfigContext(memberElement) ?: return@p true
            val usageConfigs = usageConfigContext.getConfigs(matchOptions).orNull()
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
