package icu.windea.pls.ep.resolve.config

import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.declarationConfigCacheKey
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.manipulation.CwtConfigManipulationService
import icu.windea.pls.core.collections.dropFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.collections.noneFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.util.values.singletonList
import icu.windea.pls.core.util.values.to
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.defineVariableInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.injection.ParadoxScriptInjectionManager
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.toHashString
import icu.windea.pls.lang.resolve.CwtConfigContext
import icu.windea.pls.lang.resolve.ParadoxConfigService
import icu.windea.pls.lang.resolve.defineVariableInfo
import icu.windea.pls.lang.resolve.definitionInfo
import icu.windea.pls.lang.resolve.definitionInjectionInfo
import icu.windea.pls.lang.resolve.inlineScriptExpression
import icu.windea.pls.lang.resolve.parameterElement
import icu.windea.pls.lang.resolve.parameterValueQuoted
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.model.type.ParadoxMemberRole
import icu.windea.pls.script.psi.ParadoxScriptMember

/**
 * 提供基础的规则上下文。
 *
 * - 基于文件信息（包括注入的文件信息）和成员路径。
 * - 不提供上下文规则。
 * - TODO 2.1.0+ 在以后的插件版本中，可能会提供顶级键（如 `spriteTypes`）对应的合成的上下文规则。
 */
class CwtBaseConfigContextProvider : CwtConfigContextProvider {
    override fun getContext(configGroup: CwtConfigGroup, element: ParadoxScriptMember, file: PsiFile, memberRole: ParadoxMemberRole, memberPathFromFile: ParadoxMemberPath): CwtConfigContext? {
        val vFile = selectFile(file)
        if (vFile == null) return null
        val fileInfo = vFile.fileInfo
        if (fileInfo == null) return null
        val context = CwtConfigContext.create(configGroup, memberRole, memberPathFromFile, this)
        context.element = element // necessary
        context.rootFile = selectRootFile(file) // necessary
        return context
    }

    override fun getCacheKey(context: CwtConfigContext, options: ParadoxMatchOptions?) = null

    override fun getConfigs(context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> = emptyList()

    override fun skipMissingExpressionCheck(context: CwtConfigContext) = true

    override fun skipUnresolvedExpressionCheck(context: CwtConfigContext) = true

    override fun skipTooManyExpressionCheck(context: CwtConfigContext) = true
}

/**
 * 提供定义声明中的规则上下文。
 *
 * - 基于文件信息（包括注入的文件信息）和成员路径。
 */
class CwtDefinitionConfigContextProvider : CwtConfigContextProvider {
    override fun getContext(configGroup: CwtConfigGroup, element: ParadoxScriptMember, file: PsiFile, memberRole: ParadoxMemberRole, memberPathFromFile: ParadoxMemberPath): CwtConfigContext? {
        val vFile = selectFile(file)
        if (vFile == null) return null
        val fileInfo = vFile.fileInfo
        if (fileInfo == null) return null
        val definition = selectScope { element.parentDefinition() } ?: return null
        val definitionInfo = definition.definitionInfo ?: return null
        val memberPath = definitionInfo.memberPath.relativize(memberPathFromFile) ?: return null
        val context = CwtConfigContext.createFromMember(configGroup, memberRole, memberPathFromFile, memberPath, this)
        context.element = element // necessary
        context.rootFile = selectRootFile(file) // necessary
        context.definitionInfo = definitionInfo
        return context
    }

    override fun getCacheKey(context: CwtConfigContext, options: ParadoxMatchOptions?): String? {
        val gameType = context.gameType
        val memberRole = context.memberRole
        val memberPath = context.memberPath ?: return null // null -> unexpected
        val definitionInfo = context.definitionInfo ?: return null
        // TODO 3.0.1+ [performance] consider optimization (relatively slow during indexing)
        val declarationConfig = definitionInfo.getDeclaration(options) ?: return null
        val declarationConfigCacheKey = declarationConfig.declarationConfigCacheKey ?: return null // null -> unexpected
        return buildString {
            append(gameType.ordinal)
            append("@d@")
            append(options.toHashString(forMatched = false))
            append(":")
            append(declarationConfigCacheKey)
            append("\u0000:")
            append(memberRole.ordinal)
            memberPath.subPaths.forEachFast { append("\u0000/").append(it) }
        }
    }

