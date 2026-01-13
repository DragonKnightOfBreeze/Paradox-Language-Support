package icu.windea.pls.ep.configContext

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.declarationConfigCacheKey
import icu.windea.pls.config.configContext.CwtConfigContext
import icu.windea.pls.config.configContext.definitionInfo
import icu.windea.pls.config.configContext.definitionInjectionInfo
import icu.windea.pls.config.configContext.inlineScriptExpression
import icu.windea.pls.config.configContext.memberPathFromRoot
import icu.windea.pls.config.configContext.parameterElement
import icu.windea.pls.config.configContext.parameterValueQuoted
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.toInt
import icu.windea.pls.core.util.list
import icu.windea.pls.core.util.singleton
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.injection.ParadoxScriptInjectionManager
import icu.windea.pls.lang.psi.select.parentDefinitionOld
import icu.windea.pls.lang.psi.select.parentDefinitionInjectionOld
import icu.windea.pls.lang.psi.select.select
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.model.paths.relativeTo
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isPropertyValue

/**
 * 提供基础的规则上下文。
 *
 * - 基于文件信息（包括注入的文件信息）和成员路径。
 * - 不提供上下文规则。
 * - TODO 2.1.0+ 在以后的插件版本中，可能会提供顶级键（如 `spriteTypes`）对应的合成的上下文规则。
 */
class CwtBaseConfigContextProvider : CwtConfigContextProvider {
    override fun getContext(element: ParadoxScriptMember, memberPath: ParadoxMemberPath, file: PsiFile): CwtConfigContext? {
        ProgressManager.checkCanceled()

        val vFile = selectFile(file) ?: return null
        val fileInfo = vFile.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
        val configContext = CwtConfigContext(element, fileInfo, memberPath, gameType, configGroup)
        return configContext
    }

    override fun getCacheKey(context: CwtConfigContext, matchOptions: Int): String? {
        return null
    }

    override fun getConfigs(context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        return null
    }
}

/**
 * 提供定义声明中的规则上下文。
 *
 * - 基于文件信息（包括注入的文件信息）和成员路径。
 */
class CwtDefinitionConfigContextProvider : CwtConfigContextProvider {
    override fun getContext(element: ParadoxScriptMember, memberPath: ParadoxMemberPath, file: PsiFile): CwtConfigContext? {
        ProgressManager.checkCanceled()

        val vFile = selectFile(file) ?: return null
        val fileInfo = vFile.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val definition = element.select { parentDefinitionOld() } ?: return null
        val definitionInfo = definition.definitionInfo ?: return null
        val definitionMemberPath = definitionInfo.memberPath
        val memberPathFromRoot = definitionMemberPath.relativeTo(memberPath) ?: return null
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
        val configContext = CwtConfigContext(element, fileInfo, memberPath.normalize(), gameType, configGroup)
        configContext.definitionInfo = definitionInfo
        configContext.memberPathFromRoot = memberPathFromRoot.normalize()
        return configContext
    }

    override fun getCacheKey(context: CwtConfigContext, matchOptions: Int): String? {
        val gameTypeId = context.gameType.id
        val definitionInfo = context.definitionInfo ?: return null
        val declarationConfig = definitionInfo.getDeclaration(matchOptions) ?: return null // TODO 2.0.6+ 这里存在一定的耗时，需要考虑优化
        val declarationConfigCacheKey = declarationConfig.declarationConfigCacheKey ?: return null // null -> unexpected
        val declarationKey = declarationConfigCacheKey.replace("$gameTypeId#", "")
        val memberPathFromRoot = context.memberPathFromRoot ?: return null // null -> unexpected
        val contextElement = context.element
        val isPropertyValue = contextElement is ParadoxScriptValue && contextElement.isPropertyValue()
        return "b@$gameTypeId#${matchOptions}#${isPropertyValue.toInt()}#${declarationKey}:${memberPathFromRoot}"
    }

