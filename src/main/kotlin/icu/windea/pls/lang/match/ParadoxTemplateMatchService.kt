package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.match.CwtTemplateMatchService
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.script.ParadoxScriptLanguage

@Optimized
object ParadoxTemplateMatchService {
    /**
     * 在语义级别，尝试将输入文本与模板表达式进行匹配。
     *
     * 说明：
     * - 使用完整模式。
     * - 一并匹配其中的引用片段，将其视为脚本表达式并匹配。
     *
     * @see CwtTemplateExpression
     * @see CwtTemplateMatchService.match
     */
    fun matches(input: String, templateExpression: CwtTemplateExpression, element: PsiElement, configGroup: CwtConfigGroup, options: ParadoxMatchOptions? = null): Boolean {
        if (templateExpression.expressionString.isEmpty()) return false
        val language = element.language
        if (language != ParadoxScriptLanguage) return false
        val snippetExpressions = templateExpression.snippetExpressions
        if (snippetExpressions.isEmpty()) return false
        val matchResult = CwtTemplateMatchService.match(input, templateExpression, incomplete = false)
        if (matchResult == null) return false
        val matchGroups = matchResult.groups
        if (matchGroups.size != snippetExpressions.size) return false
        matchGroups.forEachFast f@{ group ->
            ProgressManager.checkCanceled()
            if (group.expression.type == CwtDataTypes.Constant) return@f
            // NOTE 3.0.4 #430 post optimization: still match even if `matchValue` is empty (where snippet data type is `CwtDataTypes.Definition`, or not)
            // if (group.value.isEmpty()) return false
            val matchContext = ParadoxExpressionMatchContext(element, ParadoxExpression.resolve(group.value), configGroup, options)
            val matched = ParadoxExpressionMatchService.matchScriptExpression(matchContext, group.expression, null).get(options)
            if (!matched) return false
        }
        return true
    }
}