    override fun getConfigs(context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> {
        val memberPath = context.memberPath ?: return emptyList()
        if (memberPath.isNotEmpty()) return ParadoxConfigService.getFlattenedConfigsForConfigContext(context, options)
        val definitionInfo = context.definitionInfo ?: return emptyList()
        val declarationConfig = definitionInfo.getDeclaration(options) ?: return emptyList()
        val rootConfigs = declarationConfig.to.singletonList()
        return ParadoxConfigService.getTopConfigsForConfigContext(context, rootConfigs)
    }

    override fun skipUnresolvedExpressionCheck(context: CwtConfigContext): Boolean {
        // skip for root key
        return context.isDeclarationRoot() && context.memberRole == ParadoxMemberRole.Property
    }
}

/**
 * 提供定值变量声明中的规则上下文。
 */
class CwtDefineVariableConfigContextProvider : CwtConfigContextProvider {
    override fun getContext(configGroup: CwtConfigGroup, element: ParadoxScriptMember, file: PsiFile, memberRole: ParadoxMemberRole, memberPathFromFile: ParadoxMemberPath): CwtConfigContext? {
        if (!ParadoxDefineManager.isDefinesFile(file)) return null
        if (memberPathFromFile.length <= 1) return null // file level or top property level -> not within define variable
        val defineVariable = selectScope { element.parentDefineVariable() } ?: return null
        val defineVariableInfo = defineVariable.defineVariableInfo ?: return null
        if (defineVariableInfo.config == null) return null // no define variable config -> skip
        val memberPath = ParadoxMemberPath.resolve(memberPathFromFile.subPaths.dropFast(2))
        val context = CwtConfigContext.createFromMember(configGroup, memberRole, memberPathFromFile, memberPath, this)
        context.element = element // necessary
        context.rootFile = selectRootFile(file) // necessary
        context.defineVariableInfo = defineVariableInfo
        return context
    }

    override fun getCacheKey(context: CwtConfigContext, options: ParadoxMatchOptions?): String? {
        val gameType = context.gameType
        val memberRole = context.memberRole
        val memberPath = context.memberPath ?: return null // null -> unexpected
        val defineVariableInfo = context.defineVariableInfo ?: return null
        return buildString {
            append(gameType.ordinal)
            append("@dv@")
            append(options.toHashString(forMatched = false))
            append(":")
            append(defineVariableInfo.namespace).append("\u0000.").append(defineVariableInfo.variable)
            append("\u0000:")
            append(memberRole.ordinal)
            memberPath.subPaths.forEachFast { append("\u0000/").append(it) }
        }
    }

