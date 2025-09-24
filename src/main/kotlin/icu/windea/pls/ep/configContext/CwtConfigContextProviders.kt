package icu.windea.pls.ep.configContext

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.declarationConfigCacheKey
import icu.windea.pls.config.configContext.CwtConfigContext
import icu.windea.pls.config.configContext.definitionInfo
import icu.windea.pls.config.configContext.elementPathFromRoot
import icu.windea.pls.config.configGroup.inlineConfigGroup
import icu.windea.pls.core.toInt
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.list
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue
import icu.windea.pls.core.util.singleton
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.lang.util.PlsVfsManager
import icu.windea.pls.model.paths.ParadoxExpressionPath
import icu.windea.pls.model.paths.relativeTo
import icu.windea.pls.script.psi.ParadoxScriptMemberElement
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.findParentDefinition
import icu.windea.pls.script.psi.isPropertyValue

//region Extensions

var CwtConfigContext.inlineScriptRootConfigContext: CwtConfigContext? by createKey(CwtConfigContext.Keys)
var CwtConfigContext.inlineScriptExpression: String? by createKey(CwtConfigContext.Keys)
var CwtConfigContext.inlineScriptHasConflict: Boolean? by createKey(CwtConfigContext.Keys)
var CwtConfigContext.inlineScriptHasRecursion: Boolean? by createKey(CwtConfigContext.Keys)
var CwtConfigContext.parameterValueRootConfigContext: CwtConfigContext? by createKey(CwtConfigContext.Keys)
var CwtConfigContext.parameterElement: ParadoxParameterElement? by createKey(CwtConfigContext.Keys)
var CwtConfigContext.parameterValueQuoted: Boolean? by createKey(CwtConfigContext.Keys)

//endregion

/**
 * 用于获取直接的CWT规则上下文。
 *
 * * 基于文件信息（包括注入的文件信息）。
 */
class BaseCwtConfigContextProvider : CwtConfigContextProvider {
    override fun getContext(element: ParadoxScriptMemberElement, elementPath: ParadoxExpressionPath, file: PsiFile): CwtConfigContext? {
        ProgressManager.checkCanceled()
        val vFile = selectFile(file) ?: return null

        val fileInfo = vFile.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val definition = element.findParentDefinition()
        if (definition == null) {
            val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
            val configContext = CwtConfigContext(element, fileInfo, elementPath, gameType, configGroup)
            return configContext
        } else {
            val definitionInfo = definition.definitionInfo ?: return null
            val definitionElementPath = definitionInfo.elementPath
            val elementPathFromRoot = definitionElementPath.relativeTo(elementPath) ?: return null
            val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
            val configContext = CwtConfigContext(element, fileInfo, elementPath, gameType, configGroup)
            configContext.definitionInfo = definitionInfo
            configContext.elementPathFromRoot = elementPathFromRoot
            return configContext
        }
    }

    override fun getCacheKey(context: CwtConfigContext, matchOptions: Int): String? {
        val gameTypeId = context.gameType.id
        val definitionInfo = context.definitionInfo ?: return null
        val declarationConfig = definitionInfo.getDeclaration(matchOptions) ?: return null
        val declarationConfigCacheKey = declarationConfig.declarationConfigCacheKey ?: return null // null -> unexpected
        val declarationKey = declarationConfigCacheKey.replace("$gameTypeId#", "")
        val elementPathFromRoot = context.elementPathFromRoot ?: return null // null -> unexpected
        val contextElement = context.element
        val isPropertyValue = contextElement is ParadoxScriptValue && contextElement.isPropertyValue()
        return "b@$gameTypeId#${matchOptions}#${isPropertyValue.toInt()}#${declarationKey}:${elementPathFromRoot}"
    }

    override fun getConfigs(context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()
        val elementPathFromRoot = context.elementPathFromRoot ?: return null
        val definitionInfo = context.definitionInfo ?: return null
        val declarationConfig = definitionInfo.getDeclaration(matchOptions) ?: return null
        val rootConfigs = declarationConfig.singleton.list()
        val configGroup = context.configGroup
        val contextElement = context.element
        return ParadoxExpressionManager.getConfigsForConfigContext(contextElement, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
    }
}

/**
 * 用于获取内联脚本调用中的CWT规则上下文。
 *
 * * 正常提供代码高亮、引用解析、代码补全等高级语言功能。
 */
class InlineScriptUsageCwtConfigContextProvider : CwtConfigContextProvider {
    //注意：内联脚本调用可以在定义声明之外

