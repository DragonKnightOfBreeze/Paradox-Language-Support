package icu.windea.pls.ep.resolve.config

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.declarationConfigCacheKey
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.util.values.singletonList
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.injection.ParadoxScriptInjectionManager
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.toHashString
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.resolve.CwtConfigContext
import icu.windea.pls.lang.resolve.ParadoxConfigService
import icu.windea.pls.lang.resolve.definitionInfo
import icu.windea.pls.lang.resolve.definitionInjectionInfo
import icu.windea.pls.lang.resolve.inlineScriptExpression
import icu.windea.pls.lang.resolve.parameterElement
import icu.windea.pls.lang.resolve.parameterValueQuoted
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.ParadoxMemberRole
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.model.paths.relativeTo
import icu.windea.pls.script.psi.ParadoxScriptMember

/**
 * 提供基础的规则上下文。
 *
 * - 基于文件信息（包括注入的文件信息）和成员路径。
 * - 不提供上下文规则。
 * - TODO 2.1.0+ 在以后的插件版本中，可能会提供顶级键（如 `spriteTypes`）对应的合成的上下文规则。
 */
class CwtBaseConfigContextProvider : CwtConfigContextProvider {
    override fun getContext(element: ParadoxScriptMember, file: PsiFile, configGroup: CwtConfigGroup, memberPathFromFile: ParadoxMemberPath, memberRole: ParadoxMemberRole): CwtConfigContext? {
        ProgressManager.checkCanceled()

        val vFile = selectFile(file)
        if (vFile == null) return null
        val fileInfo = vFile.fileInfo
        if (fileInfo == null) return null
        val configContext = CwtConfigContext(element, memberPathFromFile, null, memberRole, configGroup)
        return configContext
    }

    override fun getCacheKey(context: CwtConfigContext, options: ParadoxMatchOptions?): String? {
        return null
    }

    override fun getConfigs(context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>>? {
        return null
    }
}

/**
 * 提供定义声明中的规则上下文。
 *
 * - 基于文件信息（包括注入的文件信息）和成员路径。
 */
class CwtDefinitionConfigContextProvider : CwtConfigContextProvider {
    override fun getContext(element: ParadoxScriptMember, file: PsiFile, configGroup: CwtConfigGroup, memberPathFromFile: ParadoxMemberPath, memberRole: ParadoxMemberRole): CwtConfigContext? {
        ProgressManager.checkCanceled()

        val vFile = selectFile(file)
        if (vFile == null) return null
        val fileInfo = vFile.fileInfo
        if (fileInfo == null) return null
        val definition = selectScope { element.parentDefinition() } ?: return null
        val definitionInfo = definition.definitionInfo ?: return null
        val definitionMemberPath = definitionInfo.memberPath
        val memberPath = definitionMemberPath.relativeTo(memberPathFromFile)?.normalize() ?: return null
        val configContext = CwtConfigContext(element, memberPathFromFile, memberPath, memberRole, configGroup)
        configContext.definitionInfo = definitionInfo
        return configContext
    }

    override fun getCacheKey(context: CwtConfigContext, options: ParadoxMatchOptions?): String? {
        val gameTypeId = context.gameType.id
        val definitionInfo = context.definitionInfo ?: return null
        val declarationConfig = definitionInfo.getDeclaration(options) ?: return null // TODO 2.0.6+ 这里存在一定的耗时，需要考虑优化
        val declarationConfigCacheKey = declarationConfig.declarationConfigCacheKey ?: return null // null -> unexpected
        val declarationKey = declarationConfigCacheKey.replace("$gameTypeId#", "")
        val memberPath = context.memberPath ?: return null // null -> unexpected
        val memberRole = context.memberRole
        val suffix = "${memberPath}\u0000${memberRole.ordinal}\u0000${options.toHashString(forMatched = false)}"
        return "d@$gameTypeId#$declarationKey#$suffix"
    }