    override fun getConfigs(context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> {
        val memberPath = context.memberPath ?: return emptyList()
        if (memberPath.isNotEmpty()) return ParadoxConfigService.getFlattenedConfigsForConfigContext(context, options)
        val defineVariableInfo = context.defineVariableInfo ?: return emptyList()
        val rootConfig = defineVariableInfo.config?.rootConfig ?: return emptyList() // NOTE 2.1.8 inline or deep copy ops should be unnecessary here
        val rootConfigs = listOf(rootConfig)
        return ParadoxConfigService.getTopConfigsForConfigContext(context, rootConfigs)
    }

    override fun skipUnresolvedExpressionCheck(context: CwtConfigContext): Boolean {
        // skip for root key (define variable name)
        return context.isDeclarationRoot() && context.memberRole == ParadoxMemberRole.Property
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

    override fun getContext(configGroup: CwtConfigGroup, element: ParadoxScriptMember, file: PsiFile, memberRole: ParadoxMemberRole, memberPathFromFile: ParadoxMemberPath): CwtConfigContext? {
        val injectionInfo = ParadoxScriptInjectionManager.getParameterValueInjectionInfoFromInjectedFile(file) ?: return null
        val parameterElement = injectionInfo.parameterElement ?: return null
        val context = CwtConfigContext.createFromFile(configGroup, memberRole, memberPathFromFile, this)
        context.element = element // necessary
        context.rootFile = selectRootFile(file) // necessary
        context.parameterElement = parameterElement
        context.parameterValueQuoted = injectionInfo.parameterValueQuoted
        return context
    }

    override fun getCacheKey(context: CwtConfigContext, options: ParadoxMatchOptions?): String? {
        val gameType = context.gameType
        val memberRole = context.memberRole
        val memberPath = context.memberPath ?: return null // null -> unexpected
        val parameterElement = context.parameterElement ?: return null // null -> unexpected
        return buildString {
            append(gameType.ordinal)
            append("@pv@")
            append(options.toHashString(forMatched = false))
            append(":")
            append(parameterElement.contextKey).append("\u0000@").append(parameterElement.name)
            append("\u0000:")
            append(memberRole.ordinal)
            memberPath.subPaths.forEachFast { append("\u0000/").append(it) }
        }
    }

    override fun getConfigs(context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> {
        val memberPath = context.memberPath ?: return emptyList()
        if (memberPath.isNotEmpty()) return ParadoxConfigService.getFlattenedConfigsForConfigContext(context, options)
        val parameterElement = context.parameterElement ?: return emptyList()
        val rootConfigs = ParadoxParameterManager.getInferredContextConfigs(parameterElement)
        return ParadoxConfigService.getTopConfigsForConfigContext(context, rootConfigs)
    }

    override fun skipMissingExpressionCheck(context: CwtConfigContext): Boolean {
        // skip for declaration roots
        return context.isDeclarationRoot()
    }

    override fun skipTooManyExpressionCheck(context: CwtConfigContext): Boolean {
        // skip for declaration roots
        return context.isDeclarationRoot()
    }
}

/**
 * 提供内联脚本用法中的规则上下文。
 */
class CwtInlineScriptUsageConfigContextProvider : CwtConfigContextProvider {
    // 注意：内联脚本用法可以在定义声明之外
    // 注意这里的 `fileInfo` 可以为 `null`（例如，在内联脚本参数的多行参数值中）

    override fun getContext(configGroup: CwtConfigGroup, element: ParadoxScriptMember, file: PsiFile, memberRole: ParadoxMemberRole, memberPathFromFile: ParadoxMemberPath): CwtConfigContext? {
        if (memberPathFromFile.subPaths.noneFast { ParadoxInlineScriptManager.isMatched(it) }) return null // 要求当前位置相对于文件的成员路径中包含子路径 `inline_script`
        if (!ParadoxInlineScriptManager.isSupported(configGroup.gameType)) return null // 忽略游戏类型不支持的情况
        val vFile = selectFile(file)
        if (vFile == null) return null
        val prefixLength = memberPathFromFile.subPaths.indexOfFirst { ParadoxInlineScriptManager.isMatched(it) } + 1
        val memberPath = ParadoxMemberPath.resolve(memberPathFromFile.subPaths.dropFast(prefixLength))
        val context = CwtConfigContext.createFromMember(configGroup, memberRole, memberPathFromFile, memberPath, this)
        context.element = element // necessary
        context.rootFile = selectRootFile(file) // necessary
        return context
    }

    override fun getCacheKey(context: CwtConfigContext, options: ParadoxMatchOptions?): String? {
        val gameType = context.gameType
        val memberRole = context.memberRole
        val memberPath = context.memberPath ?: return null // null -> unexpected
        return buildString {
            append(gameType.ordinal)
            append("@isu@")
            append(options.toHashString(forMatched = false))
            append(":")
            append(memberRole.ordinal)
            memberPath.subPaths.forEachFast { append("\u0000/").append(it) }
        }
    }

    override fun getConfigs(context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> {
        val memberPath = context.memberPath ?: return emptyList()
        if (memberPath.isNotEmpty()) return ParadoxConfigService.getFlattenedConfigsForConfigContext(context, options)
        val inlineConfigs = context.configGroup.macroModel.forInlineScripts.orNull() ?: return emptyList()
        val rootConfigs = inlineConfigs.mapFast { CwtConfigManipulationService.inlineMacro(it) }
        return ParadoxConfigService.getTopConfigsForConfigContext(context, rootConfigs)
    }

    override fun skipUnresolvedExpressionCheck(context: CwtConfigContext): Boolean {
        // skip for root key
        return context.isDeclarationRoot() && context.memberRole == ParadoxMemberRole.Property
    }
}

/**
 * 提供内联脚本文件中的规则上下文。
 *
 * - 对于顶级成员，禁用以下代码检查：`MissingExpressionInspection`、`TooManyExpressionInspection`。
 * - 会将内联脚本内容内联到对应的调用处，然后再进行相关代码检查。
 */
class CwtInlineScriptFileConfigContextProvider : CwtConfigContextProvider {
    // 获取上下文规则后才能确定是否存在冲突以及是否存在递归
    // TODO 1.1.0+ 支持解析内联脚本文件中的定义声明

    override fun getContext(configGroup: CwtConfigGroup, element: ParadoxScriptMember, file: PsiFile, memberRole: ParadoxMemberRole, memberPathFromFile: ParadoxMemberPath): CwtConfigContext? {
        val vFile = selectFile(file)
        if (vFile == null) return null
        if (VirtualFileService.isInjectedFile(vFile)) return null // ignored for injected psi
        val fileInfo = vFile.fileInfo
        if (fileInfo == null) return null
        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(vFile) ?: return null
        val context = CwtConfigContext.createFromFile(configGroup, memberRole, memberPathFromFile, this)
        context.element = element // necessary
        context.rootFile = selectRootFile(file) // necessary
        context.inlineScriptExpression = inlineScriptExpression
        return context
    }

    override fun getCacheKey(context: CwtConfigContext, options: ParadoxMatchOptions?): String? {
        val gameType = context.gameType
        val memberRole = context.memberRole
        val memberPath = context.memberPath ?: return null // null -> unexpected
        val inlineScriptExpression = context.inlineScriptExpression ?: return null // null -> unexpected
        return buildString {
            append(gameType.ordinal)
            append("@is@")
            append(options.toHashString(forMatched = false))
            append(":")
            append(inlineScriptExpression)
            append("\u0000:")
            append(memberRole.ordinal)
            memberPath.subPaths.forEachFast { append("\u0000/").append(it) }
        }
    }

    override fun getConfigs(context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> {
        val memberPath = context.memberPath ?: return emptyList() // null -> unexpected
        val element = context.element ?: return emptyList() // null -> unexpected
        if (memberPath.isNotEmpty()) return ParadoxConfigService.getFlattenedConfigsForConfigContext(context, options)
        val inlineScriptExpression = context.inlineScriptExpression ?: return emptyList()
        val rootConfigs = ParadoxInlineScriptManager.getInferredContextConfigs(inlineScriptExpression, element, context, options)
        return ParadoxConfigService.getTopConfigsForConfigContext(context, rootConfigs)
    }

    override fun skipMissingExpressionCheck(context: CwtConfigContext): Boolean {
        // skip for declaration roots
        return context.isDeclarationRoot()
    }

    override fun skipTooManyExpressionCheck(context: CwtConfigContext): Boolean {
        // skip for declaration roots
        return context.isDeclarationRoot()
    }
}

/**
 * 提供定义注入声明中的规则上下文。
 *
 * - 基于文件信息（包括注入的文件信息）和成员路径。
 * - 对于顶级成员，禁用以下代码检查：`MissingExpressionInspection`、`TooManyExpressionInspection`。
 * - （目前）不会先内联目标定义声明中的内容，然后再进行相关代码检查。
 */
class CwtDefinitionInjectionConfigContextProvider : CwtConfigContextProvider {
    override fun getContext(configGroup: CwtConfigGroup, element: ParadoxScriptMember, file: PsiFile, memberRole: ParadoxMemberRole, memberPathFromFile: ParadoxMemberPath): CwtConfigContext? {
        if (memberPathFromFile.isEmpty()) return null
        if (!ParadoxDefinitionInjectionManager.isSupported(configGroup.gameType)) return null // 忽略游戏类型不支持的情况
        val vFile = selectFile(file)
        if (vFile == null) return null
        val fileInfo = vFile.fileInfo
        if (fileInfo == null) return null
        val definitionInjection = selectScope { element.parentDefinitionInjection() } ?: return null
        val definitionInjectionInfo = definitionInjection.definitionInjectionInfo ?: return null
        val memberPath = ParadoxMemberPath.resolve(memberPathFromFile.subPaths.dropFast(1)) // 去除第一个子路径
        val context = CwtConfigContext.createFromMember(configGroup, memberRole, memberPathFromFile, memberPath, this)
        context.element = element // necessary
        context.rootFile = selectRootFile(file) // necessary
        context.definitionInjectionInfo = definitionInjectionInfo
        return context
    }

    override fun getCacheKey(context: CwtConfigContext, options: ParadoxMatchOptions?): String? {
        val gameType = context.gameType
        val memberRole = context.memberRole
        val memberPath = context.memberPath ?: return null // null -> unexpected
        val definitionInjectionInfo = context.definitionInjectionInfo ?: return null
        return buildString {
            append(gameType.ordinal)
            append("@di@")
            append(options.toHashString(forMatched = false))
            append(":")
            append(definitionInjectionInfo.type).append("\u0000@").append(definitionInjectionInfo.target)
            append("\u0000:")
            append(memberRole.ordinal)
            memberPath.subPaths.forEachFast { append("\u0000/").append(it) }
        }
    }

    override fun getConfigs(context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> {
        val memberPath = context.memberPath ?: return emptyList()
        if (memberPath.isNotEmpty()) return ParadoxConfigService.getFlattenedConfigsForConfigContext(context, options)
        val definitionInjectionInfo = context.definitionInjectionInfo ?: return emptyList()
        val declaration = definitionInjectionInfo.declaration ?: return emptyList()
        val rootConfigs = declaration.to.singletonList()
        return ParadoxConfigService.getTopConfigsForConfigContext(context, rootConfigs)
    }

    override fun skipUnresolvedExpressionCheck(context: CwtConfigContext): Boolean {
        // skip for root key (definition injection expression)
        return context.isDeclarationRoot() && context.memberRole == ParadoxMemberRole.Property
    }

    override fun skipMissingExpressionCheck(context: CwtConfigContext): Boolean {
        // skip for declaration roots
        return context.isDeclarationRoot()
    }

    override fun skipTooManyExpressionCheck(context: CwtConfigContext): Boolean {
        // skip for declaration roots
        return context.isDeclarationRoot()
    }
}
