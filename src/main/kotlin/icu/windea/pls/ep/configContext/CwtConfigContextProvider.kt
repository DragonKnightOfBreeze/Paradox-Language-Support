package icu.windea.pls.ep.configContext

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configContext.CwtConfigContext
import icu.windea.pls.ep.resolve.expression.ParadoxScriptExpressionSupport
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxScriptMember

/**
 * 提供脚本表达式所在的规则上下文。
 *
 * 通过规则上下文（[CwtConfigContext]）可以得到上下文规则（进行代码补全时可用的所有规则），
 * 接着可以进一步得到匹配的规则（进行引用解析时可用的所有规则），
 * 最终通过 [ParadoxScriptExpressionSupport] 驱动脚本表达式的各种语言功能。
 *
 * @see CwtMemberConfig
 * @see CwtConfigContext
 * @see ParadoxScriptExpressionSupport
 */
@WithGameTypeEP
interface CwtConfigContextProvider {
    fun getContext(element: ParadoxScriptMember, memberPath: ParadoxMemberPath, file: PsiFile): CwtConfigContext?

    fun getCacheKey(context: CwtConfigContext, matchOptions: Int = ParadoxMatchOptions.Default): String?

    fun getConfigs(context: CwtConfigContext, matchOptions: Int = ParadoxMatchOptions.Default): List<CwtMemberConfig<*>>?

    fun skipMissingExpressionCheck(context: CwtConfigContext) = false

    fun skipTooManyExpressionCheck(context: CwtConfigContext) = false

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtConfigContextProvider>("icu.windea.pls.configContextProvider")
    }
}