    override fun getConfigs(context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()

        val memberPathFromRoot = context.memberPathFromRoot ?: return null
        val definitionInfo = context.definitionInfo ?: return null
        val declarationConfig = definitionInfo.getDeclaration(matchOptions) ?: return null
        val rootConfigs = declarationConfig.singleton.list()
        val configGroup = context.configGroup
        val contextElement = context.element
        val configs = ParadoxExpressionManager.getConfigsForConfigContext(contextElement, rootConfigs, memberPathFromRoot, configGroup, matchOptions)
        return configs
    }
}

/**
 * 提供内联脚本参数的传入值和默认值中的规则上下文。
 *
 * - 基于语言注入功能实现。
 * - 对于由引号括起（且允许由引号括起）的传入值，允许使用整行或多行脚本片段，而非单个值。
 * - 对于顶级成员，禁用以下代码检查：`MissingExpressionInspection`、`TooManyExpressionInspection`。
 * - 不会将参数值内容内联到对应的调用处，然后再进行相关代码检查。
 * - 不会将参数值内容内联到对应的调用处，然后检查语法是否合法。
 *
 * @see icu.windea.pls.lang.injection.ParadoxScriptLanguageInjector
 */
class CwtParameterValueConfigContextProvider : CwtConfigContextProvider {
    override fun getContext(element: ParadoxScriptMember, memberPath: ParadoxMemberPath, file: PsiFile): CwtConfigContext? {
        ProgressManager.checkCanceled()

        // 兼容适用语言注入功能的 VirtualFileWindow
        // 兼容通过编辑代码碎片的意图操作打开的 LightVirtualFile

        val injectionInfo = ParadoxScriptInjectionManager.getParameterValueInjectionInfoFromInjectedFile(file)
        if (injectionInfo == null) return null

        val gameType = selectGameType(file) ?: return null
        val parameterElement = injectionInfo.parameterElement ?: return null
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
        val configContext = CwtConfigContext(element, null, memberPath.normalize(), gameType, configGroup)
        configContext.parameterElement = parameterElement
        configContext.parameterValueQuoted = injectionInfo.parameterValueQuoted
        configContext.memberPathFromRoot = configContext.memberPath
        return configContext
    }

    override fun getCacheKey(context: CwtConfigContext, matchOptions: Int): String? {
        val gameTypeId = context.gameType.id
        val parameterElement = context.parameterElement ?: return null // null -> unexpected
        val memberPathFromRoot = context.memberPathFromRoot ?: return null // null -> unexpected
        val contextElement = context.element
        val isPropertyValue = contextElement is ParadoxScriptValue && contextElement.isPropertyValue()
        return "is@$gameTypeId#${matchOptions}#${isPropertyValue.toInt()}#${parameterElement.contextKey}@${parameterElement.name}:${memberPathFromRoot}"
    }

    override fun getConfigs(context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()

        val parameterElement = context.parameterElement ?: return null
        val memberPathFromRoot = context.memberPathFromRoot ?: return null
        val contextElement = context.element
        val configGroup = context.configGroup
        val rootConfigs = ParadoxParameterManager.getInferredContextConfigs(parameterElement)
        if (rootConfigs.isEmpty()) return emptyList()
        if (memberPathFromRoot.isEmpty()) return rootConfigs
        val configs = ParadoxExpressionManager.getConfigsForConfigContext(contextElement, rootConfigs, memberPathFromRoot, configGroup, matchOptions)
        return configs
    }

    // skip MissingExpressionInspection and TooManyExpressionInspection at root level

    override fun skipMissingExpressionCheck(context: CwtConfigContext): Boolean {
        val memberPathFromRoot = context.memberPathFromRoot ?: return false
        return memberPathFromRoot.isEmpty()
    }

    override fun skipTooManyExpressionCheck(context: CwtConfigContext): Boolean {
        val memberPathFromRoot = context.memberPathFromRoot ?: return false
        return memberPathFromRoot.isEmpty()
    }
}

/**
 * 提供内联脚本用法中的规则上下文。
 */
class CwtInlineScriptUsageConfigContextProvider : CwtConfigContextProvider {
    // 注意：内联脚本用法可以在定义声明之外