    override fun getConfigs(context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()

        val memberPath = context.memberPath ?: return null
        val definitionInfo = context.definitionInfo ?: return null
        if (memberPath.isNotEmpty()) return ParadoxConfigService.getFlattenedConfigsForConfigContext(context, options)
        val declarationConfig = definitionInfo.getDeclaration(options) ?: return null
        val rootConfigs = declarationConfig.to.singletonList()
        return ParadoxConfigService.getTopConfigsForConfigContext(context, rootConfigs)
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
    // 兼容适用语言注入功能的 `VirtualFileWindow`
    // 兼容通过编辑代码碎片的意图操作打开的 `LightVirtualFile`

    override fun getContext(element: ParadoxScriptMember, file: PsiFile, configGroup: CwtConfigGroup, memberPathFromFile: ParadoxMemberPath, memberRole: ParadoxMemberRole): CwtConfigContext? {
        ProgressManager.checkCanceled()

        val injectionInfo = ParadoxScriptInjectionManager.getParameterValueInjectionInfoFromInjectedFile(file) ?: return null
        val parameterElement = injectionInfo.parameterElement ?: return null
        val configContext = CwtConfigContext(element, memberPathFromFile, memberPathFromFile, memberRole, configGroup)
        configContext.parameterElement = parameterElement
        configContext.parameterValueQuoted = injectionInfo.parameterValueQuoted
        return configContext
    }

    override fun getCacheKey(context: CwtConfigContext, options: ParadoxMatchOptions?): String? {
        val gameTypeId = context.gameType.id
        val parameterElement = context.parameterElement ?: return null // null -> unexpected
        val memberPath = context.memberPath ?: return null // null -> unexpected
        val memberRole = context.memberRole
        val suffix = "${memberPath}\u0000${memberRole.ordinal}\u0000${options.toHashString(forMatched = false)}"
        return "pv@$gameTypeId#${parameterElement.contextKey}#${parameterElement.name}#$suffix"
    }

    override fun getConfigs(context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()

        val parameterElement = context.parameterElement ?: return null
        val memberPath = context.memberPath ?: return null
        if (memberPath.isNotEmpty()) return ParadoxConfigService.getFlattenedConfigsForConfigContext(context, options)
        val rootConfigs = ParadoxParameterManager.getInferredContextConfigs(parameterElement)
        return ParadoxConfigService.getTopConfigsForConfigContext(context, rootConfigs)
    }

    // skip `MissingExpressionInspection` and `TooManyExpressionInspection` at root level

    override fun skipMissingExpressionCheck(context: CwtConfigContext): Boolean {
        val memberPath = context.memberPath ?: return false
        return memberPath.isEmpty()
    }

    override fun skipTooManyExpressionCheck(context: CwtConfigContext): Boolean {
        val memberPath = context.memberPath ?: return false
        return memberPath.isEmpty()
    }
}

/**
 * 提供内联脚本用法中的规则上下文。
 */
class CwtInlineScriptUsageConfigContextProvider : CwtConfigContextProvider {
    // 注意：内联脚本用法可以在定义声明之外
    // 注意这里的 `fileInfo` 可以为 `null`（例如，在内联脚本参数的多行参数值中）

    override fun getContext(element: ParadoxScriptMember, file: PsiFile, configGroup: CwtConfigGroup, memberPathFromFile: ParadoxMemberPath, memberRole: ParadoxMemberRole): CwtConfigContext? {
        ProgressManager.checkCanceled()

        if (memberPathFromFile.none { ParadoxInlineScriptManager.isMatched(it) }) return null // 要求当前位置相对于文件的成员路径中包含子路径 `inline_script`
        if (!ParadoxInlineScriptManager.isSupported(configGroup.gameType)) return null // 忽略游戏类型不支持的情况
        val vFile = selectFile(file)
        if (vFile == null) return null
        val memberPath = ParadoxMemberPath.resolve(memberPathFromFile.subPaths.drop(memberPathFromFile.indexOfFirst { ParadoxInlineScriptManager.isMatched(it) } + 1)).normalize()
        val configContext = CwtConfigContext(element, memberPathFromFile, memberPath, memberRole, configGroup)
        return configContext
    }

    override fun getCacheKey(context: CwtConfigContext, options: ParadoxMatchOptions?): String? {
        val gameTypeId = context.gameType.id
        val memberPath = context.memberPath ?: return null // null -> unexpected
        val memberRole = context.memberRole
        val suffix = "${memberPath}\u0000${memberRole.ordinal}\u0000${options.toHashString(forMatched = false)}"
        return "isu@$gameTypeId#$suffix"
    }

    override fun getConfigs(context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>>? {
        val memberPath = context.memberPath ?: return null
        val inlineConfigs = context.configGroup.directivesModel.inlineScript.orNull() ?: return null
        if (memberPath.isNotEmpty()) return ParadoxConfigService.getFlattenedConfigsForConfigContext(context, options)
        val rootConfigs = inlineConfigs.map { CwtConfigManipulator.inline(it) }
        return ParadoxConfigService.getTopConfigsForConfigContext(context, rootConfigs)
    }
}

/**
 * 提供内联脚本文件中的规则上下文。
 *
 * - 对于顶级成员，禁用以下代码检查：`MissingExpressionInspection`、`TooManyExpressionInspection`。
 * - 会将内联脚本内容内联到对应的调用处，然后再进行相关代码检查。
 */
class CwtInlineScriptConfigContextProvider : CwtConfigContextProvider {
    // 获取上下文规则后才能确定是否存在冲突以及是否存在递归
    // TODO 1.1.0+ 支持解析内联脚本文件中的定义声明

