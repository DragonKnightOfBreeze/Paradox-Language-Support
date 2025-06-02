package icu.windea.pls.ep.configContext

import com.intellij.lang.injection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.injection.*
import icu.windea.pls.script.psi.*

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
 */
class CwtBaseConfigContextProvider : CwtConfigContextProvider {
    override fun getContext(element: ParadoxScriptMemberElement, elementPath: ParadoxExpressionPath, file: PsiFile): CwtConfigContext? {
        ProgressManager.checkCanceled()
        val vFile = selectFile(file) ?: return null
        if (ParadoxFileManager.isInjectedFile(vFile)) return null //ignored for injected psi

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
        val elementPathFromRoot = context.elementPathFromRoot ?: return null // null -> unexpected
        val contextElement = context.element
        val isPropertyValue = contextElement is ParadoxScriptValue && contextElement.isPropertyValue()
        return "b@$gameTypeId:${matchOptions}#${isPropertyValue.toInt()}#${declarationConfigCacheKey.substringAfterLast('#')}:${elementPathFromRoot}"
    }

    override fun getConfigs(context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()
        val elementPathFromRoot = context.elementPathFromRoot ?: return null
        val definitionInfo = context.definitionInfo ?: return null
        val declarationConfig = definitionInfo.getDeclaration(matchOptions) ?: return null
        val rootConfigs = declarationConfig.toSingletonList()
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
class CwtInlineScriptUsageConfigContextProvider : CwtConfigContextProvider {
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
        return "isu@$gameTypeId:${matchOptions}#${isPropertyValue.toInt()}:${elementPathFromRoot.path}"
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
 * 用于获取内联脚本中的CWT规则上下文。
 *
 * * 正常提供代码高亮、引用解析、代码补全等高级语言功能。
 * * 对于顶级成员，禁用以下代码检查：`MissingExpressionInspection`和`TooManyExpressionInspection`。
 * * 会将内联脚本内容内联到对应的调用处，然后再进行相关代码检查。
 */
class CwtInlineScriptConfigContextProvider : CwtConfigContextProvider {
    //注意：内联脚本调用可以在定义声明之外

    //TODO 1.1.0+ 支持解析内联脚本文件中的定义声明

    //首先推断内联脚本文件的CWT规则上下文：汇总内联脚本调用处的上下文，然后合并得到最终的CWT规则上下文
    //然后再得到当前位置的CWT规则上下文

    override fun getContext(element: ParadoxScriptMemberElement, elementPath: ParadoxExpressionPath, file: PsiFile): CwtConfigContext? {
        ProgressManager.checkCanceled()

        val vFile = selectFile(file) ?: return null
        if (ParadoxFileManager.isInjectedFile(vFile)) return null //ignored for injected psi

        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(vFile)
        if (inlineScriptExpression == null) return null

        val gameType = selectGameType(file) ?: return null
        val fileInfo = vFile.fileInfo ?: return null
        val elementPathFromRoot = elementPath
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
        val configContext = CwtConfigContext(element, fileInfo, elementPath, gameType, configGroup)
        if (elementPathFromRoot.isNotEmpty()) {
            configContext.inlineScriptRootConfigContext = ParadoxExpressionManager.getConfigContext(file) ?: return null
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
        return "is@$gameTypeId:${matchOptions}#${isPropertyValue.toInt()}#${inlineScriptExpression}:${elementPathFromRoot.path}"
    }

    //获取CWT规则后才能确定是否存在冲突以及是否存在递归

    override fun getConfigs(context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()
        val elementPathFromRoot = context.elementPathFromRoot ?: return null
        val contextElement = context.element

        if (elementPathFromRoot.isNotEmpty()) {
            val rootConfigContext = context.inlineScriptRootConfigContext ?: return null
            val rootConfigs = rootConfigContext.getConfigs(matchOptions)
            val configGroup = context.configGroup
            return ParadoxExpressionManager.getConfigsForConfigContext(contextElement, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
        }

        val inlineScriptExpression = context.inlineScriptExpression ?: return null
        return ParadoxInlineScriptManager.getInferredContextConfigs(contextElement, inlineScriptExpression, context, matchOptions)
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
 * @see ParadoxScriptLanguageInjector
 */
class CwtParameterValueConfigContextProvider : CwtConfigContextProvider {
    override fun getContext(element: ParadoxScriptMemberElement, elementPath: ParadoxExpressionPath, file: PsiFile): CwtConfigContext? {
        ProgressManager.checkCanceled()

        //兼容适用语言注入功能的 VirtualFileWindow
        //兼容通过编辑代码碎片的意图操作打开的 LightVirtualFile

        val vFile = selectFile(file) ?: return null
        if (!ParadoxFileManager.isInjectedFile(vFile)) return null
        val host = InjectedLanguageManager.getInstance(file.project).getInjectionHost(file)
        if (host == null) return null

        val file0 = vFile.toPsiFile(file.project) ?: file //actual PsiFile of VirtualFileWindow
        val injectionInfo = getInjectionInfo(file0, host)
        if (injectionInfo == null) return null

        val gameType = selectGameType(file) ?: return null
        val parameterElement = injectionInfo.parameterElement ?: return null
        val elementPathFromRoot = elementPath
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
        val configContext = CwtConfigContext(element, null, elementPath, gameType, configGroup)
        if (elementPathFromRoot.isNotEmpty()) {
            configContext.parameterValueRootConfigContext = ParadoxExpressionManager.getConfigContext(file) ?: return null
        }
        configContext.elementPathFromRoot = elementPathFromRoot
        configContext.parameterElement = parameterElement
        configContext.parameterValueQuoted = injectionInfo.parameterValueQuoted
        return configContext
    }

    private fun getInjectionInfo(file: PsiFile, host: PsiElement): ParadoxParameterValueInjectionInfo? {
        val injectionInfos = host.getUserData(PlsKeys.parameterValueInjectionInfos)
        if (injectionInfos.isNullOrEmpty()) return null
        return when {
            host is ParadoxScriptStringExpressionElement -> {
                val shreds = file.getShreds()
                val shred = shreds?.singleOrNull()
                val rangeInsideHost = shred?.rangeInsideHost ?: return null
                //it.rangeInsideHost may not equal to rangeInsideHost, but inside (e.g., there are escaped double quotes)
                injectionInfos.find { it.rangeInsideHost.startOffset in rangeInsideHost }
            }
            host is ParadoxParameter -> {
                //just use the only one
                injectionInfos.singleOrNull()
            }
            else -> null
        }
    }

    override fun getCacheKey(context: CwtConfigContext, matchOptions: Int): String? {
        val gameTypeId = context.gameType.id
        val parameterElement = context.parameterElement ?: return null // null -> unexpected
        val elementPathFromRoot = context.elementPathFromRoot ?: return null // null -> unexpected
        val contextElement = context.element
        val isPropertyValue = contextElement is ParadoxScriptValue && contextElement.isPropertyValue()
        return "is@$gameTypeId:${matchOptions}#${isPropertyValue.toInt()}#${parameterElement.contextKey}@${parameterElement.name}:${elementPathFromRoot.path}"
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