    override fun getContext(element: ParadoxScriptMember, memberPath: ParadoxMemberPath, file: PsiFile): CwtConfigContext? {
        ProgressManager.checkCanceled()

        val vFile = selectFile(file) ?: return null
        val gameType = selectGameType(file) ?: return null
        if (!ParadoxInlineScriptManager.isSupported(gameType)) return null

        // 要求当前位置相对于文件的成员路径中包含子路径 `inline_script`
        val rootIndex = memberPath.indexOfFirst { ParadoxInlineScriptManager.isMatched(it) }
        if (rootIndex == -1) return null

        val fileInfo = vFile.fileInfo // 注意这里的 fileInfo 可以为 null（例如，在内联脚本参数的多行参数值中）
        val memberPathFromRoot = ParadoxMemberPath.resolve(memberPath.subPaths.drop(rootIndex + 1))
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
        val configContext = CwtConfigContext(element, fileInfo, memberPath.normalize(), gameType, configGroup)
        configContext.memberPathFromRoot = memberPathFromRoot.normalize()
        return configContext
    }

    override fun getCacheKey(context: CwtConfigContext, matchOptions: Int): String? {
        val gameTypeId = context.gameType.id
        val memberPathFromRoot = context.memberPathFromRoot ?: return null // null -> unexpected
        val contextElement = context.element
        val isPropertyValue = contextElement is ParadoxScriptValue && contextElement.isPropertyValue()
        return "isu@$gameTypeId#${matchOptions}#${isPropertyValue.toInt()}:${memberPathFromRoot}"
    }

    override fun getConfigs(context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        val memberPathFromRoot = context.memberPathFromRoot ?: return null
        val configGroup = context.configGroup
        val inlineConfigs = configGroup.directivesModel.inlineScript.orNull() ?: return null
        val contextElement = context.element
        val rootConfigs = inlineConfigs.map { CwtConfigManipulator.inline(it) }
        val configs = ParadoxExpressionManager.getConfigsForConfigContext(contextElement, rootConfigs, memberPathFromRoot, configGroup, matchOptions)
        return configs
    }
}

/**
 * 提供内联脚本文件中的规则上下文。
 *
 * - 对于顶级成员，禁用以下代码检查：`MissingExpressionInspection`、`TooManyExpressionInspection`。
 * - 会将内联脚本内容内联到对应的调用处，然后再进行相关代码检查。
 */
class CwtInlineScriptConfigContextProvider : CwtConfigContextProvider {
    // TODO 1.1.0+ 支持解析内联脚本文件中的定义声明

    override fun getContext(element: ParadoxScriptMember, memberPath: ParadoxMemberPath, file: PsiFile): CwtConfigContext? {
        ProgressManager.checkCanceled()

        val vFile = selectFile(file) ?: return null
        if (PlsFileManager.isInjectedFile(vFile)) return null // ignored for injected psi

        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(vFile)
        if (inlineScriptExpression == null) return null

        val gameType = selectGameType(file) ?: return null
        val fileInfo = vFile.fileInfo ?: return null
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
        val configContext = CwtConfigContext(element, fileInfo, memberPath.normalize(), gameType, configGroup)
        configContext.inlineScriptExpression = inlineScriptExpression
        configContext.memberPathFromRoot = configContext.memberPath
        return configContext
    }

    override fun getCacheKey(context: CwtConfigContext, matchOptions: Int): String? {
        val gameTypeId = context.gameType.id
        val inlineScriptExpression = context.inlineScriptExpression ?: return null // null -> unexpected
        val memberPathFromRoot = context.memberPathFromRoot ?: return null // null -> unexpected
        val contextElement = context.element
        val isPropertyValue = contextElement is ParadoxScriptValue && contextElement.isPropertyValue()
        return "is@$gameTypeId#${matchOptions}#${isPropertyValue.toInt()}#${inlineScriptExpression}:${memberPathFromRoot}"
    }