    override fun getContext(element: ParadoxScriptMemberElement, elementPath: ParadoxExpressionPath, file: PsiFile): CwtConfigContext? {
        ProgressManager.checkCanceled()
        val vFile = selectFile(file) ?: return null

        //要求当前位置相对于文件的表达式路径中包含子路径"inline_script"
        val rootIndex = elementPath.indexOfFirst { it.equals(ParadoxInlineScriptManager.inlineScriptKey, true) }
        if (rootIndex == -1) return null

        val gameType = selectGameType(file) ?: return null
        val fileInfo = vFile.fileInfo //注意这里的fileInfo可能为null，例如，在内联脚本参数的多行参数值中
        val elementPathFromRoot = ParadoxExpressionPath.resolve(elementPath.originalSubPaths.let { it.subList(rootIndex + 1, it.size) })
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
        val configContext = CwtConfigContext(element, fileInfo, elementPath, gameType, configGroup)
        configContext.elementPathFromRoot = elementPathFromRoot
        return configContext
    }

    override fun getCacheKey(context: CwtConfigContext, matchOptions: Int): String? {
        val gameTypeId = context.gameType.id
        val elementPathFromRoot = context.elementPathFromRoot ?: return null // null -> unexpected
        val contextElement = context.element
        val isPropertyValue = contextElement is ParadoxScriptValue && contextElement.isPropertyValue()
        return "isu@$gameTypeId#${matchOptions}#${isPropertyValue.toInt()}:${elementPathFromRoot}"
    }

    override fun getConfigs(context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        val elementPathFromRoot = context.elementPathFromRoot ?: return null
        val configGroup = context.configGroup
        val inlineConfigs = configGroup.inlineConfigGroup[ParadoxInlineScriptManager.inlineScriptKey] ?: return null
        val contextElement = context.element
        val rootConfigs = inlineConfigs.map { it.inline() }
        return ParadoxExpressionManager.getConfigsForConfigContext(contextElement, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
    }
}

/**
 * 用于获取内联脚本文件中的CWT规则上下文。
 *
 * * 正常提供代码高亮、引用解析、代码补全等高级语言功能。
 * * 对于顶级成员，禁用以下代码检查：`MissingExpressionInspection`和`TooManyExpressionInspection`。
 * * 会将内联脚本内容内联到对应的调用处，然后再进行相关代码检查。
 */
class InlineScriptCwtConfigContextProvider : CwtConfigContextProvider {
    //注意：内联脚本调用可以在定义声明之外

    //TODO 1.1.0+ 支持解析内联脚本文件中的定义声明

    //首先推断内联脚本文件的CWT规则上下文：汇总内联脚本调用处的上下文，然后合并得到最终的CWT规则上下文
    //然后再得到当前位置的CWT规则上下文

    override fun getContext(element: ParadoxScriptMemberElement, elementPath: ParadoxExpressionPath, file: PsiFile): CwtConfigContext? {
        ProgressManager.checkCanceled()

        val vFile = selectFile(file) ?: return null
        if (PlsVfsManager.isInjectedFile(vFile)) return null //ignored for injected psi

        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(vFile)
        if (inlineScriptExpression == null) return null

        val gameType = selectGameType(file) ?: return null
        val fileInfo = vFile.fileInfo ?: return null
        val elementPathFromRoot = elementPath
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
        val configContext = CwtConfigContext(element, fileInfo, elementPath, gameType, configGroup)
        if (elementPathFromRoot.isNotEmpty()) {
            // 这里不再强制依赖基于文件的 rootConfigContext，由 usages/扩展规则合并得到
            configContext.inlineScriptRootConfigContext = ParadoxExpressionManager.getConfigContext(file)
        }
        configContext.inlineScriptExpression = inlineScriptExpression
        configContext.elementPathFromRoot = elementPathFromRoot
        return configContext
    }

    override fun getCacheKey(context: CwtConfigContext, matchOptions: Int): String? {
        val gameTypeId = context.gameType.id
        val inlineScriptExpression = context.inlineScriptExpression ?: return null // null -> unexpected
        val elementPathFromRoot = context.elementPathFromRoot ?: return null // null -> unexpected
        val contextElement = context.element
        val isPropertyValue = contextElement is ParadoxScriptValue && contextElement.isPropertyValue()
        return "is@$gameTypeId#${matchOptions}#${isPropertyValue.toInt()}#${inlineScriptExpression}:${elementPathFromRoot}"
    }

    //获取CWT规则后才能确定是否存在冲突以及是否存在递归