    override fun getContext(element: ParadoxScriptMember, file: PsiFile, configGroup: CwtConfigGroup, memberPathFromFile: ParadoxMemberPath, memberRole: ParadoxMemberRole): CwtConfigContext? {
        ProgressManager.checkCanceled()

        val vFile = selectFile(file)
        if (vFile == null) return null
        if (PlsFileManager.isInjectedFile(vFile)) return null // ignored for injected psi
        val fileInfo = vFile.fileInfo
        if (fileInfo == null) return null
        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(vFile) ?: return null
        val configContext = CwtConfigContext(element, memberPathFromFile, memberPathFromFile, memberRole, configGroup)
        configContext.inlineScriptExpression = inlineScriptExpression
        return configContext
    }

    override fun getCacheKey(context: CwtConfigContext, options: ParadoxMatchOptions?): String? {
        val gameTypeId = context.gameType.id
        val inlineScriptExpression = context.inlineScriptExpression ?: return null // null -> unexpected
        val memberPath = context.memberPath ?: return null // null -> unexpected
        val memberRole = context.memberRole
        val suffix = "${memberPath}\u0000${memberRole.ordinal}\u0000${options.toHashString(forMatched = false)}"
        return "is@$gameTypeId#$inlineScriptExpression#$suffix"
    }

    override fun getConfigs(context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()

        val memberPath = context.memberPath ?: return null
        val inlineScriptExpression = context.inlineScriptExpression ?: return null
        if (memberPath.isNotEmpty()) return ParadoxConfigService.getFlattenedConfigsForConfigContext(context, options)
        val rootConfigs = ParadoxInlineScriptManager.getInferredContextConfigs(inlineScriptExpression, context.element, context, options)
        return ParadoxConfigService.getTopConfigsForConfigContext(context, rootConfigs)
    }

    // skip `MissingExpressionInspection` and `TooManyExpressionInspection` at root level

    override fun skipMissingExpressionCheck(context: CwtConfigContext): Boolean {
        val memberPath = context.memberPath ?: return false
        return memberPath.isEmpty()
    }

    override fun skipTooManyExpressionCheck(context: CwtConfigContext): Boolean {
        val memberPath = context.memberPath ?: return false
        return memberPath.isEmpty()
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
    override fun getContext(element: ParadoxScriptMember, file: PsiFile, configGroup: CwtConfigGroup, memberPathFromFile: ParadoxMemberPath, memberRole: ParadoxMemberRole): CwtConfigContext? {
        ProgressManager.checkCanceled()

        if (memberPathFromFile.isEmpty()) return null

        val vFile = selectFile(file)
        if (vFile == null) return null
        val fileInfo = vFile.fileInfo
        if (fileInfo == null) return null
        if (!ParadoxDefinitionInjectionManager.isSupported(configGroup.gameType)) return null // 忽略游戏类型不支持的情况
        val definitionInjection = selectScope { element.parentDefinitionInjection() } ?: return null
        val definitionInjectionInfo = definitionInjection.definitionInjectionInfo ?: return null
        val memberPath = ParadoxMemberPath.resolve(memberPathFromFile.subPaths.drop(1)).normalize() // 去除第一个子路径
        val configContext = CwtConfigContext(element, memberPathFromFile, memberPath, memberRole, configGroup)
        configContext.definitionInjectionInfo = definitionInjectionInfo
        return configContext
    }

    override fun getCacheKey(context: CwtConfigContext, options: ParadoxMatchOptions?): String? {
        val gameTypeId = context.gameType.id
        val definitionInjectionInfo = context.definitionInjectionInfo ?: return null
        val targetKey = definitionInjectionInfo.type + "@" + definitionInjectionInfo.target
        val memberPath = context.memberPath ?: return null // null -> unexpected
        val memberRole = context.memberRole
        val suffix = "${memberPath}\u0000${memberRole.ordinal}\u0000${options.toHashString(forMatched = false)}"
        return "di@$gameTypeId#$targetKey#$suffix"
    }

    override fun getConfigs(context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()

        val memberPath = context.memberPath ?: return null
        val definitionInjectionInfo = context.definitionInjectionInfo ?: return null
        if (memberPath.isNotEmpty()) return ParadoxConfigService.getFlattenedConfigsForConfigContext(context, options)
        val declaration = definitionInjectionInfo.declaration ?: return null
        val rootConfigs = declaration.to.singletonList()
        return ParadoxConfigService.getTopConfigsForConfigContext(context, rootConfigs)
    }

    // skip `MissingExpressionInspection` and `TooManyExpressionInspection` at root level

    override fun skipMissingExpressionCheck(context: CwtConfigContext): Boolean {
        val memberPath = context.memberPath ?: return false
        return memberPath.isEmpty()
    }

    override fun skipTooManyExpressionCheck(context: CwtConfigContext): Boolean {
        val memberPath = context.memberPath ?: return false
        return memberPath.isEmpty()
    }
}
