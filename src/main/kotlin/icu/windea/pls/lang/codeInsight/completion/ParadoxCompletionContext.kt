package icu.windea.pls.lang.codeInsight.completion

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.codeInsight.completion.GlobalBasedCompletionContext
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.core.getKeyword
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.scope.ParadoxScopeContext

data class ParadoxCompletionContext(
    override val globalContext: GlobalCompletionContext,
    val configGroup: CwtConfigGroup,
    val keyword: String,
    val keywordOffset: Int = 0,
    val expressionOffset: Int = 0,
    val extraFilter: ((PsiElement) -> Boolean)? = null,
    /**  如果是 `null`，则表示已经填充的只是键或值的其中一部分 */
    val isKey: Boolean? = null,
    val config: CwtConfig<*>? = null,
    val configs: Collection<CwtConfig<*>> = emptyList(),
    val scopeContext: ParadoxScopeContext? = null,
    val scopeMatched: Boolean = true,
    val scopeName: String? = null,
    val scopeGroupName: String? = null,
    val isInt: Boolean? = null,
    val prefix: String? = null,
    val expressionTailText: String? = null,
    val contextKey: String? = null,
    val argumentNames: MutableSet<String>? = null,
    val node: ParadoxComplexExpressionNode? = null,
    /** 在对多参数动态链接的代码补全中，表示当前光标所处的参数索引（从0开始）。 */
    val linkArgIndex: Int = 0,
) : GlobalBasedCompletionContext() {
    val gameType: ParadoxGameType get() = configGroup.gameType

    companion object {
        @JvmStatic
        fun create(globalContext: GlobalCompletionContext): ParadoxCompletionContext {
            return ParadoxCompletionContextBuilder.build(globalContext)
        }
    }
}

// region Implementations

private object ParadoxCompletionContextBuilder {
    fun build(globalContext: GlobalCompletionContext): ParadoxCompletionContext {
        val gameType = selectGameType(globalContext.file)
        val configGroup = PlsFacade.getConfigGroup(globalContext.project, gameType)
        val keyword = globalContext.contextElement.getKeyword(globalContext.offsetInParent)

        return ParadoxCompletionContext(
            globalContext = globalContext,
            configGroup = configGroup,
            keyword = keyword,
        )
    }
}

// endregion
