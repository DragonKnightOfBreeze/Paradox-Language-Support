package icu.windea.pls.lang.codeInsight.completion

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.internal.CwtSchemaConfig
import icu.windea.pls.core.codeInsight.completion.CompletionContext
import icu.windea.pls.cwt.psi.CwtExpressionElement

data class CwtCompletionContext<T : PsiElement>(
    val globalContext: CompletionContext<T>,
    val expressionElement: CwtExpressionElement? = null,
    val containerElement: PsiElement? = null,
    val keyToMatch: String? = null,
    val optionContainerIdToMatch: String? = null,
    val schema: CwtSchemaConfig? = null,
    val contextConfigs: List<CwtMemberConfig<*>> = emptyList(),
    val isOptionKey: Boolean = false,
    val isOptionValue: Boolean = false,
    val isOptionBlockValue: Boolean = false,
    val inOption: Boolean = false,
    val isPropertyKey: Boolean = false,
    val isPropertyValue: Boolean = false,
    val isBlockValue: Boolean = false,
    val isKey: Boolean = false,
    val isKeyOnly: Boolean = false,
    val isValueOnly: Boolean = false,
)