    override fun getConfigs(context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()
        val elementPathFromRoot = context.elementPathFromRoot ?: return null
        val contextElement = context.element
        val configGroup = context.configGroup

        // 统一使用“基于 usages 合并得到的 rootConfigs”，再据此下钻
        val inlineScriptExpression = context.inlineScriptExpression ?: return null
        val rootConfigs = ParadoxInlineScriptManager.getInferredContextConfigs(contextElement, inlineScriptExpression, context, matchOptions)
        // 如果既没有扩展规则也没有任何使用处，则不推断上下文（视为普通脚本文件，无 CWT 规则）
        if (rootConfigs.isEmpty()) return null
        if (elementPathFromRoot.isEmpty()) return rootConfigs
        return ParadoxExpressionManager.getConfigsForConfigContext(contextElement, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
    }

    //skip MissingExpressionInspection and TooManyExpressionInspection at root level

    override fun skipMissingExpressionCheck(context: CwtConfigContext): Boolean {
        val elementPathFromRoot = context.elementPathFromRoot ?: return false
        return elementPathFromRoot.isEmpty()
    }

    override fun skipTooManyExpressionCheck(context: CwtConfigContext): Boolean {
        val elementPathFromRoot = context.elementPathFromRoot ?: return false
        return elementPathFromRoot.isEmpty()
    }
}

/**
 * 用于获取脚本参数的传入值和默认值中的CWT规则上下文。
 *
 * * 基于语言注入功能实现。
 * * 正常提供代码高亮、引用解析、代码补全等高级语言功能。
 * * 对于由引号括起（且允许由引号括起）的传入值，允许使用整行或多行脚本片段，而非单个值。
 * * 对于顶级成员，禁用以下代码检查：`MissingExpressionInspection`和`TooManyExpressionInspection`。
 * * 不会将参数值内容内联到对应的调用处，然后再进行相关代码检查。
 * * 不会将参数值内容内联到对应的调用处，然后检查语法是否合法。
 *
 * @see icu.windea.pls.lang.injection.ParadoxScriptLanguageInjector
 */
class ParameterValueCwtConfigContextProvider : CwtConfigContextProvider {
    override fun getContext(element: ParadoxScriptMemberElement, elementPath: ParadoxExpressionPath, file: PsiFile): CwtConfigContext? {
        ProgressManager.checkCanceled()

        //兼容适用语言注入功能的 VirtualFileWindow
        //兼容通过编辑代码碎片的意图操作打开的 LightVirtualFile

        val injectionInfo = ParadoxParameterManager.getParameterValueInjectionInfoFromInjectedFile(file)
        if (injectionInfo == null) return null

        val gameType = selectGameType(file) ?: return null
        val parameterElement = injectionInfo.parameterElement ?: return null
        val elementPathFromRoot = elementPath
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
        val configContext = CwtConfigContext(element, null, elementPath, gameType, configGroup)
        if (elementPathFromRoot.isNotEmpty()) {
            configContext.parameterValueRootConfigContext = ParadoxExpressionManager.getConfigContext(file) ?: return null
        }
        configContext.parameterElement = parameterElement
        configContext.parameterValueQuoted = injectionInfo.parameterValueQuoted
        configContext.elementPathFromRoot = elementPathFromRoot
        return configContext
    }

    override fun getCacheKey(context: CwtConfigContext, matchOptions: Int): String? {
        val gameTypeId = context.gameType.id
        val parameterElement = context.parameterElement ?: return null // null -> unexpected
        val elementPathFromRoot = context.elementPathFromRoot ?: return null // null -> unexpected
        val contextElement = context.element
        val isPropertyValue = contextElement is ParadoxScriptValue && contextElement.isPropertyValue()
        return "is@$gameTypeId#${matchOptions}#${isPropertyValue.toInt()}#${parameterElement.contextKey}@${parameterElement.name}:${elementPathFromRoot}"
    }

    override fun getConfigs(context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()
        val elementPathFromRoot = context.elementPathFromRoot ?: return null
        val contextElement = context.element

        if (elementPathFromRoot.isNotEmpty()) {
            val rootConfigContext = context.parameterValueRootConfigContext ?: return null
            val rootConfigs = rootConfigContext.getConfigs(matchOptions)
            val configGroup = context.configGroup
            return ParadoxExpressionManager.getConfigsForConfigContext(contextElement, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
        }

        val parameterElement = context.parameterElement ?: return null
        return ParadoxParameterManager.getInferredContextConfigs(parameterElement)
    }

    //skip MissingExpressionInspection and TooManyExpressionInspection at root level

    override fun skipMissingExpressionCheck(context: CwtConfigContext): Boolean {
        val elementPathFromRoot = context.elementPathFromRoot ?: return false
        return elementPathFromRoot.isEmpty()
    }

    override fun skipTooManyExpressionCheck(context: CwtConfigContext): Boolean {
        val elementPathFromRoot = context.elementPathFromRoot ?: return false
        return elementPathFromRoot.isEmpty()
    }
}
