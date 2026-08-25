package icu.windea.pls.lang.resolve

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.ep.resolve.config.CwtConfigContextProvider
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.model.type.ParadoxMemberRole
import icu.windea.pls.script.psi.ParadoxScriptMember

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
    val configGroup: CwtConfigGroup
    val memberRole: ParadoxMemberRole
    val memberPathFromFile: ParadoxMemberPath?
    val memberPath: ParadoxMemberPath?
    val provider: CwtConfigContextProvider

    val element: ParadoxScriptMember?
    val rootFile: VirtualFile?
    val dynamic: Boolean

    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType

    /** 将当前的上下文对象标记动态的。这意味着获取上下文规则时，会改为从上下文对象上的缓存中获取，而非从规则分组上的缓存中获取。 */
    fun markDynamic()

    /** 是否是某种根上下文，或其任意深度的子上下文。不检查 [memberRole]。 */
    fun inRoot(): Boolean

    /** 是否是某种特定声明（如定义、定义注入、定值变量）的根上下文，或其任意深度的子上下文。不检查 [memberRole]。 */
    @Suppress("unused")
    fun inDeclarationRoot(): Boolean

    /** 是否是某种特定声明（如定义、定义注入、定值变量）的根上下文。不检查 [memberRole]。 */
    fun isDeclarationRoot(): Boolean

    /** 得到一组作为上下文的成员规则。 */
    fun getConfigs(options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>>

    /**
     * 是否跳过无法解析的表达式的代码检查。
     *
     * @see icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
     * @see icu.windea.pls.lang.inspections.csv.expression.UnresolvedExpressionInspection
     */
    fun skipUnresolvedExpressionCheck(): Boolean = false

    /**
     * 是否跳过缺失的表达式的代码检查。
     *
     * @see icu.windea.pls.lang.inspections.script.expression.MissingExpressionInspection
     */
    fun skipMissingExpressionCheck(): Boolean = false

    /**
     * 是否跳过过多的表达式的代码检查。
     *
     * @see icu.windea.pls.lang.inspections.script.expression.TooManyExpressionInspection
     */
    fun skipTooManyExpressionCheck(): Boolean = false

    object Keys : KeyRegistry()

    companion object {
        @JvmStatic
        fun create(
            configGroup: CwtConfigGroup,
            memberRole: ParadoxMemberRole,
            memberPathFromFile: ParadoxMemberPath?,
            provider: CwtConfigContextProvider,
        ): CwtConfigContextBase {
            return CwtBaseConfigContext(configGroup, memberRole, memberPathFromFile, provider)
        }

        @JvmStatic
        fun createFromFile(
            configGroup: CwtConfigGroup,
            memberRole: ParadoxMemberRole,
            memberPathFromFile: ParadoxMemberPath?,
            provider: CwtConfigContextProvider,
        ): CwtConfigContextBase {
            return CwtFromFileConfigContext(configGroup, memberRole, memberPathFromFile, provider)
        }

        @JvmStatic
        fun createFromMember(
            configGroup: CwtConfigGroup,
            memberRole: ParadoxMemberRole,
            memberPathFromFile: ParadoxMemberPath?,
            memberPath: ParadoxMemberPath?,
            provider: CwtConfigContextProvider,
        ): CwtConfigContextBase {
            return CwtFromMemberConfigContext(configGroup, memberRole, memberPathFromFile, memberPath, provider)
        }
    }
}

// region Implementations

// 12 + 6 * 4 + 1 = 37 -> 40
sealed class CwtConfigContextBase(
    override val configGroup: CwtConfigGroup,
    override val memberRole: ParadoxMemberRole, // 3.0.1 use `memberRole` directly here, no optimization (compress to byte) since it's cached on PSI level
    override val provider: CwtConfigContextProvider,
) : UserDataHolderBase(), CwtConfigContext {
    @Volatile override var rootFile: VirtualFile? = null // 3.0.1 used to get cache from config group faster
    @Volatile override var element: ParadoxScriptMember? = null // 3.0.1 use `element` directly here, no smart pointer since it's cached on PSI level
    @Volatile override var dynamic: Boolean = false // 3.0.1 optimize: declared as field to optimize access performance

    override fun markDynamic() {
        dynamic = true
    }

    override fun inRoot(): Boolean {
        val memberPath = memberPath
        return memberPath != null
    }

    override fun inDeclarationRoot(): Boolean {
        if (this is CwtBaseConfigContext) return false
        val memberPath = memberPath
        return memberPath != null
    }

    override fun isDeclarationRoot(): Boolean {
        if (this is CwtBaseConfigContext) return false
        val memberPath = memberPath
        return memberPath != null && memberPath.isEmpty()
    }

    override fun getConfigs(options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> {
        return ParadoxConfigService.getConfigsForConfigContext(this, options)
    }

    override fun skipUnresolvedExpressionCheck(): Boolean {
        return provider.skipUnresolvedExpressionCheck(this)
    }

    override fun skipMissingExpressionCheck(): Boolean {
        return provider.skipMissingExpressionCheck(this)
    }

    override fun skipTooManyExpressionCheck(): Boolean {
        return provider.skipTooManyExpressionCheck(this)
    }

    override fun toString(): String {
        return "CwtConfigContext(configGroup=$configGroup" +
            ", memberRole=$memberRole" +
            ", memberPathFromFile=$memberPathFromFile" +
            ", memberPath=$memberPath" +
            ", dynamic=$dynamic" +
            ", provider=$provider" +
            ")"
    }
}

// 12 + 7 * 4 + 1 = 41 -> 48
private class CwtBaseConfigContext(
    configGroup: CwtConfigGroup,
    memberRole: ParadoxMemberRole,
    memberPathFromFile: ParadoxMemberPath?,
    provider: CwtConfigContextProvider,
) : CwtConfigContextBase(configGroup, memberRole, provider) {
    override val memberPathFromFile: ParadoxMemberPath? = memberPathFromFile?.normalize()
    override val memberPath: ParadoxMemberPath? get() = null
}

// 12 + 7 * 4 + 1 = 41 -> 48
private class CwtFromFileConfigContext(
    configGroup: CwtConfigGroup,
    memberRole: ParadoxMemberRole,
    memberPathFromFile: ParadoxMemberPath?,
    provider: CwtConfigContextProvider,
) : CwtConfigContextBase(configGroup, memberRole, provider) {
    override val memberPathFromFile: ParadoxMemberPath? = memberPathFromFile?.normalize()
    override val memberPath: ParadoxMemberPath? get() = memberPathFromFile
}

// 12 + 8 * 4 + 1 = 45 -> 48
private class CwtFromMemberConfigContext(
    configGroup: CwtConfigGroup,
    memberRole: ParadoxMemberRole,
    memberPathFromFile: ParadoxMemberPath?,
    memberPath: ParadoxMemberPath?,
    provider: CwtConfigContextProvider,
) : CwtConfigContextBase(configGroup, memberRole, provider) {
    override val memberPathFromFile: ParadoxMemberPath? = memberPathFromFile?.normalize()
    override val memberPath: ParadoxMemberPath? = memberPath?.normalize()
}

// endregion
