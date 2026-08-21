package icu.windea.pls.ep.resolve.config

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.addExtensionPointListener
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.ep.resolve.expression.ParadoxScriptExpressionSupport
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.resolve.CwtConfigContext
import icu.windea.pls.lang.resolve.CwtDeclarationConfigContext
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.model.type.ParadoxMemberRole
import icu.windea.pls.script.psi.ParadoxScriptMember

/**
 * 提供脚本表达式所在的规则上下文。
 *
 * 说明：
 * - 通过 [CwtConfigContext] 可以得到上下文规则（基本等同于进行代码补全时可用的所有规则）。
 * - 接着可以进一步得到匹配的规则（等同于进行引用解析时可用的所有规则）。
 * - 最终再通过 [ParadoxScriptExpressionSupport] 驱动脚本表达式的各种语言功能。
 *
 * @see CwtConfigContext
 * @see CwtDeclarationConfigContext
 */
interface CwtConfigContextProvider {
    fun supports(gameType: ParadoxGameType): Boolean = true

    fun getContext(configGroup: CwtConfigGroup, element: ParadoxScriptMember, file: PsiFile, memberRole: ParadoxMemberRole, memberPathFromFile: ParadoxMemberPath): CwtConfigContext?

    fun getCacheKey(context: CwtConfigContext, options: ParadoxMatchOptions? = null): String?

    fun getConfigs(context: CwtConfigContext, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>>

    fun skipMissingExpressionCheck(context: CwtConfigContext) = false

    fun skipTooManyExpressionCheck(context: CwtConfigContext) = false

    fun skipUnresolvedExpressionCheck(context: CwtConfigContext) = false

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<CwtConfigContextProvider>("icu.windea.pls.configContextProvider")
        @JvmField val CACHE = LazyValue<List<CwtConfigContextProvider>>()

        fun getAll(): List<CwtConfigContextProvider> = CACHE.get().orEmpty()

        // region Implementations

        init {
            CACHE.reinitialize { compute() }
            EP_NAME.addExtensionPointListener { CACHE.reinitialize { compute() } }
        }

        private fun compute(): List<CwtConfigContextProvider> {
            return EP_NAME.extensionList.optimized()
        }

        // endregion
    }
}