    override fun getConfigs(context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()

        // 获取上下文规则后才能确定是否存在冲突以及是否存在递归

        val memberPathFromRoot = context.memberPathFromRoot ?: return null
        val inlineScriptExpression = context.inlineScriptExpression ?: return null
        val contextElement = context.element
        val configGroup = context.configGroup
        val rootConfigs = ParadoxInlineScriptManager.getInferredContextConfigs(inlineScriptExpression, contextElement, context, matchOptions)
        if (rootConfigs.isEmpty()) return emptyList()
        if (memberPathFromRoot.isEmpty()) return rootConfigs
        val configs = ParadoxExpressionManager.getConfigsForConfigContext(contextElement, rootConfigs, memberPathFromRoot, configGroup, matchOptions)
        return configs
    }

    // skip MissingExpressionInspection and TooManyExpressionInspection at root level

    override fun skipMissingExpressionCheck(context: CwtConfigContext): Boolean {
        val memberPathFromRoot = context.memberPathFromRoot ?: return false
        return memberPathFromRoot.isEmpty()
    }

    override fun skipTooManyExpressionCheck(context: CwtConfigContext): Boolean {
        val memberPathFromRoot = context.memberPathFromRoot ?: return false
        return memberPathFromRoot.isEmpty()
    }
}

/**
 * 提供定义注入中的规则上下文。
 *
 * - 基于文件信息（包括注入的文件信息）和成员路径。
 * - 对于顶级成员，禁用以下代码检查：`MissingExpressionInspection`、`TooManyExpressionInspection`。
 * - （目前）不会先内联目标定义声明中的内容，然后再进行相关代码检查。
 */
class CwtDefinitionInjectionConfigContextProvider : CwtConfigContextProvider {
    override fun getContext(element: ParadoxScriptMember, memberPath: ParadoxMemberPath, file: PsiFile): CwtConfigContext? {
        ProgressManager.checkCanceled()

        val vFile = selectFile(file) ?: return null
        val fileInfo = vFile.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        if (memberPath.isEmpty()) return null
        if (!ParadoxDefinitionInjectionManager.isSupported(gameType)) return null // 忽略游戏类型不支持的情况
        val definitionInjection = element.select { parentDefinitionInjectionOld() } ?: return null
        val definitionInjectionInfo = definitionInjection.definitionInjectionInfo ?: return null
        val memberPathFromRoot = ParadoxMemberPath.resolve(memberPath.subPaths.drop(1)) // 去除第一个子路径
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
        val configContext = CwtConfigContext(element, fileInfo, memberPath.normalize(), gameType, configGroup)
        configContext.definitionInjectionInfo = definitionInjectionInfo
        configContext.memberPathFromRoot = memberPathFromRoot.normalize()
        return configContext
    }

    override fun getCacheKey(context: CwtConfigContext, matchOptions: Int): String? {
        val gameTypeId = context.gameType.id
        val definitionInjectionInfo = context.definitionInjectionInfo ?: return null
        val targetKey = definitionInjectionInfo.type + "@" + definitionInjectionInfo.target
        val memberPathFromRoot = context.memberPathFromRoot ?: return null // null -> unexpected
        val contextElement = context.element
        val isPropertyValue = contextElement is ParadoxScriptValue && contextElement.isPropertyValue()
        return "b@$gameTypeId#${matchOptions}#${isPropertyValue.toInt()}#${targetKey}:${memberPathFromRoot}"
    }

    override fun getConfigs(context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()

        val memberPathFromRoot = context.memberPathFromRoot ?: return null
        val definitionInjectionInfo = context.definitionInjectionInfo ?: return null
        val contextElement = context.element
        val declarationConfig = ParadoxDefinitionInjectionManager.getDeclaration(contextElement, definitionInjectionInfo) ?: return null
        val rootConfigs = declarationConfig.singleton.list()
        val configGroup = context.configGroup
        val configs = ParadoxExpressionManager.getConfigsForConfigContext(contextElement, rootConfigs, memberPathFromRoot, configGroup, matchOptions)
        return configs
    }
}
