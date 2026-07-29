package icu.windea.pls.lang.resolve

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.ep.resolve.config.CwtConfigContextProvider
import icu.windea.pls.lang.inspections.script.expression.MissingExpressionInspection
import icu.windea.pls.lang.inspections.script.expression.TooManyExpressionInspection
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.model.ParadoxDefineVariableInfo
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.model.type.ParadoxMemberRole
import icu.windea.pls.script.psi.ParadoxScriptMember
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * 规则上下文。
 *
 * 用于后续获取对应的上下文规则以及匹配的规则，从而提供各种高级语言功能。
 *
 * 备注：
 * - 上下文规则可视为当前位置适用的所有规则，基本上等同于进行代码补全时可用的所有规则（但存在一些细节上的区别）。
 * - 得到的上下文规则是经过处理后的规则，例如展开别名（不会展开别名键引用和并集值引用）。
 * - 规则上下文不一定存在对应的上下文规则。
 * - 如果一个规则上下文开始存在对应的上下文规则，并且需要在子上下文中展开，则视作根上下文。
 *
 * @property memberPathFromFile 相对于所在文件的成员路径。
 * @property memberPath 相对于根上下文的成员路径。
 * @property provider 上下文提供者。
 *
 * @see CwtConfigContextProvider
 */
interface CwtConfigContext : UserDataHolder {
    val element: ParadoxScriptMember
    val configGroup: CwtConfigGroup
    val memberRole: ParadoxMemberRole
    val memberPathFromFile: ParadoxMemberPath?
    val memberPath: ParadoxMemberPath?
    val declarationRoot: Boolean
    val provider: CwtConfigContextProvider

    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType

    /** 是否是整个文件中的根上下文。 */
    fun inRoot(): Boolean

    /** 是否是某种特定声明（如定义、定义注入、定值变量）的根上下文。 */
    fun isDeclarationRoot(): Boolean

