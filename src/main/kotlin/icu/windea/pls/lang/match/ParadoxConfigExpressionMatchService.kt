package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigExpressionManager
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression

object ParadoxConfigExpressionMatchService {
    fun matchesTemplate(
        element: PsiElement,
        configGroup: CwtConfigGroup,
        expressionText: String,
        templateExpression: CwtTemplateExpression,
        options: ParadoxMatchOptions? = null,
    ): Boolean {
        val snippetExpressions = templateExpression.snippetExpressions
        if (snippetExpressions.isEmpty()) return false
        val expressionString = expressionText.unquote()
        val regex = CwtConfigExpressionManager.toRegex(templateExpression)
        val matchResult = regex.matchEntire(expressionString) ?: return false
        if (templateExpression.referenceExpressions.size != matchResult.groups.size - 1) return false
        var i = 1
        for (snippetExpression in snippetExpressions) {
            ProgressManager.checkCanceled()
            if (snippetExpression.type == CwtDataTypes.Constant) continue
            val matchGroup = matchResult.groups.get(i++) ?: return false
            val matchValue = matchGroup.value
            if (matchValue.isEmpty() && snippetExpression.type == CwtDataTypes.Definition) return false // skip anonymous definitions
            val context = ParadoxScriptExpressionMatchContext(element, ParadoxScriptExpression.resolve(matchValue, false), snippetExpression, null, configGroup, options)
            val matched = ParadoxMatchService.matchScriptExpression(context).get(options)
            if (!matched) return false
        }
        return true
    }
}
