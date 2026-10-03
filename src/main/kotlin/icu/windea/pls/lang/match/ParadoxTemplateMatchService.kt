package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.match.CwtTemplateMatchService
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.lang.match.ParadoxExpressionMatchService.matchScriptExpression
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.script.ParadoxScriptLanguage

@Optimized
object ParadoxTemplateMatchService {
    fun matches(input: String, templateExpression: String, element: PsiElement, configGroup: CwtConfigGroup, options: ParadoxMatchOptions? = null): Boolean {
        return matches(input, CwtTemplateExpression.resolve(templateExpression), element, configGroup, options)
    }

    fun matches(input: String, templateExpression: CwtTemplateExpression, element: PsiElement, configGroup: CwtConfigGroup, options: ParadoxMatchOptions? = null): Boolean {
        if (templateExpression.expressionString.isEmpty()) return false
        val language = element.language
        if (language != ParadoxScriptLanguage) return false
        val snippetExpressions = templateExpression.snippetExpressions
        if (snippetExpressions.isEmpty()) return false
        val regex = CwtTemplateMatchService.toRegex(templateExpression)
        val matchResult = regex.matchEntire(input) ?: return false
        if (templateExpression.referenceExpressions.size != matchResult.groups.size - 1) return false
        var i = 1
        snippetExpressions.forEachFast f@{ snippetExpression ->
            ProgressManager.checkCanceled()
            if (snippetExpression.type == CwtDataTypes.Constant) return@f
            val matchGroup = matchResult.groups.get(i++) ?: return false
            val matchValue = matchGroup.value
            // NOTE 3.0.4 #430 post optimization: still match even if `matchValue` is empty (where snippet data type is `CwtDataTypes.Definition`, or not)
            // if (matchValue.isEmpty()) return false
            val matchContext = ParadoxExpressionMatchContext(element, ParadoxExpression.resolve(matchValue), configGroup, options)
            val matched = matchScriptExpression(matchContext, snippetExpression, null).get(options)
            if (!matched) return false
        }
        return true
    }
}