    /** 得到一组作为上下文的成员规则。 */
    fun getConfigs(options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>>

    /** 是否跳过代码检查 [MissingExpressionInspection] */
    fun skipMissingExpressionCheck(): Boolean

    /** 是否跳过代码检查 [TooManyExpressionInspection] */
    fun skipTooManyExpressionCheck(): Boolean

    object Keys : KeyRegistry()

    companion object {
        @JvmStatic
        fun create(
            element: ParadoxScriptMember,
            configGroup: CwtConfigGroup,
            memberRole: ParadoxMemberRole,
            declarationRoot: Boolean,
            provider: CwtConfigContextProvider,
            memberPathFromFile: ParadoxMemberPath?,
        ): CwtConfigContext {
            return CwtBaseConfigContext(element, configGroup, memberRole, declarationRoot, provider, memberPathFromFile)
        }

        @JvmStatic
        fun createFromFile(
            element: ParadoxScriptMember,
            configGroup: CwtConfigGroup,
            memberRole: ParadoxMemberRole,
            declarationRoot: Boolean,
            provider: CwtConfigContextProvider,
            memberPathFromFile: ParadoxMemberPath?,
        ): CwtConfigContext {
            return CwtFromFileConfigContext(element, configGroup, memberRole, declarationRoot, provider, memberPathFromFile)
        }

        @JvmStatic
        fun createFromMember(
            element: ParadoxScriptMember,
            configGroup: CwtConfigGroup,
            memberRole: ParadoxMemberRole,
            declarationRoot: Boolean,
            provider: CwtConfigContextProvider,
            memberPathFromFile: ParadoxMemberPath?,
            memberPath: ParadoxMemberPath?,
        ): CwtConfigContext {
            return CwtFromMemberConfigContext(element, configGroup, memberRole, declarationRoot, provider, memberPathFromFile, memberPath)
        }
    }
}

// region Accessors

var CwtConfigContext.dynamic: Boolean by registerKey(CwtConfigContext.Keys, false)
val CwtConfigContext.dynamicCache: ConcurrentMap<String, List<CwtMemberConfig<*>>> by registerKey(CwtConfigContext.Keys) { ConcurrentHashMap() }

var CwtConfigContext.definitionInfo: ParadoxDefinitionInfo? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.defineVariableInfo: ParadoxDefineVariableInfo? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.parameterElement: ParadoxParameterLightElement? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.parameterValueQuoted: Boolean? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.inlineScriptExpression: String? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.inlineScriptHasConflict: Boolean? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.inlineScriptHasRecursion: Boolean? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.definitionInjectionInfo: ParadoxDefinitionInjectionInfo? by registerKey(CwtConfigContext.Keys)

// endregion

// region Implementations

// 12 + 5 * 4 + 1 = 33 -> 40
private sealed class CwtConfigContextBase(
    override val element: ParadoxScriptMember,
    override val configGroup: CwtConfigGroup, // 3.0.1 use `element` directly here, no smart pointer since it's cached on PSI level
    override val memberRole: ParadoxMemberRole, // 3.0.1 use `memberRole` directly here, no optimization (compress to byte) since it's cached on PSI level
    override val declarationRoot: Boolean,
    override val provider: CwtConfigContextProvider,
) : UserDataHolderBase(), CwtConfigContext {
    override fun inRoot(): Boolean {
        return memberPath != null
    }

    override fun isDeclarationRoot(): Boolean {
        return memberPath != null && declarationRoot
    }

    override fun getConfigs(options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> {
        return ParadoxConfigService.getConfigsForConfigContext(this, options)
    }

    override fun skipMissingExpressionCheck(): Boolean {
        return provider.skipMissingExpressionCheck(this)
    }

    override fun skipTooManyExpressionCheck(): Boolean {
        return provider.skipTooManyExpressionCheck(this)
    }

    override fun toString(): String {
        return "CwtConfigContext(configGroup=$configGroup" +
            ", element=$element" +
            ", memberRole=$memberRole" +
            ", memberPathFromFile=$memberPathFromFile" +
            ", memberPath=$memberPath" +
            ", declarationRoot=$declarationRoot" +
            ", provider=$provider" +
            ")"
    }
}

// 12 + 6 * 4 + 1 = 37 -> 40
private class CwtBaseConfigContext(
    element: ParadoxScriptMember,
    configGroup: CwtConfigGroup,
    memberRole: ParadoxMemberRole,
    declarationRoot: Boolean,
    provider: CwtConfigContextProvider,
    memberPathFromFile: ParadoxMemberPath?,
) : CwtConfigContextBase(element, configGroup, memberRole, declarationRoot, provider) {
    override val memberPathFromFile: ParadoxMemberPath? = memberPathFromFile?.normalize()
    override val memberPath: ParadoxMemberPath? get() = null
}

// 12 + 6 * 4 + 1 = 37 -> 40
private class CwtFromFileConfigContext(
    element: ParadoxScriptMember,
    configGroup: CwtConfigGroup,
    memberRole: ParadoxMemberRole,
    declarationRoot: Boolean,
    provider: CwtConfigContextProvider,
    memberPathFromFile: ParadoxMemberPath?,
) : CwtConfigContextBase(element, configGroup, memberRole, declarationRoot, provider) {
    override val memberPathFromFile: ParadoxMemberPath? = memberPathFromFile?.normalize()
    override val memberPath: ParadoxMemberPath? get() = memberPathFromFile
}

// 12 + 7 * 4 + 1 = 41 -> 48
private class CwtFromMemberConfigContext(
    element: ParadoxScriptMember,
    configGroup: CwtConfigGroup,
    memberRole: ParadoxMemberRole,
    declarationRoot: Boolean,
    provider: CwtConfigContextProvider,
    memberPathFromFile: ParadoxMemberPath?,
    memberPath: ParadoxMemberPath?,
) : CwtConfigContextBase(element, configGroup, memberRole, declarationRoot, provider) {
    override val memberPathFromFile: ParadoxMemberPath? = memberPathFromFile?.normalize()
    override val memberPath: ParadoxMemberPath? = memberPath?.normalize()
}

// endregion
