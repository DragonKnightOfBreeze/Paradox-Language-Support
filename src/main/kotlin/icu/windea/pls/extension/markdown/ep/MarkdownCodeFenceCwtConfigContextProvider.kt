package icu.windea.pls.extension.markdown.ep

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.configContext.*
import icu.windea.pls.extension.markdown.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

var CwtConfigContext.mdCodeFencePath: String? by createKey(CwtConfigContext.Keys)

/**
 * 用于获取Markdown代码块中的CWT规则上下文。
 *
 * 通过在语言ID声明之后添加 `path={gameType}:{filePath}` 来注入路径信息。
 */
class MarkdownCodeFenceCwtConfigContextProvider : CwtConfigContextProvider {
    // 类似 icu.windea.pls.ep.configContext.BaseCwtConfigContextProvider
    // 但是使用注入的 fileInfo

    override fun getContext(element: ParadoxScriptMemberElement, elementPath: ParadoxExpressionPath, file: PsiFile): CwtConfigContext? {
        ProgressManager.checkCanceled()

        val codeFence = PlsMarkdownManager.getCodeFenceFromInjectedFile(file)
        if (codeFence == null) return null

        val pathInfo = PlsMarkdownManager.getPathInfo(codeFence)
        val fileInfo = PlsMarkdownManager.injectFileInfoToInjectedFile(file, codeFence, pathInfo)
        if (pathInfo == null || fileInfo == null) return null

        val gameType = fileInfo.rootInfo.gameType
        val definition = element.findParentDefinition()
        if (definition == null) {
            val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
            val configContext = CwtConfigContext(element, fileInfo, elementPath, gameType, configGroup)
            configContext.elementPathFromRoot = ParadoxExpressionPath.Empty
            configContext.mdCodeFencePath = pathInfo.path
            return configContext
        } else {
            val definitionInfo = definition.definitionInfo ?: return null
            val definitionElementPath = definitionInfo.elementPath
            val elementPathFromRoot = definitionElementPath.relativeTo(elementPath) ?: return null
            val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
            val configContext = CwtConfigContext(element, fileInfo, elementPath, gameType, configGroup)
            configContext.definitionInfo = definitionInfo
            configContext.elementPathFromRoot = elementPathFromRoot
            configContext.mdCodeFencePath = pathInfo.path
            return configContext
        }
    }

    override fun getCacheKey(context: CwtConfigContext, matchOptions: Int): String? {
        val gameTypeId = context.gameType.id
        val path = context.mdCodeFencePath ?: return null // null -> unexpected
        val elementPathFromRoot = context.elementPathFromRoot ?: return null // null -> unexpected
        val contextElement = context.element
        val isPropertyValue = contextElement is ParadoxScriptValue && contextElement.isPropertyValue()
        return "md@$gameTypeId:${matchOptions}#${isPropertyValue.toInt()}#${path}:${elementPathFromRoot}"
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
